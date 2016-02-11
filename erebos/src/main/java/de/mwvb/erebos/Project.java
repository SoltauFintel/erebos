/* Lizenz: Apache 2.0 */
package de.mwvb.erebos;

import java.io.File;

public class Project {
	private File dir;
	private boolean insideWorkspaceFolder = true;
	private boolean git = false;
	private File gitDir;
	private String remote = null;
	
	public File getDir() {
		return dir;
	}
	
	public boolean folderExists() {
		return dir != null && dir.isDirectory();
	}
	
	public void setDir(File dir) {
		this.dir = dir;
	}

	public boolean isGit() {
		return git;
	}

	public void setGit(boolean git) {
		this.git = git;
	}

	public File getGitDir() {
		return gitDir;
	}

	public void setGitDir(File gitDir) {
		this.gitDir = gitDir;
	}

	public String getRemote() {
		return remote;
	}

	public void setRemote(String remote) {
		this.remote = remote;
	}

	public boolean isInsideWorkspaceFolder() {
		return insideWorkspaceFolder;
	}

	public void setInsideWorkspaceFolder(boolean insideWorkspaceFolder) {
		this.insideWorkspaceFolder = insideWorkspaceFolder;
	}
}
