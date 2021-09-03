package org.tiling.computefarm.impl.simple;

import java.util.LinkedList;

import org.tiling.computefarm.CancelledException;
import org.tiling.computefarm.Task;
import org.tiling.computefarm.impl.AbstractComputeSpace;
/**
 * A {@link ComputeSpace} implemented using an in-memory queue.
 */
class SimpleComputeSpace extends AbstractComputeSpace {
    
    private final LinkedList entries = new LinkedList();

    public synchronized void write(Task task) throws CancelledException {
    	checkIfCancelled();
        entries.addLast(task);
        notifyAll();
    }

	public Object take() throws CancelledException {
	    checkIfCancelled();
        Task task;
        synchronized(this) {
            while(entries.isEmpty()) {
                try { wait(); } catch (InterruptedException e) { }
              	checkIfCancelled();
            }
            task = (Task) entries.removeFirst();
        }
        return task.execute();
    }

}
