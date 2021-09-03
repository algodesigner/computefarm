package org.tiling.computefarm.impl.javaspaces.worker;

import java.util.EventObject;

public class WorkerEvent extends EventObject {

	private final WorkerEventType type;
	
	public WorkerEvent(Worker source, WorkerEventType type) {
		super(source);
		this.type = type;
	}
	
	public Worker getWorker() {
		return (Worker) source;
	}

	public WorkerEventType getType() {
		return type;
	}
}
