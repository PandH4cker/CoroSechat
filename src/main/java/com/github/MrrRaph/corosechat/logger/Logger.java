package com.github.MrrRaph.corosechat.logger;

import com.github.MrrRaph.corosechat.logger.level.Level;

/**
 * <h1>The logger interface</h1>
 * <p>
 *     Define a log function to be implemented.
 * </p>
 * //TODO Include diagram of Logger
 *
 * @author Raphael Dray
 * @version 0.0.1
 * @since 0.0.2
 * @see Level
 */
public interface Logger {
    /**
     * Logs a message
     * @param message The message of the log
     * @param level The level of the message
     * @see Level
     */
    void log(String message, Level level);
}