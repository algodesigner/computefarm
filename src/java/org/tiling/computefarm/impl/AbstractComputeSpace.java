package org.tiling.computefarm.impl;

import org.tiling.computefarm.CancelledException;
import org.tiling.computefarm.ComputeSpace;

public abstract class AbstractComputeSpace implements ComputeSpace {

    private boolean cancelled;

    public synchronized void cancel() {
        cancelled = true;
    }

    protected synchronized void checkIfCancelled() throws CancelledException {
        if (cancelled) {
            throw new CancelledException();
        }
    }

}