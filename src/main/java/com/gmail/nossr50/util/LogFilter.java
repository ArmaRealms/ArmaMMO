package com.gmail.nossr50.util;

import com.gmail.nossr50.mcMMO;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

import static com.gmail.nossr50.util.LogUtils.DEBUG_STR;

public class LogFilter implements Filter {
    private final boolean debug;

    public LogFilter(final mcMMO plugin) {
        // Doing a config loading lite here, because we can't depend on the config loader to have loaded before any debug messages are sent
        debug = plugin.getConfig().getBoolean("General.Verbose_Logging");
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        final String message = record.getMessage();
        // Debug messages carry the marker as a prefix; a mid-string match is a normal message
        return message == null || debug || !message.startsWith(DEBUG_STR);
    }
}
