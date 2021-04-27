package org.nikita.util;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class LoggerInconsistencyMatcher {
    private int matches;
    private int inconsistencies;
    private List<String> inconsistencyLogs;
    private Logger logger;

    public LoggerInconsistencyMatcher(Logger logger) {
        matches = 0;
        inconsistencies = 0;
        inconsistencyLogs = new LinkedList<>();
        this.logger = logger;
    }

    public void addMatch() {
        matches++;
    }

    public void addInconsistency(int row, String expected, String actual) {
        inconsistencyLogs.add(
            String.format(
                "Inconsistency at row %d. Expected: %s. Actual: %s",
                row,
                expected,
                actual
            )
        );
        inconsistencies++;
    }

    public void log() {
        logger.info(String.format(">>> Matches: %d", matches));
        logger.info(String.format("XXX Inconsistencies: %d", inconsistencies));

        if (inconsistencyLogs.size() > 0) {
            inconsistencyLogs.stream().forEach(log -> logger.severe(log));
        }
    }

    public int getMatches() {
        return matches;
    }

    public int getInconsistencies() {
        return inconsistencies;
    }
}
