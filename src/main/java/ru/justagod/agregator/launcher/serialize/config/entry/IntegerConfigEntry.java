package ru.justagod.agregator.launcher.serialize.config.entry;

import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.HOutput;

import java.io.IOException;

public final class IntegerConfigEntry extends ConfigEntry<Integer> {
    public IntegerConfigEntry(int value, boolean ro, int cc) {
        super(value, ro, cc);
    }

    public IntegerConfigEntry(HInput input, boolean ro) throws IOException {
        this(input.readVarInt(), ro, 0);
    }

    @Override
    public Type getType() {
        return Type.INTEGER;
    }

    @Override
    public void write(HOutput output) throws IOException {
        output.writeVarInt(getValue());
    }
}