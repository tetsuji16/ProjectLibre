package com.projectlibre1.collaboration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Vector;
import java.util.Base64;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CollaborationMetadataStore {
	private static final Logger logger = Logger.getLogger(CollaborationMetadataStore.class.getName());
	private static final int MAX_METADATA_BYTES = 1024 * 1024; // 1 MB safety limit
	public static final int SCHEMA_VERSION = 1;

	private final File projectFile;
	private final File sidecarFile;

	public CollaborationMetadataStore(File projectFile) {
		this.projectFile = projectFile;
		this.sidecarFile = buildSidecarFile(projectFile);
	}

	public static boolean isCollaborationCandidate(String fileName) {
		if (fileName == null) {
			return false;
		}
		String lower = fileName.toLowerCase();
		return lower.endsWith(".pod") || lower.endsWith(".xml");
	}

	public static File buildSidecarFile(File projectFile) {
		String name = projectFile.getName();
		int dot = name.lastIndexOf('.');
		String base = dot >= 0 ? name.substring(0, dot) : name;
		File parent = projectFile.getParentFile();
		return parent != null ? new File(parent, base + ".projectlibre-sync.json") : new File(base + ".projectlibre-sync.json");
	}

	public File getSidecarFile() {
		return sidecarFile;
	}

	public Metadata load() {
		return withLockedMetadata(new MetadataCallback<Metadata>() {
			public Metadata execute(Metadata metadata) {
				return metadata;
			}
		});
	}

	public void mutate(MetadataMutation mutation) {
		withLockedMetadata(new MetadataCallback<Object>() {
			public Object execute(Metadata metadata) {
				mutation.mutate(metadata);
				return null;
			}
		});
	}

	public <T> T withLockedMetadata(MetadataCallback<T> callback) {
		RandomAccessFile raf = null;
		FileChannel channel = null;
		FileLock lock = null;
		try {
			File parent = sidecarFile.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
			raf = new RandomAccessFile(sidecarFile, "rw");
			channel = raf.getChannel();
			lock = channel.lock();
			byte[] bytes = readAllBytes(raf);
			Metadata metadata = bytes.length == 0 ? createDefaultMetadata() : parseMetadata(bytes);
			normalize(metadata);
			T result = callback.execute(metadata);
			writeMetadata(raf, metadata);
			return result;
		} catch (Exception e) {
			return callback.onError(e);
		} finally {
			try {
				if (lock != null) {
					lock.release();
				}
			} catch (IOException e) {
				logger.log(Level.FINE, "Failed to release file lock on {0}", sidecarFile);
			}
			try {
				if (channel != null) {
					channel.close();
				}
			} catch (IOException e) {
				logger.log(Level.FINE, "Failed to close file channel on {0}", sidecarFile);
			}
			try {
				if (raf != null) {
					raf.close();
				}
			} catch (IOException e) {
				logger.log(Level.FINE, "Failed to close RandomAccessFile on {0}", sidecarFile);
			}
		}
	}

	private byte[] readAllBytes(RandomAccessFile raf) throws IOException {
		raf.seek(0L);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int read;
		int total = 0;
		while ((read = raf.read(buf)) != -1) {
			total += read;
			if (total > MAX_METADATA_BYTES) {
				throw new IOException("Metadata file exceeds maximum allowed size of " + MAX_METADATA_BYTES + " bytes");
			}
			out.write(buf, 0, read);
		}
		return out.toByteArray();
	}

	private void writeMetadata(RandomAccessFile raf, Metadata metadata) throws IOException {
		byte[] bytes = toJsonBytes(metadata.toMap());
		raf.setLength(0L);
		raf.seek(0L);
		raf.write(bytes);
	}

	private Metadata createDefaultMetadata() {
		Metadata metadata = new Metadata();
		metadata.setSchemaVersion(SCHEMA_VERSION);
		metadata.setProjectFileName(projectFile.getName());
		metadata.setProjectFingerprint(projectFile.getName());
		refreshProjectStats(metadata);
		return metadata;
	}

	public void refreshProjectStats(Metadata metadata) {
		metadata.setProjectFileLastModified(projectFile.exists() ? projectFile.lastModified() : 0L);
		metadata.setProjectFileLength(projectFile.exists() ? projectFile.length() : 0L);
	}

	private Metadata parseMetadata(byte[] bytes) {
		String json = new String(bytes, StandardCharsets.UTF_8);
		Object parsed = new JsonParser(json).parseValue();
		if (!(parsed instanceof Map)) {
			return createDefaultMetadata();
		}
		return Metadata.fromMap((Map) parsed);
	}

	private byte[] toJsonBytes(Object value) {
		return JsonWriter.write(value).getBytes(StandardCharsets.UTF_8);
	}

	private void normalize(Metadata metadata) {
		if (metadata.getSchemaVersion() <= 0) {
			metadata.setSchemaVersion(SCHEMA_VERSION);
		}
		if (metadata.getProjectFileName() == null) {
			metadata.setProjectFileName(projectFile.getName());
		}
		if (metadata.getProjectFingerprint() == null) {
			metadata.setProjectFingerprint(projectFile.getName());
		}
		if (metadata.getUsers() == null) {
			metadata.setUsers(new LinkedHashMap<String, UserRecord>());
		}
		if (metadata.getLocks() == null) {
			metadata.setLocks(new LinkedHashMap<String, LockRecord>());
		}
		if (metadata.getPerUserWorkspace() == null) {
			metadata.setPerUserWorkspace(new LinkedHashMap<String, UserWorkspaceState>());
		}
	}

	public interface MetadataMutation {
		void mutate(Metadata metadata);
	}

	public static abstract class MetadataCallback<T> {
		public abstract T execute(Metadata metadata);

		public T onError(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static class Metadata implements java.io.Serializable {
		private static final long serialVersionUID = 1L;
		private int schemaVersion;
		private String projectFingerprint;
		private String projectFileName;
		private long projectFileLastModified;
		private long projectFileLength;
		private Map<String, UserRecord> users = new LinkedHashMap<String, UserRecord>();
		private Map<String, LockRecord> locks = new LinkedHashMap<String, LockRecord>();
		private Map<String, UserWorkspaceState> perUserWorkspace = new LinkedHashMap<String, UserWorkspaceState>();

		public static Metadata fromMap(Map map) {
			Metadata metadata = new Metadata();
			metadata.schemaVersion = asInt(map.get("schemaVersion"), SCHEMA_VERSION);
			metadata.projectFingerprint = asString(map.get("projectFingerprint"));
			metadata.projectFileName = asString(map.get("projectFileName"));
			metadata.projectFileLastModified = asLong(map.get("projectFileLastModified"));
			metadata.projectFileLength = asLong(map.get("projectFileLength"));
			Map usersMap = asMap(map.get("users"));
			if (usersMap != null) {
				Iterator it = usersMap.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					metadata.users.put(String.valueOf(entry.getKey()), UserRecord.fromMap(asMap(entry.getValue())));
				}
			}
			Map locksMap = asMap(map.get("locks"));
			if (locksMap != null) {
				Iterator it = locksMap.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					metadata.locks.put(String.valueOf(entry.getKey()), LockRecord.fromMap(asMap(entry.getValue())));
				}
			}
			Map workspaceMap = asMap(map.get("perUserWorkspace"));
			if (workspaceMap != null) {
				Iterator it = workspaceMap.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					metadata.perUserWorkspace.put(String.valueOf(entry.getKey()), fromWorkspaceMap(asMap(entry.getValue())));
				}
			}
			return metadata;
		}

		private static UserWorkspaceState fromWorkspaceMap(Map map) {
			UserWorkspaceState state = new UserWorkspaceState();
			if (map == null) {
				return state;
			}
			state.setUserKey(asString(map.get("userKey")));
			state.setDisplayName(asString(map.get("displayName")));
			state.setSavedAt(asLong(map.get("savedAt")));
			state.setWorkspacePayload(asString(map.get("workspacePayload")));
			return state;
		}

		public Map<String, Object> toMap() {
			Map<String, Object> map = new LinkedHashMap<String, Object>();
			map.put("schemaVersion", Integer.valueOf(schemaVersion));
			map.put("projectFingerprint", projectFingerprint);
			map.put("projectFileName", projectFileName);
			map.put("projectFileLastModified", Long.valueOf(projectFileLastModified));
			map.put("projectFileLength", Long.valueOf(projectFileLength));
			Map<String, Object> usersMap = new LinkedHashMap<String, Object>();
			for (Map.Entry<String, UserRecord> entry : users.entrySet()) {
				usersMap.put(entry.getKey(), entry.getValue().toMap());
			}
			map.put("users", usersMap);
			Map<String, Object> locksMap = new LinkedHashMap<String, Object>();
			for (Map.Entry<String, LockRecord> entry : locks.entrySet()) {
				locksMap.put(entry.getKey(), entry.getValue().toMap());
			}
			map.put("locks", locksMap);
			Map<String, Object> workspaceMap = new LinkedHashMap<String, Object>();
			for (Map.Entry<String, UserWorkspaceState> entry : perUserWorkspace.entrySet()) {
				Map<String, Object> stateMap = new LinkedHashMap<String, Object>();
				UserWorkspaceState state = entry.getValue();
				stateMap.put("userKey", state.getUserKey());
				stateMap.put("displayName", state.getDisplayName());
				stateMap.put("savedAt", Long.valueOf(state.getSavedAt()));
				stateMap.put("workspacePayload", state.getWorkspacePayload());
				workspaceMap.put(entry.getKey(), stateMap);
			}
			map.put("perUserWorkspace", workspaceMap);
			return map;
		}

		public int getSchemaVersion() {
			return schemaVersion;
		}

		public void setSchemaVersion(int schemaVersion) {
			this.schemaVersion = schemaVersion;
		}

		public String getProjectFingerprint() {
			return projectFingerprint;
		}

		public void setProjectFingerprint(String projectFingerprint) {
			this.projectFingerprint = projectFingerprint;
		}

		public String getProjectFileName() {
			return projectFileName;
		}

		public void setProjectFileName(String projectFileName) {
			this.projectFileName = projectFileName;
		}

		public long getProjectFileLastModified() {
			return projectFileLastModified;
		}

		public void setProjectFileLastModified(long projectFileLastModified) {
			this.projectFileLastModified = projectFileLastModified;
		}

		public long getProjectFileLength() {
			return projectFileLength;
		}

		public void setProjectFileLength(long projectFileLength) {
			this.projectFileLength = projectFileLength;
		}

		public Map<String, UserRecord> getUsers() {
			return users;
		}

		public void setUsers(Map<String, UserRecord> users) {
			this.users = users;
		}

		public Map<String, LockRecord> getLocks() {
			return locks;
		}

		public void setLocks(Map<String, LockRecord> locks) {
			this.locks = locks;
		}

		public Map<String, UserWorkspaceState> getPerUserWorkspace() {
			return perUserWorkspace;
		}

		public void setPerUserWorkspace(Map<String, UserWorkspaceState> perUserWorkspace) {
			this.perUserWorkspace = perUserWorkspace;
		}
	}

	public static class UserRecord implements java.io.Serializable {
		private static final long serialVersionUID = 1L;
		private String userKey;
		private String displayName;
		private String clientInstanceId;
		private long lastSeenAt;

		public static UserRecord fromMap(Map map) {
			UserRecord record = new UserRecord();
			if (map == null) {
				return record;
			}
			record.userKey = asString(map.get("userKey"));
			record.displayName = asString(map.get("displayName"));
			record.clientInstanceId = asString(map.get("clientInstanceId"));
			record.lastSeenAt = asLong(map.get("lastSeenAt"));
			return record;
		}

		public Map<String, Object> toMap() {
			Map<String, Object> map = new LinkedHashMap<String, Object>();
			map.put("userKey", userKey);
			map.put("displayName", displayName);
			map.put("clientInstanceId", clientInstanceId);
			map.put("lastSeenAt", Long.valueOf(lastSeenAt));
			return map;
		}

		public String getUserKey() {
			return userKey;
		}

		public void setUserKey(String userKey) {
			this.userKey = userKey;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getClientInstanceId() {
			return clientInstanceId;
		}

		public void setClientInstanceId(String clientInstanceId) {
			this.clientInstanceId = clientInstanceId;
		}

		public long getLastSeenAt() {
			return lastSeenAt;
		}

		public void setLastSeenAt(long lastSeenAt) {
			this.lastSeenAt = lastSeenAt;
		}
	}

	public static class LockRecord implements java.io.Serializable {
		private static final long serialVersionUID = 1L;
		private long taskId;
		private String ownerKey;
		private String userKey;
		private String displayName;
		private String clientInstanceId;
		private long leaseUntil;
		private long updatedAt;

		public static LockRecord fromMap(Map map) {
			LockRecord record = new LockRecord();
			if (map == null) {
				return record;
			}
			record.taskId = asLong(map.get("taskId"));
			record.ownerKey = asString(map.get("ownerKey"));
			record.userKey = asString(map.get("userKey"));
			record.displayName = asString(map.get("displayName"));
			record.clientInstanceId = asString(map.get("clientInstanceId"));
			record.leaseUntil = asLong(map.get("leaseUntil"));
			record.updatedAt = asLong(map.get("updatedAt"));
			return record;
		}

		public Map<String, Object> toMap() {
			Map<String, Object> map = new LinkedHashMap<String, Object>();
			map.put("taskId", Long.valueOf(taskId));
			map.put("ownerKey", ownerKey);
			map.put("userKey", userKey);
			map.put("displayName", displayName);
			map.put("clientInstanceId", clientInstanceId);
			map.put("leaseUntil", Long.valueOf(leaseUntil));
			map.put("updatedAt", Long.valueOf(updatedAt));
			return map;
		}

		public long getTaskId() {
			return taskId;
		}

		public void setTaskId(long taskId) {
			this.taskId = taskId;
		}

		public String getOwnerKey() {
			return ownerKey;
		}

		public void setOwnerKey(String ownerKey) {
			this.ownerKey = ownerKey;
		}

		public String getUserKey() {
			return userKey;
		}

		public void setUserKey(String userKey) {
			this.userKey = userKey;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getClientInstanceId() {
			return clientInstanceId;
		}

		public void setClientInstanceId(String clientInstanceId) {
			this.clientInstanceId = clientInstanceId;
		}

		public long getLeaseUntil() {
			return leaseUntil;
		}

		public void setLeaseUntil(long leaseUntil) {
			this.leaseUntil = leaseUntil;
		}

		public long getUpdatedAt() {
			return updatedAt;
		}

		public void setUpdatedAt(long updatedAt) {
			this.updatedAt = updatedAt;
		}
	}

	private static Map asMap(Object value) {
		return value instanceof Map ? (Map) value : null;
	}

	private static String asString(Object value) {
		return value == null ? null : String.valueOf(value);
	}

	private static long asLong(Object value) {
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		if (value == null) {
			return 0L;
		}
		try {
			return Long.parseLong(String.valueOf(value));
		} catch (NumberFormatException e) {
			return 0L;
		}
	}

	private static int asInt(Object value, int defaultValue) {
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		if (value == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(String.valueOf(value));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	private static class JsonWriter {
		static String write(Object value) {
			StringBuilder builder = new StringBuilder();
			appendValue(builder, value);
			return builder.toString();
		}

		private static void appendValue(StringBuilder builder, Object value) {
			if (value == null) {
				builder.append("null");
			} else if (value instanceof String) {
				appendString(builder, (String) value);
			} else if (value instanceof Number || value instanceof Boolean) {
				builder.append(String.valueOf(value));
			} else if (value instanceof Map) {
				appendMap(builder, (Map) value);
			} else if (value instanceof Collection) {
				appendCollection(builder, (Collection) value);
			} else {
				appendString(builder, String.valueOf(value));
			}
		}

		private static void appendMap(StringBuilder builder, Map map) {
			builder.append('{');
			boolean first = true;
			Iterator it = map.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				if (!first) {
					builder.append(',');
				}
				first = false;
				appendString(builder, String.valueOf(entry.getKey()));
				builder.append(':');
				appendValue(builder, entry.getValue());
			}
			builder.append('}');
		}

		private static void appendCollection(StringBuilder builder, Collection values) {
			builder.append('[');
			boolean first = true;
			Iterator it = values.iterator();
			while (it.hasNext()) {
				if (!first) {
					builder.append(',');
				}
				first = false;
				appendValue(builder, it.next());
			}
			builder.append(']');
		}

		private static void appendString(StringBuilder builder, String value) {
			builder.append('"');
			for (int i = 0; i < value.length(); i++) {
				char c = value.charAt(i);
				switch (c) {
				case '\\':
					builder.append("\\\\");
					break;
				case '"':
					builder.append("\\\"");
					break;
				case '\n':
					builder.append("\\n");
					break;
				case '\r':
					builder.append("\\r");
					break;
				case '\t':
					builder.append("\\t");
					break;
				default:
					if (c < 0x20) {
						builder.append(String.format("\\u%04x", Integer.valueOf(c)));
					} else {
						builder.append(c);
					}
				}
			}
			builder.append('"');
		}
	}

	private static class JsonParser {
		private final String text;
		private int index;

		JsonParser(String text) {
			this.text = text == null ? "" : text;
		}

		Object parseValue() {
			skipWhitespace();
			if (index >= text.length()) {
				return null;
			}
			char c = text.charAt(index);
			if (c == '{') {
				return parseObject();
			}
			if (c == '[') {
				return parseArray();
			}
			if (c == '"') {
				return parseString();
			}
			if (c == 't' || c == 'f') {
				return parseBoolean();
			}
			if (c == 'n') {
				index += 4;
				return null;
			}
			return parseNumber();
		}

		private Map<String, Object> parseObject() {
			Map<String, Object> map = new LinkedHashMap<String, Object>();
			index++;
			while (true) {
				skipWhitespace();
				if (index >= text.length()) {
					return map;
				}
				if (text.charAt(index) == '}') {
					index++;
					return map;
				}
				String key = parseString();
				skipWhitespace();
				if (index < text.length() && text.charAt(index) == ':') {
					index++;
				}
				Object value = parseValue();
				map.put(key, value);
				skipWhitespace();
				if (index < text.length() && text.charAt(index) == ',') {
					index++;
				}
			}
		}

		private List<Object> parseArray() {
			List<Object> list = new Vector<Object>();
			index++;
			while (true) {
				skipWhitespace();
				if (index >= text.length()) {
					return list;
				}
				if (text.charAt(index) == ']') {
					index++;
					return list;
				}
				list.add(parseValue());
				skipWhitespace();
				if (index < text.length() && text.charAt(index) == ',') {
					index++;
				}
			}
		}

		private String parseString() {
			StringBuilder builder = new StringBuilder();
			if (text.charAt(index) == '"') {
				index++;
			}
			while (index < text.length()) {
				char c = text.charAt(index++);
				if (c == '"') {
					return builder.toString();
				}
				if (c == '\\' && index < text.length()) {
					char escaped = text.charAt(index++);
					switch (escaped) {
					case '"':
						builder.append('"');
						break;
					case '\\':
						builder.append('\\');
						break;
					case 'n':
						builder.append('\n');
						break;
					case 'r':
						builder.append('\r');
						break;
					case 't':
						builder.append('\t');
						break;
					case 'u':
						if (index + 4 <= text.length()) {
							String hex = text.substring(index, index + 4);
							builder.append((char) Integer.parseInt(hex, 16));
							index += 4;
						}
						break;
					default:
						builder.append(escaped);
					}
				} else {
					builder.append(c);
				}
			}
			return builder.toString();
		}

		private Boolean parseBoolean() {
			if (text.startsWith("true", index)) {
				index += 4;
				return Boolean.TRUE;
			}
			index += 5;
			return Boolean.FALSE;
		}

		private Number parseNumber() {
			int start = index;
			while (index < text.length()) {
				char c = text.charAt(index);
				if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') {
					index++;
				} else {
					break;
				}
			}
			String number = text.substring(start, index);
			if (number.indexOf('.') >= 0 || number.indexOf('e') >= 0 || number.indexOf('E') >= 0) {
				return Double.valueOf(number);
			}
			return Long.valueOf(number);
		}

		private void skipWhitespace() {
			while (index < text.length()) {
				char c = text.charAt(index);
				if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
					index++;
				} else {
					return;
				}
			}
		}
	}
}
