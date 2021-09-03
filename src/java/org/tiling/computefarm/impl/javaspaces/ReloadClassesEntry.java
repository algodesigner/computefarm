package org.tiling.computefarm.impl.javaspaces;

import org.tiling.computefarm.impl.javaspaces.worker.Worker;

import net.jini.core.entry.Entry;

/**
 * An entry used to send a signal to {@link Worker}s
 * that they should reload their classes. 
 */
public class ReloadClassesEntry implements Entry {
}
