package org.tiling.computefarm.impl.javaspaces.worker;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.export.Exporter;

public class Config {
    private static String CONFIG_FILE = "jeri.config";

    private Config() {
    }

    public static Exporter getExporter(String name) throws ConfigurationException {
        Configuration config = ConfigurationProvider.getInstance(new String[] { CONFIG_FILE });
        return (Exporter) config.getEntry(name, "exporter", Exporter.class);
    }

}