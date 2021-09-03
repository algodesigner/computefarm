package org.tiling.computefarm.impl.javaspaces.worker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

abstract class WorkerSupport implements Worker {

	private WorkerState state = WorkerState.INITIALIZING;
	private final List listeners = Collections.synchronizedList(new ArrayList());
	
	public synchronized void addWorkerListener(WorkerListener workerListener) {
		listeners.add(workerListener);
	}

	public synchronized void removeWorkerListener(WorkerListener workerListener) {
		listeners.remove(workerListener);
	}
	
	public WorkerState getState() {
		return state;
	}

	private synchronized List getWorkerListeners() {
		List copy = new ArrayList();
		copy.addAll(listeners);
		return copy;
	}
	
	protected void fireStateChanged(WorkerEventType type) {
		WorkerEvent event = new WorkerEvent(this, type);
		if (event.getType() == WorkerEventType.COMPUTATION_FINISHED) {
			state = WorkerState.WAITING_FOR_TASK;
		} else if (event.getType() == WorkerEventType.COMPUTATION_STARTED) {
			state = WorkerState.COMPUTING;
		} else if (event.getType() == WorkerEventType.CONNECTED) {
			state = WorkerState.WAITING_FOR_TASK;
		} else if (event.getType() == WorkerEventType.DISCONNECTED) {
			state = WorkerState.CONNECTING;
		} else if (event.getType() == WorkerEventType.TASK_RECEIVED) {
			state = WorkerState.COMPUTING;
		} else if (event.getType() == WorkerEventType.WORKER_STARTED) {
			state = WorkerState.CONNECTING;
		}
		List listeners = getWorkerListeners();
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			WorkerListener listener = (WorkerListener) i.next();
			listener.stateChanged(event);
		}
	}	
	
    public abstract void discard();
	
	
}
