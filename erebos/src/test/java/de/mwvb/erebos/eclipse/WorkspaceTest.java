package de.mwvb.erebos.eclipse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.mwvb.erebos.Project;
import de.mwvb.erebos.WorkspaceException;

// Entwickler-Tests
@Ignore
public class WorkspaceTest {

	@Test
	public void projectIstImWorkspace() {
		String dir = "C:\\_workspaces\\mars\\tools";
		List<Project> projects = new Workspace().getProjects(dir);
		Project x = null;
		for (Project p : projects) {
			if (p.getDir() != null && p.getDir().getName().equals("spark1")) {
				x = p;
			}
		}
		Assert.assertNotNull("Project spark1 nicht gefunden!", x);
		Assert.assertEquals(dir + "\\spark1", x.getDir().getAbsolutePath().replace("/", "\\"));
	}

	@Test
	public void gitProject() {
		String dir = "C:\\_workspaces\\mars\\tools";
		List<Project> projects = new Workspace().getProjects(dir);
		Project x = null;
		for (Project p : projects) {
			if (p.getDir() != null && p.getDir().getName().equals("data1a")) {
				x = p;
			}
		}
		Assert.assertNotNull("Project data1a nicht gefunden!", x);
		Assert.assertEquals("C:\\dat\\gitrepos2\\data1a-repo\\data1a", x.getDir().getAbsolutePath().replace("/", "\\"));
	}

	@Test(expected = WorkspaceException.class)
	public void keinWorkspace() {
		String dir = "C:\\_workspaces\\mars";
		new Workspace().getProjects(dir);
	}

	@Test(expected = WorkspaceException.class)
	public void dirNichtVorhanden() {
		String dir = "C:\\quatschmitsosse";
		new Workspace().getProjects(dir);
	}

	@Test
	public void searchWorkspaces() {
		List<String> dirs = new ArrayList<>();
		dirs.add("C:\\_workspaces");
		final List<File> result = new Workspace().searchWorkspaces(dirs, 2);
		int np = 0;
		if (result.isEmpty()) System.out.println("nichts gefunden");
		Set<String> repos = new TreeSet<>();
		for (File f : result) {
			System.out.println("Workspace: " + f.getAbsolutePath());
			final List<Project> projects = new Workspace().getProjects(f.getAbsolutePath());
			if (projects.isEmpty()) {
				System.out.println("\t// no projects");
			}
			for (Project p : projects) {
				if (p.getGitDir() != null) {
					repos.add(p.getGitDir().getAbsolutePath());
				}
				if (p.isGit() && p.getRemote() != null) {
					if (p.getRemote().contains("github")) {
						System.out.println("\tGithub Project: " + p.getDir().getName() + " => " + p.getRemote());
					} else {
						System.out.println("\tGit    Project: " + p.getDir().getName() + " => " + p.getRemote());
					}
					repos.add(p.getRemote() + "  [REMOTE]");
				} else if (p.isGit()) {
					System.out.println("\tGit    Project: " + p.getDir().getName());
				} else if (!p.isInsideWorkspaceFolder()) {
					System.out.println("\text.   Project: " + p.getDir().getName() + " => " + p.getDir().getAbsolutePath());
				} else {
					System.out.println("\t       Project: " + p.getDir().getName());
				}
				np++;
			}
			System.out.println();
		}
		System.out.println(result.size() + " Workspaces gefunden, insgesamt " + np + " Projekte gefunden");
		System.out.println("\n" + repos.size() + " Git Repos:");
		for (String gitDir : repos) {
			System.out.println(" - " + gitDir);
		}
	}
}
