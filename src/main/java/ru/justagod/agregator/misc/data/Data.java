package ru.justagod.agregator.misc.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.sun.javafx.collections.ImmutableObservableList;
import ru.justagod.agregator.Application;
import ru.justagod.agregator.helper.IOHelper;
import ru.justagod.agregator.misc.data.runtime.LoadedEntries;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;

public final class Data {

    private static Data instance;

    private String savedLogin = "";
    private String savedPassword = "";
    private String lastEntry = "";
    private List<DataEntry> entries = new LinkedList<>();

    private Data() {
    }

    public DataEntry[] getEntries() {
        Object[] arr = entries.toArray();
        DataEntry[] tmp = new DataEntry[arr.length];

        System.arraycopy(arr, 0, tmp, 0, arr.length);

        return tmp;
    }

    private static void init() {
        try {
            String pathName = IOHelper.WORKING_DIR + "/data.json";

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileReader reader = new FileReader(pathName);
            Data data = gson.fromJson(reader, Data.class);
            reader.close();
            instance = data;
        } catch (Throwable e) {
            e.printStackTrace();
            instance = new Data();
        }
    }

    public static Data getInstance() {
        if (instance == null) init();
        return instance;
    }

    public boolean hasSameName(String name) {
        for (DataEntry entry : entries) {
            if (entry.getLauncherName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSameCRC(CRC32 sum) {
        for (DataEntry entry : entries) {
            if (entry.getChecksum().getValue() == sum.getValue()) {
                return true;
            }
        }
        return false;
    }

    public void addLauncher(byte[] configs, byte[] icon, String name) throws Exception {
        CRC32 sum = new CRC32();
        sum.update(configs);

        if (hasSameName(name)) throw new SameNameException(name);
        if (hasSameCRC(sum)) throw new SameSumException(name + ' ' + sum.getValue());

        DataEntry entry = new DataEntry(sum, name);
        entries.add(entry);
        entry.writeConfig(configs);

        if (icon != null) {
            entry.writeIcon(icon);
        }
        LoadedEntries.load(entry);
        write();
        Application.getInstance().showServers();
    }

    public String getSavedLogin() {
        return savedLogin;
    }

    public void setSavedLogin(String savedLogin) {
        this.savedLogin = savedLogin;
    }

    public String getSavedPassword() {
        return savedPassword;
    }

    public void setSavedPassword(String savedPassword) {
        this.savedPassword = savedPassword;
    }

    public String getLastEntry() {
        return lastEntry;
    }

    public void setLastEntry(String lastEntry) {
        this.lastEntry = lastEntry;
    }

    public void write() throws IOException {
        String pathName = IOHelper.WORKING_DIR + "/data.json";

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        File file = new File(pathName);
        if (!file.exists()) file.createNewFile();
        FileWriter writer = new FileWriter(file);
        gson.toJson(gson.toJsonTree(this, this.getClass()), writer);
        writer.flush();
        writer.close();
    }
}
