package ru.justagod.agregator.misc.data;

import ru.justagod.agregator.helper.IOHelper;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.CRC32;

public class DataEntry {
    public static final String CONFIG = "config.bin";
    public static final String ICON = "icon.png";

    private CRC32 checksum;
    private String launcherName;

    public DataEntry(CRC32 checksum, String launcherName) throws IOException {
        this.checksum = checksum;
        this.launcherName = launcherName;
        try {
            Files.createDirectory(Paths.get(IOHelper.WORKING_DIR + "/" + launcherName));
        } catch (FileAlreadyExistsException ignored) {

        }
    }

    @Override
    public String toString() {
        return launcherName;
    }

    public CRC32 getChecksum() {
        return checksum;
    }

    public void setChecksum(CRC32 checksum) {
        this.checksum = checksum;
    }

    public String getLauncherName() {
        return launcherName;
    }

    public void setLauncherName(String launcherName) {
        this.launcherName = launcherName;
    }

    public String getDirectory() {
        return IOHelper.WORKING_DIR + '/' + launcherName + '/';
    }

    public void writeFile(byte[] buffer, String name) throws IOException {
        File file = new File(getDirectory() + name);
        if (!file.exists()) file.createNewFile();
        OutputStream output = new FileOutputStream(file);
        output.write(buffer);
        output.flush();
        output.close();

    }

    public void writeConfig(byte[] buffer) throws IOException {
        writeFile(buffer, CONFIG);
    }

    public void writeIcon(byte[] buffer) throws IOException {
        writeFile(buffer, ICON);
    }

    public Path getConfig() {
        return Paths.get(getDirectory() + CONFIG);
    }

    public Path getIcon() {
        return Paths.get(getDirectory() + ICON);
    }
}
