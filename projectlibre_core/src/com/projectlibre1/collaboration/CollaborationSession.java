package com.projectlibre1.collaboration;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.projectlibre1.collaboration.CollaborationMetadataStore.Metadata;
import com.projectlibre1.collaboration.CollaborationMetadataStore.UserRecord;
import com.projectlibre1.pm.task.Project;
import com.projectlibre1.pm.task.Task;
import com.projectlibre1.util.Alert;
import com.projectlibre1.workspace.WorkspaceSetting;

public class CollaborationSession {
	private static final Logger logger = Logger.getLogger(CollaborationSession.class.getName());
	private static final long POLL_INTERVAL_MS = 2000L;
	private static final long HEARTBEAT_INTERVAL_MS = 5000L;

	public static final int SAVE_CANCEL = 0;
	public static final int SAVE_PROCEED = 1;
	public static final int SAVE_AS_COPY = 2;

	private final Project project;
	private final File projectFile;
	private final CollaborationMetadataStore store;
	private final TaskLockManager lockManager;
	private final ProjectMergeService mergeService;
	private final String userKey;
	private final String displayName;
	private final String clientInstanceId;
	private volatile Timer timer;
	private long lastKnownProjectModified;
	private long lastKnownProjectLength;
	private long lastKnownSidecarModified;
	private volatile boolean externalChangePending;
	private volatile boolean externalChangeWarned;
	private volatile long lastHeartbeatAt;

	public CollaborationSession(Project project, String fileName, String userKey) {
		this.project = project;
		this.projectFile = fileName == null ? null : new File(fileName);
		this.userKey = userKey == null || userKey.trim().length() == 0 ? "unknown" : userKey.trim();
		this.displayName = this.userKey;
		this.clientInstanceId = UUID.randomUUID().toString();
		this.store = new CollaborationMetadataStore(projectFile);
		this.lockManager = new TaskLockManager(store, this.userKey, this.displayName, this.clientInstanceId);
		this.mergeService = new ProjectMergeService();
		refreshKnownFileStats();
	}

	public static CollaborationSession create(Project project, String fileName, String userKey) {
		if (project == null || fileName == null || !CollaborationMetadataStore.isCollaborationCandidate(fileName)) {
			return null;
		}
		return new CollaborationSession(project, fileName, userKey);
	}

