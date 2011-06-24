package org.protege.owl.codegeneration;

import java.io.File;

public class DeleteFolder {
	public static void deleteFolder(File folder) {
		if (folder.exists() && folder.isDirectory()) {
			for (File toDelete : folder.listFiles()) {
				if (toDelete.isDirectory()) {
					deleteFolder(toDelete);
				}
				else {
					toDelete.delete();
				}
			}
			folder.delete();
		}
	}
}
