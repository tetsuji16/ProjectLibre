package com.projectlibre1.collaboration;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import com.projectlibre1.exchange.FileImporter;
import com.projectlibre1.pm.resource.ResourcePool;
import com.projectlibre1.pm.resource.ResourcePoolFactory;
import com.projectlibre1.pm.task.Project;
import com.projectlibre1.pm.task.ProjectFactory;
import com.projectlibre1.pm.task.Task;
import com.projectlibre1.server.data.DataUtil;
import com.projectlibre1.session.LocalSession;
import com.projectlibre1.undo.DataFactoryUndoController;

public class ProjectMergeService {
	public Project loadExternalProject(String fileName) {
		if (fileName == null) {
			return null;
		}
		try {
			if (fileName.toLowerCase().endsWith(".pod")) {
				com.projectlibre1.exchange.LocalFileImporter importer = new com.projectlibre1.exchange.LocalFileImporter();
				importer.setFileName(fileName);
				importer.setProjectFactory(ProjectFactory.getInstance());
				importer.importFile();
				return importer.getProject();
			}
			InputStream in = new FileInputStream(fileName);
			try {
				FileImporter importer = LocalSession.getImporter(LocalSession.MICROSOFT_PROJECT_IMPORTER);
				DataFactoryUndoController undoController = new DataFactoryUndoController();
				ResourcePool resourcePool = ResourcePoolFactory.getInstance().createResourcePool("", undoController);
				resourcePool.setLocal(true);
				Project project = Project.createProject(resourcePool, undoController);
				importer.setProject(project);
				importer.setProjectFactory(ProjectFactory.getInstance());
				return importer.loadProject(in);
			} finally {
				in.close();
			}
		} catch (Exception e) {
			return null;
		}
	}

	public Set<Long> findDeletedTasks(String fileName, Set<Long> lockedTaskIds) {
		Set<Long> deleted = new LinkedHashSet<Long>();
		if (lockedTaskIds == null || lockedTaskIds.isEmpty()) {
			return deleted;
		}
		Project external = loadExternalProject(fileName);
		if (external == null) {
			return deleted;
		}
		for (Long taskId : lockedTaskIds) {
			Task task = external.findByUniqueId(taskId.longValue());
			if (task == null) {
				deleted.add(taskId);
			}
		}
		return deleted;
	}
}
