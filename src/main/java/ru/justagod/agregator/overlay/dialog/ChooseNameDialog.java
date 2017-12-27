package ru.justagod.agregator.overlay.dialog;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import ru.justagod.agregator.helper.IOHelper;
import ru.justagod.agregator.helper.OverlayHelper;

import java.io.IOException;
import java.util.concurrent.Callable;

public class ChooseNameDialog implements Dialog<String> {

    private Pane pane;
    private TextField textField;
    private Button button;
    private Label error;






    @Override
    public void init() {
        try {
            pane = (Pane) IOHelper.loadFXML("choosename/choosename.fxml");
            textField = (TextField) pane.lookup("#name");
            button = (Button) pane.lookup("#button");
            error = (Label) pane.lookup("#nameError");
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
        button.setOnAction(null);
        error.setVisible(false);
        textField.setText("");
    }

    @Override
    public void start(Callback<String, Boolean> callback) {
        button.setOnAction(event -> fireEvent(callback));
        textField.setOnAction(event -> fireEvent(callback));
    }

    private void fireEvent(Callback<String, Boolean> callback) {
        if (callback.call(textField.getText())) {
            OverlayHelper.hide(null);
        } else {
            error.setVisible(true);
        }
    }

    public void setExampleName(String s) {
        textField.setText(s);
    }
}
