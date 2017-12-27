package ru.justagod.agregator;

import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.justagod.agregator.helper.CommonHelper;
import ru.justagod.agregator.helper.IOHelper;
import ru.justagod.agregator.helper.OverlayHelper;
import ru.justagod.agregator.helper.RequestHelper;
import ru.justagod.agregator.misc.LauncherProcessor;
import ru.justagod.agregator.misc.data.Data;
import ru.justagod.agregator.misc.data.runtime.LoadedDataEntry;
import ru.justagod.agregator.misc.data.runtime.LoadedEntries;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipFile;

public class Application extends javafx.application.Application {

    private static Application instance;
    private Scene scene;
    private Pane rootPane;
    private ScrollPane launchers;
    private Node currentEntry;

    public Application() {
        instance = this;
    }

    public static Application getInstance() {
        return instance;
    }

    public static void main(String[] args) throws IOException {
        try {
            launch(args);
        } finally {
            Data.getInstance().write();
        }
    }

    public Scene getScene() {
        return scene;
    }

    public Pane getRootPane() {
        return rootPane;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = IOHelper.loadFXML("root.fxml");


        scene = new Scene(root);

        Box box = createAnimatedCube();
        box.setLayoutX(300);
        box.setLayoutY(300);

        rootPane = (Pane) scene.lookup("#pane");
        launchers = (ScrollPane) scene.lookup("#launchersList");

        //rootPane.getChildren().add(box);

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.sizeToScene();
        stage.setTitle("Minecraft Agregator");

        box.setLayoutY(235);
        box.setLayoutX(475);


        stage.setResizable(false);
        initializeFunctions();

        final TextField loginField = (TextField) scene.lookup("#login");
        final PasswordField passwordField = (PasswordField) scene.lookup("#password");

        loginField.setText(Data.getInstance().getSavedLogin());
        passwordField.setText(Data.getInstance().getSavedPassword());

        LoadedEntries.loadAll();
        showServers();

        stage.show();
    }

    private Box createAnimatedCube() {
        Box box = new Box();
        box.setMaterial(IOHelper.getRandomTexture().createMaterial());
        box.setTranslateX(0);
        box.setTranslateY(0);
        box.setTranslateZ(0);

        box.setDepth(100);
        box.setHeight(100);
        box.setWidth(100);

        box.setRotationAxis(Rotate.X_AXIS);
        box.setRotate(100);

        final AtomicReference<Double> angleLink = new AtomicReference<>();
        final AtomicLong time = new AtomicLong(System.nanoTime());
        angleLink.set(0d);
        AnimationTimer animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double angle = angleLink.get();
                angle += 0.5;
                if (now - time.get() >= 1000) {
                    box.setRotationAxis(Rotate.Y_AXIS);
                    box.setRotate(angle);
                }
                angleLink.set(angle);
                time.set(now);
            }
        };
        animationTimer.start();

        return box;
    }

    private void initializeFunctions() {
        Button addButton = (Button) scene.lookup("#add");
        addButton.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select minecraft launcher");
            chooser.selectedExtensionFilterProperty().setValue(new FileChooser.ExtensionFilter("Java file", "*.jar"));
            File file = chooser.showOpenDialog(null);
            try {
                ZipFile zip = new ZipFile(file);
                if (!LauncherProcessor.existsLauncher(zip)) {
                    OverlayHelper.showErrorOverlay("Этот файл не подходит", 5000);
                    return;
                }
                OverlayHelper.showChooseNameDialog("", param -> {
                    if (LauncherProcessor.existsName(param)) {
                        OverlayHelper.showProgressOverlay("Добавление лаунчера", null);
                        try {
                            LauncherProcessor.addLauncher(zip, param, param1 -> {
                                if (param1 != LauncherProcessor.ProcessorResult.OK) {
                                    OverlayHelper.showErrorOverlay(param1.message, 5000);
                                } else {
                                    OverlayHelper.hide(null);
                                }
                                return null;
                            });
                        } catch (Exception e) {
                            OverlayHelper.showErrorOverlay(e.getMessage(), 5000);
                        }

                        return true;
                    } else return false;
                });
            } catch (IOException e) {
                OverlayHelper.showErrorOverlay(e.getMessage(), 5000);
            }


        });

        Button authButton = (Button) scene.lookup("#auth");

        final TextField loginField = (TextField) scene.lookup("#login");
        final PasswordField passwordField = (PasswordField) scene.lookup("#password");
        authButton.setOnAction(event -> {
            String login = loginField.getText();
            String password = passwordField.getText();

            if (CommonHelper.isNullOrEmpty(login)) {
                OverlayHelper.showErrorOverlay("Введите логин", 5000);
            }

            if (CommonHelper.isNullOrEmpty(password)) {
                OverlayHelper.showErrorOverlay("Введите пароль", 5000);
            }

            Data.getInstance().setSavedLogin(login);
            Data.getInstance().setSavedPassword(password);

            try {
                RequestHelper.goAuth(login, password, authResult -> {
                    if (LoadedEntries.getCurrentEntry() != null) {
                        LoadedEntries.getCurrentEntry().downloadAndLaunch(authResult);
                    } else {
                        OverlayHelper.showErrorOverlay("Выберите лаунчер", 5000);
                    }
                    return null;
                });

            } catch (Exception e) {
                OverlayHelper.showErrorOverlay(e.getMessage(), 5000);
                e.printStackTrace();
            }

        });
    }

    public void showServers() throws IOException {
        final double entry_height = 40;
        final double entry_width = 190;
        final double space = 5;

        Pane pane = new Pane();

        int i = 0;
        for (Map.Entry<String, LoadedDataEntry> entry : LoadedEntries.getDataEntries().entrySet()) {
            Node node = createEntryNode(entry.getValue());
            node.setLayoutY((entry_height + space) * i);
            pane.getChildren().add(node);
            i++;
        }
        launchers.setContent(pane);
    }

    private Node createEntryNode(LoadedDataEntry entry) throws IOException {
        Node node = IOHelper.loadFXML("launcher-entry.fxml");
        {

            Label name = (Label) node.lookup("#serverName");

            name.setText(entry.getEntry().getLauncherName());
        }
        node.setOnMouseClicked(event -> {

            try {
                if (currentEntry != null) {
                    Pane bPane = (Pane) currentEntry.lookup("#entryBackground");
                    Pane rPane = (Pane) currentEntry.lookup("#serverEntryRoot");
                    Label name = (Label) currentEntry.lookup("#serverName");

                    bPane.getChildren().clear();
                    bPane.setVisible(false);
                    rPane.getChildren().add(name);
                }
            } catch (Exception e) {
                currentEntry = null;
            }

            try {
                LoadedEntries.setCurrentEntry(entry.getEntry().getLauncherName());

                Pane bPane = (Pane) node.lookup("#entryBackground");
                Pane rPane = (Pane) node.lookup("#serverEntryRoot");
                Label name = (Label) node.lookup("#serverName");

                bPane.getChildren().add(name);
                bPane.setVisible(true);
                rPane.getChildren().removeIf(node1 -> node1.getId().equals("serverName"));


                currentEntry = node;
            } catch (Exception e) {
                e.printStackTrace();
                OverlayHelper.showErrorOverlay(e.getMessage(), 5000);
                ScrollPane servers = (ScrollPane) Application.getInstance().getScene().lookup("#serversList");
                servers.setContent(new Pane());
            }




        });

        if (LoadedEntries.getCurrentEntry() == entry) {
            node.onMouseClickedProperty().getValue().handle(null);
        }

        return node;
    }


}
