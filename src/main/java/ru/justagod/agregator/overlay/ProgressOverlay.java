package ru.justagod.agregator.overlay;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import ru.justagod.agregator.helper.IOHelper;

import java.io.IOException;

public class ProgressOverlay implements Overlay {

    private Pane pane;
    private Label label;

    @Override
    public void init() {
        try {
            pane = (Pane) IOHelper.loadFXML("process/process.fxml");
            pane.setLayoutX(480 - 250);
            pane.setLayoutY(254.5 - 75);

            label = (Label) pane.lookup("#text");
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
        setDescription("...");
        label.setStyle("");
    }

    public void setDescription(String description) {
        label.setText(description);
    }

    public void setError() {
        label.setStyle("color: #ff0000");
    }
}
