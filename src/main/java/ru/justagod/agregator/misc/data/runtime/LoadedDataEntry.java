package ru.justagod.agregator.misc.data.runtime;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import ru.justagod.agregator.Application;
import ru.justagod.agregator.helper.IOHelper;
import ru.justagod.agregator.helper.OverlayHelper;
import ru.justagod.agregator.launcher.Launcher;
import ru.justagod.agregator.launcher.client.ClientLauncher;
import ru.justagod.agregator.launcher.client.ClientProfile;
import ru.justagod.agregator.launcher.client.ServerPinger;
import ru.justagod.agregator.launcher.hasher.HashedDir;
import ru.justagod.agregator.launcher.helper.SecurityHelper;
import ru.justagod.agregator.launcher.request.auth.AuthRequest;
import ru.justagod.agregator.launcher.request.update.LauncherRequest;
import ru.justagod.agregator.launcher.request.update.UpdateRequest;
import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.config.entry.StringConfigEntry;
import ru.justagod.agregator.launcher.serialize.signed.SignedObjectHolder;
import ru.justagod.agregator.misc.data.DataEntry;
import ru.justagod.agregator.overlay.DownloadOverlay;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LoadedDataEntry {

    private DataEntry dataEntry;
    private Launcher.Config config;
    private Image icon;
    private List<SignedObjectHolder<ClientProfile>> profiles = new LinkedList<>();
    private Node currentServer = null;
    private SignedObjectHolder<ClientProfile> currentProfile = null;

    public LoadedDataEntry(DataEntry dataEntry) throws Exception {
        this.dataEntry = dataEntry;
        load();
    }

    private void load() throws Exception {

        {
            Path config = dataEntry.getConfig();
            if (!config.toFile().exists()) throw new NoSuchFileException("Couldn't find config file");
            HInput input = new HInput(new FileInputStream(config.toFile()));
            this.config = new Launcher.Config(input);
        }
        label:
        {
            Path icon = dataEntry.getIcon();
            if (!icon.toFile().exists()) {
                System.out.println("Couldn't find icon file");
                break label;
            }
            this.icon = new Image(new FileInputStream(icon.toFile()));
        }

        List<SignedObjectHolder<ClientProfile>> notLoadedProfiles = new LauncherRequest(config).request().profiles;
        profiles.addAll(notLoadedProfiles);
    }

    public void downloadAndLaunch(AuthRequest.Result authResult) {
        String jvmDirName = IOHelper.jvmDirName;
        Path jvmDir = Paths.get(IOHelper.WORKING_DIR + '/' + IOHelper.jvmDirName);
        AtomicReference<UpdateRequest> request = new AtomicReference<>(new UpdateRequest(config, jvmDirName, jvmDir, null));
        DownloadOverlay overlay = OverlayHelper.showDownloadOverlay("Хеширование", null);
        overlay.setCaption("Загрузка JVM");
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                UpdateRequest.State state = request.get().getState();

                if (state == null) return;

                double bps = state.getBps();
                Duration estimated = state.getEstimatedTime();
                long estimatedSeconds = estimated == null ? 0 : estimated.getSeconds();
                long estimatedHH = (estimatedSeconds / 3600);
                long estimatedMM = ((estimatedSeconds % 3600) / 60);
                long estimatedSS = (estimatedSeconds % 60);
                overlay.setDescription(String.format(
                        "Файл: %s%n" + // File line
                                "Загружено (Файл): %.2f / %.2f MiB.%n" + // File downloaded line
                                "Загружено (Всего): %.2f / %.2f MiB.%n" + // Total downloaded line
                                "%n" +
                                "Средняя скорость: %.1f Kbps%n" + // Speed line
                                "Примерно осталось: %d:%02d:%02d%n", // Estimated line

                        // Formatting
                        state.filePath, // File path
                        state.getFileDownloadedMiB(), state.getFileSizeMiB(), // File downloaded
                        state.getTotalDownloadedMiB(), state.getTotalSizeMiB(), // Total downloaded
                        bps <= 0.0 ? 0.0 : bps / 1024.0, // Speed
                        estimatedHH, estimatedMM, estimatedSS // Estimated (hh:mm:ss)
                ));
                double progress = state.getTotalDownloadedMiB() / state.getTotalSizeMiB();
                overlay.setProgress(progress);
            }
        };
        timer.start();

        AtomicReference<Thread> thread = new AtomicReference<>();
        Thread tmp = doDownload(request.get(), jvmHDir -> {
            String assetDirName = currentProfile.object.block.getEntryValue("assetDir", StringConfigEntry.class);
            Path assetDir = Paths.get(dataEntry.getDirectory() + '/' + currentProfile.object.block.getEntryValue("assetDir", StringConfigEntry.class));
            UpdateRequest tmpRequest = new UpdateRequest(config, assetDirName, assetDir, currentProfile.object.getAssetUpdateMatcher());
            request.set(tmpRequest);
            overlay.setCaption("Загрузка Assets");
            thread.set(doDownload(tmpRequest, assetHDir -> {
                overlay.setCaption("Загрузка Модов");
                String clientDirName = currentProfile.object.block.getEntryValue("dir", StringConfigEntry.class);
                Path clientDir = Paths.get(dataEntry.getDirectory() + '/' + currentProfile.object.getTitle());
                UpdateRequest tmpRequest1 = new UpdateRequest(config, clientDirName, clientDir, currentProfile.object.getClientUpdateMatcher());
                request.set(tmpRequest1);
                thread.set(doDownload(tmpRequest1, clientHDir -> {
                    OverlayHelper.hide(null);
                    try {
                        ClientLauncher.launch(jvmDir, jvmHDir, assetHDir, clientHDir, currentProfile, new ClientLauncher.Params(new byte[256],
                                assetDir, clientDir, authResult.pp, authResult.accessToken, false, false, 4096, 0, 0), false, config);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    timer.stop();
                    return null;
                }));
                return null;
            }));

            return null;
        });
        thread.set(tmp);

        overlay.setOnCancel(() -> {
            OverlayHelper.hide(null);
            thread.get().suspend();
            timer.stop();
        });

    }

    private Thread doDownload(UpdateRequest request, Callback<SignedObjectHolder<HashedDir>, Void> onFinished) {
        Task<SignedObjectHolder<HashedDir>> task = new Task<SignedObjectHolder<HashedDir>>() {
            @Override
            protected SignedObjectHolder<HashedDir> call() throws Exception {
                return request.request();
            }
        };

        task.setOnSucceeded(event -> onFinished.call((SignedObjectHolder<HashedDir>) event.getSource().getValue()));
        task.setOnFailed(event -> onFinished.call((SignedObjectHolder<HashedDir>) event.getSource().getValue()));

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        return thread;
    }

    public void onChosen() throws IOException {
        ScrollPane servers = (ScrollPane) Application.getInstance().getScene().lookup("#serversList");

        final double entry_height = 40;
        final double space = 5;


        AtomicInteger integer = new AtomicInteger(0);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {

                if (integer.get() == 0) {
                    OverlayHelper.showProgressOverlay("Сбор информации о серверах", null);
                    integer.incrementAndGet();
                    return;
                }

                if (integer.get() == 1) {

                    Pane pane = new Pane();

                    int i = 0;
                    for (SignedObjectHolder<ClientProfile> profile : profiles) {
                        try {
                            Node node = LoadedDataEntry.this.createServerNode(profile);
                            node.setLayoutY((entry_height + space) * i);
                            pane.getChildren().add(node);
                        } catch (IOException ignored) {
                        }
                        i++;
                    }

                    servers.setContent(pane);
                    Platform.runLater(() -> OverlayHelper.hide(null));
                    this.stop();
                }
            }
        };
        timer.start();
        timer.start();


    }

    private Node createServerNode(SignedObjectHolder<ClientProfile> profile) throws IOException {
        Node node = IOHelper.loadFXML("server-entry.fxml");
        {

            Label name = (Label) node.lookup("#pingServerName");
            Label participants = (Label) node.lookup("#participants");

            ServerInformation information = getInformation(profile.object);

            name.setText(information.name);
            participants.setText(information.participantsInfo);
        }
        node.setOnMouseClicked(event -> {

            if (currentServer != null) {
                Pane bPane = (Pane) currentServer.lookup("#entryBackground");
                Pane rPane = (Pane) currentServer.lookup("#serverEntryRoot");
                Label name = (Label) currentServer.lookup("#pingServerName");
                Label participants = (Label) currentServer.lookup("#participants");

                bPane.getChildren().clear();
                bPane.setVisible(false);
                rPane.getChildren().add(name);
                rPane.getChildren().add(participants);
            }

            Pane bPane = (Pane) node.lookup("#entryBackground");
            Pane rPane = (Pane) node.lookup("#serverEntryRoot");
            Label name = (Label) node.lookup("#pingServerName");
            Label participants = (Label) node.lookup("#participants");

            bPane.getChildren().add(name);
            bPane.getChildren().add(participants);
            bPane.setVisible(true);
            rPane.getChildren().removeIf(node1 -> node1.getId().equals("serverName"));
            rPane.getChildren().removeIf(node1 -> node1.getId().equals("participants"));


            currentServer = node;

            currentProfile = profile;
        });

        return node;
    }

    public ServerPinger.Result ping(ClientProfile profile) throws IOException {
        ServerPinger pinger = new ServerPinger(profile.getServerSocketAddress(), profile.getVersion());
        return pinger.ping();
    }

    public ServerInformation getInformation(ClientProfile profile) {
        String name = profile.getTitle();
        String participantsInfo = "Ошибка пинга";
        ServerPinger.Result ping = null;
        try {
            ping = ping(profile);

            participantsInfo = ping.onlinePlayers + "/" + ping.maxPlayers;
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return new ServerInformation(name, participantsInfo);
    }

    public Launcher.Config getConfig() {
        return config;
    }

    public void setConfig(Launcher.Config config) {
        this.config = config;
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    public DataEntry getEntry() {
        return dataEntry;
    }

    public static class ServerInformation {
        public final String name;
        public final String participantsInfo;

        public ServerInformation(String name, String participantsInfo) {
            this.name = name;
            this.participantsInfo = participantsInfo;
        }
    }
}
