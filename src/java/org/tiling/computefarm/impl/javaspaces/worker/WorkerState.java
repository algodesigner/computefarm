package org.tiling.computefarm.impl.javaspaces.worker;

public class WorkerState {
	private final String name;
	
	private WorkerState(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	public static final WorkerState NO_STATE = new WorkerState("none");
	public static final WorkerState INITIALIZING = new WorkerState("initializing");
	public static final WorkerState CONNECTING = new WorkerState("connecting");
	public static final WorkerState WAITING_FOR_TASK = new WorkerState("waiting");
	public static final WorkerState COMPUTING = new WorkerState("computing");
	
}
