/* Lizenz: Apache 2.0 */
package de.mwvb.erebos;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.mwvb.erebos.eclipse.Workspace;

/**
 * Erebos Startklasse, Java 8
 * 
 * @author Marcus Warm
 */
public class Application {
	private StringBuilder sb;
	private Set<String> repos;
	private int anzahlProjects;
	private Map<String, Integer> projectAnzahlMap;
	
	public static void main(String[] args) {
		System.out.println("Erebos Eclipse Workspace Finder 0.2");
		if (args.length != 1) {
			System.out.println("Bitte Pfad angeben in dem nach Eclipse Workspaces gesucht werden soll.");
			System.exit(-1);
		}
		new Application().start(args[0], 2);
	}

	public void start(String pfad, int depth) {
		init(pfad);
		List<File> workspaces = searchWorkspaces(pfad, depth);
		generateHTML(pfad, workspaces);
		write(sb);
	}

	private void init(String pfad) {
		if (!new File(pfad).isDirectory()) {
			System.out.println("Nicht vorhanden: " + pfad);
			System.exit(-1);
		}
		
		repos = new TreeSet<>();
		anzahlProjects = 0;
		projectAnzahlMap = new HashMap<>();
	}

	private List<File> searchWorkspaces(String pfad, int depth) {
		List<String> dirs = new ArrayList<>();
		dirs.add(pfad);
		List<File> workspaces = new Workspace().searchWorkspaces(dirs, depth);
		if (workspaces.size() == 1) {
			System.out.println("1 Workspace gefunden");
		} else {
			System.out.println(workspaces.size() + " Workspaces gefunden");
		}
		return workspaces;
	}

	private void generateHTML(String pfad, List<File> workspaces) {
		sb = new StringBuilder();
		sb.append("<html><head><title>Eclipse Workspaces - ");
		sb.append(pfad);
		sb.append("</title><style>" + css() + "</style></head><body>");
		generateWorkspaceHTML(workspaces);
		generateReposHTML();
		generateMultiProjectHTML();
		sb.append("\n<p><font size=\"1\">Erstellt mit <a href=\"https://github.com/SoltauFintel/erebos\">Erebos</a></font></p>");
		sb.append("\n</body>\n</html>\n");
	}

	private String css() {
		return "* { font-family: Arial; color: black; }"
				+ " th { background-color: #aaa; }"
				+ " a { color: black; }"
				+ " a:hover { color: blue; text-decoration: none; }";
	}

	private void generateWorkspaceHTML(List<File> workspaces) {
		sb.append("<h1>Eclipse Workspaces</h1>");
		sb.append("\n<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\"><tr><th>Project</th><th>Project dir</th>"
				+ "<th>Git dir</th><th>Git remote</th></tr>");
		sb.append("<ul>");
		for (File ws : workspaces) {
			sb.append("<li style=\"font-family: Courier New;\">" + ws.getAbsolutePath() + "</li>");
		}
		sb.append("</ul>");
		for (File ws : workspaces) {
			List<Project> projects = new Workspace().getProjects(ws.getAbsolutePath());
			sb.append("\n<tr><td colspan=\"4\" style=\"background-color: #ccf;\"><b>" + ws.getAbsolutePath() + "</b>"
					+ " &nbsp;(" + projects.size() + " Projekt"
					+ (projects.size() == 1 ? "" : "e") + ")</td></tr>");
			
			generateProjectHTML(projects);
			
			anzahlProjects += projects.size();
		}
		sb.append("\n<tr><td colspan=\"4\" style=\"background-color: #aaa;\">Anzahl Workspaces: "
				+ workspaces.size() + ", Anzahl Projekte: " + anzahlProjects + "</td></tr>");
		sb.append("</table>\n");
	}

	private void generateProjectHTML(List<Project> projects) {
		for (Project p : projects) {
			sb.append("\n<tr><td style=\"padding-left: 0.5cm;\">" + p.getDir().getName() + "</td><td>" + p.getDir().getAbsolutePath() + "</td>");
			sb.append("<td>" + (p.getGitDir() != null ? p.getGitDir().getAbsolutePath() : "") + "</td>");
			String f = "";
			if (p.getRemote() != null && p.getRemote().contains("github")) {
				f = " style=\"background-color: #fc0;\"";
			}
			sb.append("<td" + f + ">" + (p.getRemote() != null ? makeLink(p.getRemote()) : "") + "</td>");
			sb.append("</tr>");
			if (p.getGitDir() != null) repos.add(p.getGitDir().getAbsolutePath());
			if (p.getRemote() != null) repos.add(p.getRemote());
			String key = p.getDir().getName();
			Integer i = projectAnzahlMap.get(key);
			if (i == null) {
				projectAnzahlMap.put(key, Integer.valueOf(1));
			} else {
				projectAnzahlMap.put(key, Integer.valueOf(i.intValue() + 1));
			}
		}
	}
	
	private String makeLink(String url) {
		if (url.startsWith("https://")) {
			url = "<a href=\"" + url + "\">" + url + "</a>";
		}
		return url;
	}

	private void generateReposHTML() {
		sb.append("<h2>Git Repositories</h2><ul>");
		for (String repo : repos) {
			sb.append("<li>" + makeLink(repo) + "</li>");
		}
		sb.append("</ul>Anzahl: " + repos.size() + "</p>");
	}

	private void generateMultiProjectHTML() {
		sb.append("\n<p><h2>Mehrfach vorhandene Projekte</h2><ul>");
		for (Map.Entry<String, Integer> e : projectAnzahlMap.entrySet()) {
			if (e.getValue() > 1) {
				sb.append("\n<li>" + e.getValue() + "x - "+ e.getKey()+"</li>");
			}
		}
		sb.append("\n</ul></p>");
	}

	private void write(StringBuilder sb) {
		try {
			String dn = "workspaces.html";
			FileWriter fw = new FileWriter(dn);
			fw.write(sb.toString());
			fw.close();
			System.out.println(dn + " erzeugt");
			System.out.println();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
