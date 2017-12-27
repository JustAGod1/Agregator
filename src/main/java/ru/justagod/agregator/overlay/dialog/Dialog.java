package ru.justagod.agregator.overlay.dialog;

import javafx.event.EventHandler;
import javafx.util.Callback;
import ru.justagod.agregator.launcher.request.update.UpdateRequest;
import ru.justagod.agregator.overlay.Overlay;

import java.util.concurrent.Callable;

public interface Dialog<Result> extends Overlay {

    void start(Callback<Result, Boolean> callback);
}
