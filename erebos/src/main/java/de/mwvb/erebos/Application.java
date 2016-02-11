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

public class Application {
	// TODO Command line: depth, Ausgabe Verzeichnis
	
	public static void main(String[] args) {
		System.out.println("Erebos Eclipse Workspace Finder 0.1");
		if (args.length != 1) {
			System.out.println("Bitte Pfad angeben in dem nach Eclipse Workspaces gesucht werden soll.");
			System.exit(-1);
		}
		new Application().start(args[0], 2);
	}

	public void start(String pfad, int depth) {
		if (!new File(pfad).isDirectory()) {
			System.out.println("Nicht vorhanden: " + pfad);
			System.exit(-1);
		}
		List<String> dirs = new ArrayList<>();
		dirs.add(pfad);
		List<File> workspaces = new Workspace().searchWorkspaces(dirs, depth);
		if (workspaces.size() == 1) {
			System.out.println("1 Workspace gefunden");
		} else {
			System.out.println(workspaces.size() + " Workspaces gefunden");
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>");
		sb.append(pfad);
		sb.append("</title><style>* { font-family: Arial; } th { background-color: #aaa; }</style></head><body><h1>Eclipse Workspaces</h1>");
		sb.append("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\"><tr><th>Project</th><th>Project dir</th>"
				+ "<th>Git dir</th><th>Git remote</th></tr>");
		Set<String> repos = new TreeSet<>();
		int np = 0;
		Map<String, Integer> map = new HashMap<>();
		for (File ws : workspaces) {
			List<Project> projects = new Workspace().getProjects(ws.getAbsolutePath());
			sb.append("<tr><td colspan=\"4\" style=\"background-color: #ccf;\"><b>" + ws.getAbsolutePath() + "</b> &nbsp;(" + projects.size() + " Projekt"
					+ (projects.size() == 1 ? "" : "e") + ")</td></tr>");
			for (Project p : projects) {
				sb.append("<tr><td style=\"padding-left: 0.5cm;\">" + p.getDir().getName() + "</td><td>" + p.getDir().getAbsolutePath() + "</td>");
				sb.append("<td>" + (p.getGitDir() != null ? p.getGitDir().getAbsolutePath() : "") + "</td>");
				String f = "";
				if (p.getRemote() != null && p.getRemote().contains("github")) {
					f = " style=\"background-color: #fc0;\"";
				}
				sb.append("<td" + f + ">" + (p.getRemote() != null ? p.getRemote() : "") + "</td>");
				sb.append("</tr>");
				if (p.getGitDir() != null) repos.add(p.getGitDir().getAbsolutePath());
				if (p.getRemote() != null) repos.add(p.getRemote());
				String key = p.getDir().getName();
				Integer i = map.get(key);
				if (i == null) {
					map.put(key, Integer.valueOf(1));
				} else {
					map.put(key, Integer.valueOf(i.intValue() + 1));
				}
			}
			np += projects.size();
		}
		sb.append("<tr><td colspan=\"4\" style=\"background-color: #aaa;\">Anzahl Workspaces: "
				+ workspaces.size() + ", Anzahl Projekte: " + np + "</td></tr>");
		sb.append("</table><h2>Git Repositories</h2><ul>");
		for (String repo : repos) {
			sb.append("<li>" + repo + "</li>");
		}
		sb.append("</ul>Anzahl: " + repos.size() + "</p>");
		sb.append("<p><h2>Mehrfach vorhandene Projekte</h2><ul>");
		for (Map.Entry<String, Integer> e : map.entrySet()) {
			if (e.getValue() > 1) {
				sb.append("<li>" + e.getValue() + "x - "+ e.getKey()+"</li>");
			}
		}
		sb.append("</ol></p>");
		sb.append("</body></html>");
		write(sb);
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