	public void start() {
		registerUser();
		timer = new Timer("projectlibre-collaboration-" + clientInstanceId, true);
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				poll();
			}
		}, POLL_INTERVAL_MS, POLL_INTERVAL_MS);
	}

	public void stop() {
		if (timer == null) {
			return;
		}
		try {
			saveWorkspace(project.getCollaborationWorkspace());
		} catch (Exception e) {
			logger.log(Level.FINE, "Failed to save workspace on session stop", e);
		}
		lockManager.releaseAll();
		timer.cancel();
		timer = null;
	}

	private void registerUser() {
		store.mutate(metadata -> {
			UserRecord record = metadata.getUsers().get(userKey);
			if (record == null) {
				record = new UserRecord();
				record.setUserKey(userKey);
				metadata.getUsers().put(userKey, record);
			}
			record.setDisplayName(displayName);
			record.setClientInstanceId(clientInstanceId);
			record.setLastSeenAt(System.currentTimeMillis());
			store.refreshProjectStats(metadata);
		});
	}

	private void poll() {
		if (projectFile == null) {
			return;
		}
		long now = System.currentTimeMillis();
		if (now - lastHeartbeatAt >= HEARTBEAT_INTERVAL_MS) {
			lockManager.renewAll();
			lastHeartbeatAt = now;
			registerUser();
		}
		long projectModified = projectFile.exists() ? projectFile.lastModified() : 0L;
		long projectLength = projectFile.exists() ? projectFile.length() : 0L;
		long sidecarModified = store.getSidecarFile().exists() ? store.getSidecarFile().lastModified() : 0L;
		if (projectModified != lastKnownProjectModified || projectLength != lastKnownProjectLength || sidecarModified != lastKnownSidecarModified) {
			externalChangePending = true;
			lastKnownProjectModified = projectModified;
			lastKnownProjectLength = projectLength;
			lastKnownSidecarModified = sidecarModified;
			if (!externalChangeWarned) {
				externalChangeWarned = true;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						Alert.warn("This project was updated by another user. Save will re-check for conflicts before writing.");
					}
				});
			}
		}
	}

	private void refreshKnownFileStats() {
		if (projectFile == null) {
			return;
		}
		lastKnownProjectModified = projectFile.exists() ? projectFile.lastModified() : 0L;
		lastKnownProjectLength = projectFile.exists() ? projectFile.length() : 0L;
		lastKnownSidecarModified = store.getSidecarFile().exists() ? store.getSidecarFile().lastModified() : 0L;
	}

	public boolean tryLockTask(Task task, Component parent, String actionLabel) {
		if (task == null) {
			return true;
		}
		long taskId = task.getUniqueId();
		if (lockManager.isLockedByCurrentUser(taskId)) {
			return true;
		}
		if (!lockManager.acquire(taskId)) {
			String owner = lockManager.describeOwner(taskId);
			if (owner == null || owner.length() == 0) {
				owner = "another user";
			}
			Alert.warn("Cannot " + actionLabel + " task \"" + task.getName() + "\" because it is locked by " + owner + ".", parent);
			return false;
		}
		return true;
	}

	public boolean tryLockTasks(Iterable<Task> tasks, Component parent, String actionLabel) {
		if (tasks == null) {
			return true;
		}
		for (Task task : tasks) {
			if (!tryLockTask(task, parent, actionLabel)) {
				return false;
			}
		}
		return true;
	}

	public int checkBeforeSave(Component parent) {
		if (!externalChangePending) {
			return SAVE_PROCEED;
		}
		Set<Long> deleted = mergeService.findDeletedTasks(projectFile.getAbsolutePath(), lockManager.getLocalLocks());
		if (deleted.isEmpty()) {
			return SAVE_PROCEED;
		}
		Object[] options = new Object[] {
			"Restore and Save",
			"Discard My Changes",
			"Save Copy"
		};
		int result = JOptionPane.showOptionDialog(parent,
			"One or more tasks you are editing were deleted by another user.\nChoose how to resolve the conflict.",
			"ProjectLibre",
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.WARNING_MESSAGE,
			null,
			options,
			options[0]);
		if (result == 0) {
			return SAVE_PROCEED;
		}
		if (result == 2) {
			return SAVE_AS_COPY;
		}
		return SAVE_CANCEL;
	}

	public void afterSave() {
		store.mutate(metadata -> {
			store.refreshProjectStats(metadata);
		});
		refreshKnownFileStats();
		externalChangePending = false;
		externalChangeWarned = false;
	}

	public void saveWorkspace(WorkspaceSetting workspace) {
		if (workspace == null) {
			return;
		}
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream objectOut = new ObjectOutputStream(out);
			objectOut.writeObject(workspace);
			objectOut.close();
			String payload = Base64.getEncoder().encodeToString(out.toByteArray());
			store.mutate(metadata -> {
				UserWorkspaceState state = new UserWorkspaceState();
				state.setUserKey(userKey);
				state.setDisplayName(displayName);
				state.setSavedAt(System.currentTimeMillis());
				state.setWorkspacePayload(payload);
				metadata.getPerUserWorkspace().put(userKey, state);
				store.refreshProjectStats(metadata);
			});
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to save workspace state for user " + userKey, e);
		}
	}

	public WorkspaceSetting loadWorkspace() {
		Metadata metadata = store.load();
		if (metadata == null) {
			return null;
		}
		UserWorkspaceState state = metadata.getPerUserWorkspace().get(userKey);
		if (state == null || state.getWorkspacePayload() == null) {
			return null;
		}
		try {
			byte[] data = Base64.getDecoder().decode(state.getWorkspacePayload());
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
			try {
				return (WorkspaceSetting) in.readObject();
			} finally {
				in.close();
			}
		} catch (Exception e) {
			return null;
		}
	}

	public String getSidecarFileName() {
		return store.getSidecarFile().getAbsolutePath();
	}
}
