package ru.justagod.agregator.misc;

import javafx.util.Callback;
import ru.justagod.agregator.helper.IOHelper;
import ru.justagod.agregator.misc.data.Data;
import ru.justagod.agregator.misc.data.DataException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.spec.InvalidKeySpecException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LauncherProcessor {

    public static final Pattern LAUNCHER_NAME_PATTERN;

    private static byte[] config = null;
    private static byte[] icon = null;


    static {
        LAUNCHER_NAME_PATTERN = Pattern.compile("[a-zA-Z]+");
    }

    public static void addLauncher(ZipFile file, String name, Callback<ProcessorResult, ?> onFinished) {
        ProcessorResult result = doAddLauncher(file, name);
        onFinished.call(result);

    }

    private static ProcessorResult doAddLauncher(ZipFile file, String name) {
        config = null;
        icon = null;

        if (!existsLauncher(file)) return ProcessorResult.ISNT_VALID;
        if (!existsName(name)) return ProcessorResult.ISNT_VALID;

        Path tmp = Paths.get(IOHelper.WORKING_DIR + "/tmp/");
        tmp.toFile().mkdir();
        Stream<? extends ZipEntry> zipInput = file.stream();
        zipInput.forEach((Consumer<ZipEntry>) zipEntry -> {
            if (zipEntry.getName().endsWith("config.bin")) {
                try {
                    InputStream input = file.getInputStream(zipEntry);
                    config = new byte[input.available()];
                    input.read(config);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (zipEntry.getName().endsWith(".ico")) {
                try {
                    InputStream input = file.getInputStream(zipEntry);
                    icon = new byte[input.available()];
                    input.read(icon);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });

        if (config == null) {
            System.err.println("Configs didnt find");
            return ProcessorResult.ISNT_VALID;
        }
        if (icon == null) {
            System.err.println("Icon didnt find");
        }

        try {
            Data.getInstance().addLauncher(config, icon, name);
        } catch (DataException e) {
            return ProcessorResult.ALREADY_ADDED;
        } catch (Exception e) {
            return ProcessorResult.ERROR;
        }
        return ProcessorResult.OK;
    }

    public static boolean existsName(String name) {
        Matcher matcher = LAUNCHER_NAME_PATTERN.matcher(name);
        return matcher.matches();
    }

    public static boolean existsLauncher(ZipFile file) {
        if (!file.getName().endsWith(".jar")) return false;

        Path tmp = Paths.get(IOHelper.WORKING_DIR + "/tmp/");
        tmp.toFile().mkdir();
        Stream<? extends ZipEntry> zipInput = file.stream();
        zipInput.forEach((Consumer<ZipEntry>) zipEntry -> {
            if (zipEntry.getName().endsWith("config.bin")) {
                try {
                    InputStream input = file.getInputStream(zipEntry);
                    config = new byte[input.available()];
                    input.read(config);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (zipEntry.getName().endsWith(".ico")) {
                try {
                    InputStream input = file.getInputStream(zipEntry);
                    icon = new byte[input.available()];
                    input.read(icon);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });

        if (config == null) {
            System.err.println("Configs didnt find");
            return false;
        }

        return true;
    }


    public enum ProcessorResult {
        OK("Launcher added"), NOT_INTEGRATED("Launcher added, but didn't integrate."), ALREADY_ADDED("File already added"), ISNT_VALID("File isn't valid, sorry"), ERROR("Error");

        public final String message;

        ProcessorResult(String message) {
            this.message = message;
        }


    }


}
