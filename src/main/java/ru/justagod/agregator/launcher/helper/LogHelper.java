package ru.justagod.agregator.launcher.helper;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiOutputStream;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LogHelper {
    public static final String DEBUG_PROPERTY = "launcher.debug";
    public static final boolean JANSI;

    // Output settings
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss", Locale.US);
    private static final AtomicBoolean DEBUG_ENABLED = new AtomicBoolean(Boolean.getBoolean(DEBUG_PROPERTY));
    private static final Set<Output> OUTPUTS = Collections.newSetFromMap(new ConcurrentHashMap<>(2));
    private static final Output STD_OUTPUT;

    static {
        // Use JAnsi if available
        boolean jansi;
        try {
            Class.forName("org.fusesource.jansi.Ansi");
            AnsiConsole.systemInstall();
            jansi = true;
        } catch (ClassNotFoundException ignored) {
            jansi = false;
        }
        JANSI = jansi;

        // Add std writer
        STD_OUTPUT = System.out::println;
        addOutput(STD_OUTPUT);

        // Add file log writer
        String logFile = System.getProperty("launcher.logFile");
        if (logFile != null) {
            try {
                addOutput(IOHelper.toPath(logFile));
            } catch (IOException e) {
                error(e);
            }
        }
    }

    private LogHelper() {
    }

    public static void addOutput(Output output) {
        OUTPUTS.add(Objects.requireNonNull(output, "output"));
    }

    public static void addOutput(Path file) throws IOException {
        if (JANSI) {
            addOutput(new JAnsiOutput(IOHelper.newOutput(file, true)));
        } else {
            addOutput(IOHelper.newWriter(file, true));
        }
    }

    public static void addOutput(Writer writer) throws IOException {
        addOutput(new WriterOutput(writer));
    }

    public static void debug(String message) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, message, false);
        }
    }

    public static void debug(String format, Object... args) {
        debug(String.format(format, args));
    }

    public static void error(Throwable exc) {
        error(isDebugEnabled() ? toString(exc) : exc.toString());
    }

    public static void error(String message) {
        log(Level.ERROR, message, false);
    }

    public static void error(String format, Object... args) {
        error(String.format(format, args));
    }

    public static void info(String message) {
        log(Level.INFO, message, false);
    }

    public static void info(String format, Object... args) {
        info(String.format(format, args));
    }

    public static boolean isDebugEnabled() {
        return DEBUG_ENABLED.get();
    }

    public static void setDebugEnabled(boolean debugEnabled) {
        DEBUG_ENABLED.set(debugEnabled);
    }

    public static void log(Level level, String message, boolean sub) {
        System.out.println(message);
        String dateTime = DATE_TIME_FORMATTER.format(LocalDateTime.now());
        println(JANSI ? ansiFormatLog(level, dateTime, message, sub) :
                formatLog(level, message, dateTime, sub));
    }



    public static synchronized void println(String message) {
        for (Output output : OUTPUTS) {
            output.println(message);
        }
    }

    public static boolean removeOutput(Output output) {
        return OUTPUTS.remove(output);
    }

    public static boolean removeStdOutput() {
        return removeOutput(STD_OUTPUT);
    }

    public static void subDebug(String message) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, message, true);
        }
    }

    public static void subDebug(String format, Object... args) {
        subDebug(String.format(format, args));
    }

    public static void subInfo(String message) {
        log(Level.INFO, message, true);
    }

    public static void subInfo(String format, Object... args) {
        subInfo(String.format(format, args));
    }

    public static void subWarning(String message) {
        log(Level.WARNING, message, true);
    }

    public static void subWarning(String format, Object... args) {
        subWarning(String.format(format, args));
    }

    public static String toString(Throwable exc) {
        try (StringWriter sw = new StringWriter(IOHelper.BUFFER_SIZE)) {
            try (PrintWriter pw = new PrintWriter(sw)) {
                exc.printStackTrace(pw);
            }
            return sw.toString();
        } catch (IOException e) {
            throw new InternalError(e);
        }
    }

    public static void warning(String message) {
        log(Level.WARNING, message, false);
    }

    public static void warning(String format, Object... args) {
        warning(String.format(format, args));
    }

    private static String ansiFormatLog(Level level, String dateTime, String message, boolean sub) {
        Ansi.Color levelColor;
        boolean bright = level != Level.DEBUG;
        switch (level) {
            case WARNING:
                levelColor = Ansi.Color.YELLOW;
                break;
            case ERROR:
                levelColor = Ansi.Color.RED;
                break;
            default: // INFO, DEBUG, Unknown
                levelColor = Ansi.Color.WHITE;
                break;
        }

        // Date-time
        Ansi ansi = new Ansi(IOHelper.BUFFER_SIZE);
        ansi.fg(Ansi.Color.WHITE).a(dateTime);

        // Level
        ansi.fg(Ansi.Color.WHITE).a(" [").bold();
        if (bright) {
            ansi.fg(levelColor);
        } else {
            ansi.fg(levelColor);
        }
        ansi.a(level).boldOff().fg(Ansi.Color.WHITE).a("] ");

        // Message
        if (bright) {
            ansi.fg(levelColor);
        } else {
            ansi.fg(levelColor);
        }
        if (sub) {
            ansi.a(' ').a(Ansi.Attribute.ITALIC);
        }
        ansi.a(message);

        // Finish with reset code
        return ansi.reset().toString();
    }

    private static String ansiFormatVersion(String product) {
        return new Ansi(IOHelper.BUFFER_SIZE).bold(). // Setup
                fg(Ansi.Color.MAGENTA).a("sashok724's "). // sashok724's
                fg(Ansi.Color.CYAN).a(product). // Product
                reset().toString(); // To string
    }

    private static String formatLog(Level level, String message, String dateTime, boolean sub) {
        if (sub) {
            message = ' ' + message;
        }
        return dateTime + " [" + level.name + "] " + message;
    }


    public enum Level {
        DEBUG("DEBUG"), INFO("INFO"), WARNING("WARN"), ERROR("ERROR");
        public final String name;

        Level(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @FunctionalInterface
    public interface Output {
        void println(String message);
    }

    private static class WriterOutput implements Output, AutoCloseable {
        private final Writer writer;

        private WriterOutput(Writer writer) throws IOException {
            this.writer = writer;
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }

        @Override
        public void println(String message) {
            try {
                writer.write(message + System.lineSeparator());
                writer.flush();
            } catch (IOException ignored) {
                // Do nothing?
            }
        }
    }

    private static final class JAnsiOutput extends WriterOutput {
        private JAnsiOutput(OutputStream output) throws IOException {
            super(IOHelper.newWriter(new AnsiOutputStream(output)));
        }
    }
}
