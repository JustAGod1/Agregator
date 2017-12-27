package ru.justagod.agregator.overlay;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import ru.justagod.agregator.helper.IOHelper;

import java.io.IOException;

public class DownloadOverlay implements Overlay {

    private Pane pane;
    private Label caption;
    private Label description;
    private ProgressBar progress;
    private Button cancel;

    @Override
    public void init() {
        try {
            pane = (Pane) IOHelper.loadFXML("download.fxml");
            caption = (Label) pane.lookup("#caption");
            description = (Label) pane.lookup("#info");
            progress = (ProgressBar) pane.lookup("#progress");
            cancel = (Button) pane.lookup("#cancelButton");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Pane getPane() {
        return pane;
    }

    @Override
    public void reset() {
        setDescription("Error");
        setCaption("...");
        setProgress(0);
        setOnCancel(null);
    }

    public void setDescription(String s) {
        description.setText(s);
    }

    public void setCaption(String s) {
        caption.setText(s);
    }

    public void setProgress(double d) {
        progress.setProgress(d);
    }

    public void setOnCancel(Runnable runnable) {
        cancel.setOnAction(event -> runnable.run());
    }
}
