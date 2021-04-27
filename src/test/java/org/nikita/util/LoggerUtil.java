package org.nikita.util;

import java.io.IOException;
import java.sql.Date;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtil {
    public Logger initFileLogger(String outputPath, String name) throws IOException {
        Logger logger = Logger.getLogger(name);

        FileHandler handler = new FileHandler(outputPath);
        handler.setFormatter(new LoggerFormatter());
        logger.addHandler(handler);

        return logger;
    }

    public void logRunningTest(Logger logger, String testName) {
        logger.info(String.format(">>> Running test: [%s]", testName));
    }

    private class LoggerFormatter extends SimpleFormatter {
        private static final String format = "[%1$tF %1$tT] [%2$s] %3$s %n";

        @Override
        public synchronized String format(LogRecord log) {
            return String.format(format,
                new Date(log.getMillis()),
                log.getLevel().getLocalizedName(),
                log.getMessage()
            );
        }
    }
}
