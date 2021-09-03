package org.tiling.computefarm.impl.javaspaces.util;

import java.rmi.RemoteException;

import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;

/**
 * A utility class for finding Jini service objects.
 */
public class ServiceFinder {
    private ServiceFinder() {}
    
	public static Object findService(ServiceRegistrar registrar, Class serviceClass) throws RemoteException {
		ServiceTemplate template = new ServiceTemplate(null, new Class[] { serviceClass }, null);
		return registrar.lookup(template);
	}	
}
