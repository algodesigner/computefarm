package org.tiling.computefarm.impl.javaspaces.worker;


public class WorkerEventType {

	private final String name;
	
	private WorkerEventType(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	public static final WorkerEventType WORKER_STARTED = new WorkerEventType("workerStarted");
	public static final WorkerEventType CONNECTED = new WorkerEventType("connected");
	public static final WorkerEventType DISCONNECTED = new WorkerEventType("disconnected");
	public static final WorkerEventType TASK_RECEIVED = new WorkerEventType("taskReceived");
	public static final WorkerEventType COMPUTATION_STARTED = new WorkerEventType("computationStarted");
	public static final WorkerEventType COMPUTATION_FINISHED = new WorkerEventType("computationFinished");
	public static final WorkerEventType WORKER_FINISHED = new WorkerEventType("workerFinished");
	
}
