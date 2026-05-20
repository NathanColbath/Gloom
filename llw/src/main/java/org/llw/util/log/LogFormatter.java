package org.llw.util.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Formats {@link LogRecord} instances as single- or multi-line text.
 */
public final class LogFormatter {
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    private LogFormatter() {
    }

    public static String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(TIMESTAMP.format(record.timestamp()))
                .append(" [")
                .append(record.level().name())
                .append("] [")
                .append(record.loggerName())
                .append("] ")
                .append(record.message());
        if (record.throwable() != null) {
            sb.append(System.lineSeparator());
            sb.append(stackTrace(record.throwable()));
        }
        return sb.toString();
    }

    private static String stackTrace(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString().stripTrailing();
    }
}
