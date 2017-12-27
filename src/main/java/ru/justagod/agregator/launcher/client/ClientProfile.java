package ru.justagod.agregator.launcher.client;

import ru.justagod.agregator.launcher.hasher.FileNameMatcher;
import ru.justagod.agregator.launcher.helper.IOHelper;
import ru.justagod.agregator.launcher.helper.VerifyHelper;
import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.config.ConfigObject;
import ru.justagod.agregator.launcher.serialize.config.entry.*;
import ru.justagod.agregator.launcher.serialize.stream.StreamObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ComparableImplementedButEqualsNotOverridden")
public final class ClientProfile extends ConfigObject implements Comparable<ClientProfile> {
    public static final StreamObject.Adapter<ClientProfile> RO_ADAPTER = input -> new ClientProfile(input, true);
    private static final FileNameMatcher ASSET_MATCHER = new FileNameMatcher(
            new String[0], new String[]{"indexes", "objects"}, new String[0]);

    // Version
    private final StringConfigEntry version;
    private final StringConfigEntry assetIndex;

    // Client
    private final IntegerConfigEntry sortIndex;
    private final StringConfigEntry title;
    private final StringConfigEntry serverAddress;
    private final IntegerConfigEntry serverPort;

    //  Updater and launcher.client watch service
    private final ListConfigEntry update;
    private final ListConfigEntry updateExclusions;
    private final ListConfigEntry updateVerify;

    // Client launcher
    private final StringConfigEntry mainClass;
    private final ListConfigEntry jvmArgs;
    private final ListConfigEntry classPath;
    private final ListConfigEntry clientArgs;

    public ClientProfile(BlockConfigEntry block) {
        super(block);

        // Version
        version = block.getEntry("version", StringConfigEntry.class);
        assetIndex = block.getEntry("assetIndex", StringConfigEntry.class);

        // Client
        sortIndex = block.getEntry("sortIndex", IntegerConfigEntry.class);
        title = block.getEntry("title", StringConfigEntry.class);
        serverAddress = block.getEntry("serverAddress", StringConfigEntry.class);
        serverPort = block.getEntry("serverPort", IntegerConfigEntry.class);

        //  Updater and launcher.client watch service
        update = block.getEntry("update", ListConfigEntry.class);
        updateVerify = block.getEntry("updateVerify", ListConfigEntry.class);
        updateExclusions = block.getEntry("updateExclusions", ListConfigEntry.class);

        // Client launcher
        mainClass = block.getEntry("mainClass", StringConfigEntry.class);
        classPath = block.getEntry("classPath", ListConfigEntry.class);
        jvmArgs = block.getEntry("jvmArgs", ListConfigEntry.class);
        clientArgs = block.getEntry("clientArgs", ListConfigEntry.class);
    }

    public ClientProfile(HInput input, boolean ro) throws IOException {
        this(new BlockConfigEntry(input, ro));
    }

    public String getAssetIndex() {
        return assetIndex.getValue();
    }

    public FileNameMatcher getAssetUpdateMatcher() {
        return getVersion().compareTo(Version.MC1710) >= 0 ? ASSET_MATCHER : null;
    }

    public String[] getClassPath() {
        return classPath.stream(StringConfigEntry.class).toArray(String[]::new);
    }

    public String[] getClientArgs() {
        return clientArgs.stream(StringConfigEntry.class).toArray(String[]::new);
    }

    public String[] getJvmArgs() {
        return jvmArgs.stream(StringConfigEntry.class).toArray(String[]::new);
    }

    public String getMainClass() {
        return mainClass.getValue();
    }

    public String getServerAddress() {
        return serverAddress.getValue();
    }

    public int getServerPort() {
        return serverPort.getValue();
    }

    public InetSocketAddress getServerSocketAddress() {
        return InetSocketAddress.createUnresolved(getServerAddress(), getServerPort());
    }

    public int getSortIndex() {
        return sortIndex.getValue();
    }

    public String getTitle() {
        return title.getValue();
    }

    public void setTitle(String title) {
        this.title.setValue(title);
    }

    public FileNameMatcher getClientUpdateMatcher() {
        String[] updateArray = update.stream(StringConfigEntry.class).toArray(String[]::new);
        String[] verifyArray = updateVerify.stream(StringConfigEntry.class).toArray(String[]::new);
        String[] exclusionsArray = updateExclusions.stream(StringConfigEntry.class).toArray(String[]::new);
        return new FileNameMatcher(updateArray, verifyArray, exclusionsArray);
    }

    public Version getVersion() {
        return Version.byName(version.getValue());
    }

    public void setVersion(Version version) {
        this.version.setValue(version.name);
    }

    public void verify() {
        // Version
        getVersion();
        IOHelper.verifyFileName(getAssetIndex());

        // Client
        VerifyHelper.verify(getTitle(), VerifyHelper.NOT_EMPTY, "Profile title can't be empty");
        VerifyHelper.verify(getServerAddress(), VerifyHelper.NOT_EMPTY, "Server address can't be empty");
        VerifyHelper.verifyInt(getServerPort(), VerifyHelper.range(0, 65535), "Illegal server port: " + getServerPort());

        //  Updater and launcher.client watch service
        update.verifyOfType(ConfigEntry.Type.STRING);
        updateVerify.verifyOfType(ConfigEntry.Type.STRING);
        updateExclusions.verifyOfType(ConfigEntry.Type.STRING);

        // Client launcher
        jvmArgs.verifyOfType(ConfigEntry.Type.STRING);
        classPath.verifyOfType(ConfigEntry.Type.STRING);
        clientArgs.verifyOfType(ConfigEntry.Type.STRING);
        VerifyHelper.verify(getTitle(), VerifyHelper.NOT_EMPTY, "Main class can't be empty");
    }

    @Override
    public int compareTo(ClientProfile o) {
        return Integer.compare(getSortIndex(), o.getSortIndex());
    }

    @Override
    public String toString() {
        return title.getValue();
    }

    public enum Version {
        MC152("1.5.2", 61), MC164("1.6.4", 78), MC172("1.7.2", 4), MC1710("1.7.10", 5), MC188("1.8.8", 47), MC189("1.8.9", 47),;
        private static final Map<String, Version> VERSIONS;

        static {
            Version[] versionsValues = values();
            VERSIONS = new HashMap<>(versionsValues.length);
            for (Version version : versionsValues) {
                VERSIONS.put(version.name, version);
            }
        }

        public final String name;
        public final int protocol;

        Version(String name, int protocol) {
            this.name = name;
            this.protocol = protocol;
        }

        public static Version byName(String name) {
            return VerifyHelper.getMapValue(VERSIONS, name, String.format("Unknown launcher.client version: '%s'", name));
        }

        @Override
        public String toString() {
            return "Minecraft " + name;
        }
    }
}
