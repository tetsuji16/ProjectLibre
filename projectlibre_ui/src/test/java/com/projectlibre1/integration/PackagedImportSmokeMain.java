package com.projectlibre1.integration;

import com.projectlibre1.collaboration.ProjectMergeService;
import com.projectlibre1.pm.task.Project;

public final class PackagedImportSmokeMain {
	private PackagedImportSmokeMain() {
	}

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			throw new IllegalArgumentException("usage: PackagedImportSmokeMain <file>...");
		}
		ProjectMergeService mergeService = new ProjectMergeService();
		for (String fileName : args) {
			Project project = mergeService.loadExternalProject(fileName);
			if (project == null) {
				throw new IllegalStateException("Failed to load project file: " + fileName);
			}
			System.out.println("OK " + fileName + " tasks=" + project.getTasks().size());
		}
	}
}
