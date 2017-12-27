package ru.justagod.agregator.overlay;

import javafx.scene.layout.Pane;

public interface Overlay {

    void init();

    Pane getPane();

    void reset();
}
