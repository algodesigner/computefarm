package org.tiling.computefarm;

/**
 * Indicates that there is a serious problem with the configuration.
 */
public class ConfigurationException extends RuntimeException {

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

}
