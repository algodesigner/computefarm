package org.tiling.computefarm.impl.javaspaces.worker;

public interface Worker {
	public void addWorkerListener(WorkerListener workerListener);
	public void removeWorkerListener(WorkerListener workerListener);
	public WorkerState getState();
}
