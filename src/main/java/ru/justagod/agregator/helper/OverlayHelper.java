package ru.justagod.agregator.helper;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import javafx.util.Duration;
import ru.justagod.agregator.Application;
import ru.justagod.agregator.overlay.DownloadOverlay;
import ru.justagod.agregator.overlay.Overlay;
import ru.justagod.agregator.overlay.ProgressOverlay;
import ru.justagod.agregator.overlay.dialog.ChooseNameDialog;

public final class OverlayHelper {
    private static final Pane auth = (Pane) Application.getInstance().getScene().lookup("#authPane");
    private static final Pane launchers = (Pane) Application.getInstance().getScene().lookup("#serversPane");
    private static final Pane dimPane = (Pane) Application.getInstance().getScene().lookup("#dimPane");
    private static final ScrollPane servers = (ScrollPane) Application.getInstance().getScene().lookup("#serversList");

    private static final ProgressOverlay progress = new ProgressOverlay();
    private static final ChooseNameDialog choose_name = new ChooseNameDialog();
    private static final DownloadOverlay download_overlay = new DownloadOverlay();

    private static Overlay currentOverlay = null;

    static {
        progress.init();
        choose_name.init();
        download_overlay.init();

        dimPane.toFront();
    }

    public static DownloadOverlay showDownloadOverlay(String caption, Runnable onCancel) {
        download_overlay.reset();
        download_overlay.setCaption(caption);
        download_overlay.setOnCancel(onCancel);

        showOverlay(download_overlay, null);

        return download_overlay;
    }

    public static void showErrorOverlay(String error, long time) {
        showErrorOverlay(error, event -> {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                hide(null);
            }

        });
    }

    public static void showErrorOverlay(String error, EventHandler<ActionEvent> onFinished) {
        progress.reset();
        progress.setError();
        progress.setDescription(error);
        showOverlay(progress, onFinished);
    }

    public static void showProgressOverlay(String description, EventHandler<ActionEvent> onFinished) {
        progress.reset();
        progress.setDescription(description);
        showOverlay(progress, onFinished);
    }

    public static void showChooseNameDialog(String exampleName, Callback<String, Boolean> callback) {
        choose_name.reset();
        choose_name.setExampleName(exampleName);
        choose_name.start(callback);
        showOverlay(choose_name, null);
    }

    public synchronized static void showOverlay(Overlay overlay, EventHandler<ActionEvent> handler) {
        if (currentOverlay != null) {
            hide(null);
        }


        auth.setDisable(true);
        launchers.setDisable(true);
        servers.setDisable(true);


        currentOverlay = overlay;

        fade(dimPane, 0, 0, 1, event -> {
            synchronized (OverlayHelper.class) {
                dimPane.requestFocus();
                dimPane.setVisible(true);
                dimPane.getChildren().clear();
                try {

                    overlay.getPane().setLayoutX(getWidth() / 2 - currentOverlay.getPane().getWidth() / 2.0);
                    overlay.getPane().setLayoutY(getHeight() / 2 - currentOverlay.getPane().getHeight() / 2.0);

                    dimPane.getChildren().add(overlay.getPane());

                    // Fix overlay position


                    // Fade in
                    fade(overlay.getPane(), 0, 0.0, 1.0, handler);
                } catch (NullPointerException e) {
                    //e.printStackTrace();
                }

            }
        });
    }

    public static void fade(Node node, long delay, double from, double to, EventHandler<ActionEvent> onFinished) {
        FadeTransition transition = new FadeTransition(new Duration(100), node);

        transition.setFromValue(from);
        transition.setToValue(to);
        if (onFinished != null) {
            transition.setOnFinished(onFinished);
        }

        transition.play();
    }

    public synchronized static void hide(Runnable onFinished) {
        if (currentOverlay != null) {
            fade(currentOverlay.getPane(), 0, 1.0, 0.0, event -> {
                synchronized (OverlayHelper.class) {
                    dimPane.getChildren().clear();
                    fade(dimPane, 0, 1.0, 0.0, event1 -> {
                        dimPane.setVisible(false);

                        // Unfreeze root pane
                        auth.setDisable(false);
                        launchers.setDisable(false);
                        servers.setDisable(false);

                        Application.getInstance().getRootPane().requestFocus();

                        // Reset overlay state
                        currentOverlay = null;
                        if (onFinished != null) {
                            onFinished.run();
                        }
                    });
                }
            });
        }
    }

    public static void startTask(Runnable task) {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                task.run();
            }
        };
        timer.start();
        timer.start();
    }

    private static double getWidth() {
        return Application.getInstance().getRootPane().getPrefWidth();
    }

    private static double getHeight() {
        return Application.getInstance().getRootPane().getPrefHeight();
    }

}
