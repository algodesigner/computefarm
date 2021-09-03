package org.tiling.computefarm.impl.javaspaces.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.logging.Logger;

/**
 * A {@link ClassServer} is a very simple HTTP server for serving class files or jars from
 * the file system.
 */
public class ClassServer extends AbstractServer {
	private static final Logger logger = Logger.getLogger(ClassServer.class.getName());
	
	private static final String RMI_SERVER_CODEBASE_TEMPLATE_PROPERTY_NAME = "org.tiling.computefarm.rmi.server.codebase.template";

	public static final File DEFAULT_ROOT = new File("./");
	private final File rootDirectory;
	
	public ClassServer() throws IOException {
		this(DEFAULT_ROOT, DEFAULT_START_PORT_NUMBER);
	}
	
	public ClassServer(File root, int startPort) throws IOException {
		this(root, startPort, true);
	}
	
	public ClassServer(File root, int startPort, boolean daemon) throws IOException {
	    super(startPort, daemon, System.getProperty(RMI_SERVER_CODEBASE_TEMPLATE_PROPERTY_NAME));
		rootDirectory = root.getCanonicalFile();
		if (!rootDirectory.isDirectory()) {
			throw new IOException("Specified root directory is not recognised: " + rootDirectory);
		}
	}
	
	protected void handleRequest(String method, String path, OutputStream out) throws IOException {
		if (method.equals("GET") || method.equals("HEAD")) { 
			File file = new File(rootDirectory, path).getCanonicalFile();
			if (!file.exists()) {
				sendError(out, "404", "Not Found");
				return;
			} else if (file.isDirectory()) {
				sendError(out, "403", "Forbidden");
				return; 
			} else {
				out.write("HTTP/1.0 200 OK\r\n".getBytes());
				out.write("Content-Type: application/java\r\n".getBytes());
				out.write(("Content-Length: " + file.length() + "\r\n").getBytes());
				out.write(("Last-modified: " + new Date(file.lastModified()).toString() + "\r\n").getBytes());
				out.write("\r\n".getBytes());
				if (method.equals("GET")) {
					writeFile(file, out);
				}
				out.flush();
				out.close();
			}
		} else {
			sendError(out, "501", "Not Implemented");
			return; 
		}

	}
	
	private void writeFile(File file, OutputStream out) throws IOException {
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			byte[] buf = new byte[4096];
			int bytesRead;
			while ((bytesRead = in.read(buf)) != -1) {
				out.write(buf, 0, bytesRead);
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}
	
    public static void main(String[] args) throws IOException {
		new ClassServer(DEFAULT_ROOT, 80, false).start();
	}
}
