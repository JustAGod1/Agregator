package ru.justagod.agregator.launcher.serialize.config.entry;

import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.HOutput;

import java.io.IOException;

public final class BooleanConfigEntry extends ConfigEntry<Boolean> {
    public BooleanConfigEntry(boolean value, boolean ro, int cc) {
        super(value, ro, cc);
    }

    public BooleanConfigEntry(HInput input, boolean ro) throws IOException {
        this(input.readBoolean(), ro, 0);
    }

    @Override
    public Type getType() {
        return Type.BOOLEAN;
    }

    @Override
    public void write(HOutput output) throws IOException {
        output.writeBoolean(getValue());
    }
}