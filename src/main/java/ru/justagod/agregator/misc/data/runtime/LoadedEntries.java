package ru.justagod.agregator.misc.data.runtime;

import ru.justagod.agregator.helper.OverlayHelper;
import ru.justagod.agregator.misc.data.Data;
import ru.justagod.agregator.misc.data.DataEntry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class LoadedEntries {

    private static final Map<String, LoadedDataEntry> DATA_ENTRIES = new HashMap<>();
    private static LoadedDataEntry currentEntry = null;

    public static void loadAll() {
        DataEntry[] entries = Data.getInstance().getEntries();
        for (DataEntry entry : entries) {
            if (entry != null) {
                try {
                    load(entry);
                } catch (Exception e) {
                    System.err.println("Can't load entry: " + entry.toString());
                    e.printStackTrace();
                }
            }
        }
        try {
            setCurrentEntry(Data.getInstance().getLastEntry());
        } catch (Exception e) {
            System.err.println("Couldn't restore last entry");
        }
    }

    public static void load(DataEntry entry) throws Exception {
        DATA_ENTRIES.put(entry.getLauncherName(), new LoadedDataEntry(entry));
    }

    public static LoadedDataEntry getCurrentEntry() {
        return currentEntry;
    }

    public static void setCurrentEntry(String name) throws IOException {
        LoadedDataEntry entry = DATA_ENTRIES.get(name);
        if (entry == null) {
            throw new NoSuchEntryException(name);
        }
        currentEntry = entry;
        OverlayHelper.showProgressOverlay("Загрузка информации о серверах", null);
        currentEntry.onChosen();
    }

    public static Map<String, LoadedDataEntry> getDataEntries() {
        return DATA_ENTRIES;
    }
}
