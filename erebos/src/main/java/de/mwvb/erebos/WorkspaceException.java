package de.mwvb.erebos;

public class WorkspaceException extends RuntimeException {
	private static final long serialVersionUID = -1954554627389168414L;

	public WorkspaceException(String msg) {
		super(msg);
	}

	public WorkspaceException(String msg, Throwable t) {
		super(msg, t);
	}
}
