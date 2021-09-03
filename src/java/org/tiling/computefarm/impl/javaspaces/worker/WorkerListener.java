package org.tiling.computefarm.impl.javaspaces.worker;

import java.util.EventListener;

public interface WorkerListener extends EventListener {

	public void stateChanged(WorkerEvent event);
	
}
