package ru.justagod.agregator.launcher.request;

import ru.justagod.agregator.launcher.Launcher;
import ru.justagod.agregator.launcher.client.ClientProfile;
import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.HOutput;

import java.io.IOException;

public final class PingRequest extends Request<Void> {
    public static final byte EXPECTED_BYTE = 0b01010101;

    public PingRequest(Launcher.Config config) {
        super(config);
    }

    public PingRequest() {
        this(null);
    }

    @Override
    public Type getType() {
        return Type.PING;
    }

    @Override
    protected Void requestDo(HInput input, HOutput output) throws IOException {
        byte pong = (byte) input.readUnsignedByte();
        if (pong != EXPECTED_BYTE) {
            throw new IOException("Illegal ping response: " + pong);
        }
        return null;
    }
}
