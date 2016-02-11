package de.mwvb.erebos.eclipse;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.mwvb.erebos.Project;
import de.mwvb.erebos.WorkspaceException;

/**
 * <p>Zweck 1: Eclipse Workspace Ordner finden.
 * 
 * <p>Zweck 2: In Workspace Ordner die Projekte analysieren.
 * Wo liegt das Projektverzeichnis? Wird Git verwendet?
 * Wird ein Remote-Git-Repository verwendet?
 * 
 * <p>Zweck 3: Dateien zählen
 * 
 * @author Marcus Warm
 */
public class Workspace {
	boolean modus;
	
	/**
	 * @param depth 0 = nur schauen, ob dirs[i] einen .metadata Ordner hat;
	 * 1 = das auch für die Subordner von dirs[i] machen;
	 * usw.
	 */
	public List<File> searchWorkspaces(List<String> dirs, int depth) {
		if (depth > 10) throw new IllegalArgumentException("depth: 0 .. 10");
		List<File> workspaces = new ArrayList<>();
		for (String dir : dirs) {
			searchWorkspaces(new File(dir), workspaces, depth);
		}
		return workspaces;
	}
	
	private void searchWorkspaces(File dir, List<File> workspaces, int depth) {
		if (depth < 0 || !dir.isDirectory() || dir.getName().startsWith(".")) return;
		if (new File(dir, ".metadata").isDirectory()) {
			// dir ist ein Workspace
			workspaces.add(dir);
			return;
		}
		if (depth == 0) return;
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				searchWorkspaces(f, workspaces, depth - 1);
			}
		}
	}

	/**
	 * @return Projekte des Eclipse Workspaces
	 */
	public List<Project> getProjects(String workspaceDir) {
		final File fw = new File(workspaceDir);
		final String pfad = fw.getAbsolutePath();
		if (!fw.isDirectory()) {
			throw new WorkspaceException("Workspace-Verzeichnis '" + pfad + "' ist nicht vorhanden!");
		}
		final File fm = new File(fw, ".metadata");
		if (!fm.isDirectory()) {
			throw new WorkspaceException("Ist kein Eclipse Workspace: " + pfad + "\n.metadata Verzeichnis nicht vorhanden");
		}
		final File fp = new File(fm, ".plugins/org.eclipse.core.resources/.projects");
		List<Project> projects = new ArrayList<>();
		if (!fp.isDirectory()) {
			return projects; // leerer Workspace
		}
		
		for (File f : fp.listFiles()) {
			if (f.isDirectory()) {
				if ("RemoteSystemsTempFiles".equalsIgnoreCase(f.getName())
						|| "Servers".equalsIgnoreCase(f.getName())
						|| f.getName().startsWith(".")) continue;
				
				projects.add(createProject(f, fw));
			}
		}
		return projects;
	}
	
	private Project createProject(File f, File fw) {
		Project p = new Project();
		File loc = new File(f, ".location");
		if (loc.exists()) {
			p.setDir(readLocation(loc));
			if (p.getDir() != null) {
				checkForGit(p);
			}
		}
		if (p.getDir() == null) {
			p.setDir(new File(fw, f.getName()));
		}
		String pdir = p.getDir().getAbsolutePath().replace("\\", "/");
		String wsdir = fw.getAbsolutePath().replace("\\", "/");
		p.setInsideWorkspaceFolder(pdir.startsWith(wsdir));
		return p;
	}

	// inspiriert durch https://github.com/PaulKeeble/maven-eclipse-plugin/blob/master/src/main/java/org/apache/maven/plugin/eclipse/reader/ReadWorkspaceLocations.java
	private File readLocation(File location) {
		try {
			DataInputStream dataInputStream = new DataInputStream(new SafeChunkyInputStream(location));
			try {
				String file = dataInputStream.readUTF().trim();
				if (file.length() > 0) {
					if (!file.startsWith("URI//")) {
						throw new WorkspaceException("readLocation Fehler: " + location.getAbsolutePath() + " contains unexpected data: " + file);
					}
					file = file.substring("URI//".length());
					return new File(new URI(file));
				}
			} finally {
				dataInputStream.close();
			}
		} catch (IOException e) {
			throw new WorkspaceException("readLocation Fehler für " + location.getAbsolutePath(), e);
		} catch (URISyntaxException e) {
			throw new WorkspaceException("readLocation Fehler für " + location.getAbsolutePath(), e);
		}
		return null;
	}
	
	private void checkForGit(Project p) {
		File git = null;
		p.setGit(false);
		p.setGitDir(null);
		p.setRemote(null);

		File that = p.getDir();
		while (that != null) {
			git = new File(that, ".git");
			if (git.isDirectory()) {
				p.setGit(true);
				p.setGitDir(git);
				break;
			} else {
				that = that.getParentFile();
			}
		}
		
		if (p.isGit()) {
			try {
				readRemote(p, git);
			} catch (IOException e) {
				throw new WorkspaceException("Fehler beim Laden der Git-Config-Datei: " + git.getAbsolutePath());
			}
		}
	}

	private void readRemote(Project p, File git) throws IOException {
		Files.lines(new File(git, "config").toPath()).forEach(new Consumer<String>() { // nicht mit Lambda!
			@Override
			public void accept(String line) {
				if (p.getRemote() != null || line == null || line.trim().isEmpty()) return;
				line = line.trim();
				if (line.startsWith("[")) {
					modus = "[remote \"origin\"]".equals(line);
				} else {
					int o = line.indexOf("=");
					if (o >= 0) {
						String li = line.substring(0, o).trim();
						if (modus && li.equalsIgnoreCase("url")) {
							p.setRemote(shorten(line.substring(o + 1)));
						}
					}
				}
			}
			
			private String shorten(String remoteDir) {
				remoteDir = remoteDir.trim();
				if (remoteDir.startsWith("file:///")) {
					remoteDir = remoteDir.substring("file:///".length()).replace("\\\\", "\\");
				}
				return remoteDir;
			}
		});
	}
	
	public int countFiles(File dir, String ext) {
		int n = 0;
		if (dir.isDirectory()) {
			if ("bin".equals(dir.getName()) || "webapp".equals(dir.getName()) || "classes".equals(dir.getName())) return 0;
			for (File f : dir.listFiles()) {
				if (f.isDirectory()) {
					n += countFiles(f, ext);
				} else if (f.getName().toLowerCase().endsWith(ext.toLowerCase())) {
					n++;
				}
			}
		}
		return n;
	}
}
