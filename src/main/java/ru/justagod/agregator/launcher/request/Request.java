package ru.justagod.agregator.launcher.request;

import com.sun.istack.internal.NotNull;
import ru.justagod.agregator.launcher.Launcher;
import ru.justagod.agregator.launcher.helper.IOHelper;
import ru.justagod.agregator.launcher.helper.SecurityHelper;
import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.HOutput;
import ru.justagod.agregator.launcher.serialize.stream.EnumSerializer;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Request<R> {
    protected final Launcher.Config config;
    private final AtomicBoolean started = new AtomicBoolean(false);

    protected Request(@NotNull Launcher.Config config) {
        if (config == null) {
            throw new NullPointerException("config is null");
        }
        this.config = config;
    }

    protected Request() {
        this(null);
    }

    public static void requestError(String message) throws RequestException {
        throw new RequestException(message);
    }

    public abstract Type getType();

    @SuppressWarnings("DesignForExtension")
    public R request() throws Exception {
        if (!started.compareAndSet(false, true)) {
            //throw new IllegalStateException("Request already started");
        }

        // Make launcher.request to LaunchServer
        try (Socket socket = IOHelper.newSocket()) {
            socket.connect(IOHelper.resolve(config.address));
            try (HInput input = new HInput(socket.getInputStream());
                 HOutput output = new HOutput(socket.getOutputStream())) {
                writeHandshake(input, output);
                return requestDo(input, output);
            }
        }
    }

    protected final void readError(HInput input) throws IOException {
        String error = input.readString(0);
        if (!error.isEmpty()) {
            requestError(error);
        }
    }

    protected abstract R requestDo(HInput input, HOutput output) throws Exception;

    private void writeHandshake(HInput input, HOutput output) throws IOException {
        // Write handshake
        output.writeInt(Launcher.PROTOCOL_MAGIC);
        output.writeBigInteger(config.publicKey.getModulus(), SecurityHelper.RSA_KEY_LENGTH + 1);
        EnumSerializer.write(output, getType());
        output.flush();

        // Verify is accepted
        if (!input.readBoolean()) {
            requestError("Serverside not accepted this connection");
        }
    }

    public enum Type implements EnumSerializer.Itf {
        PING(0), // Ping launcher.request
        LAUNCHER(1), UPDATE(2), UPDATE_LIST(3), // Update requests
        AUTH(4), JOIN_SERVER(5), CHECK_SERVER(6), // Auth requests
        PROFILE_BY_USERNAME(7), PROFILE_BY_UUID(8), BATCH_PROFILE_BY_USERNAME(9), // Profile requests
        CUSTOM(255); // Custom requests
        private static final EnumSerializer<Type> SERIALIZER = new EnumSerializer<>(Type.class);
        private final int n;

        Type(int n) {
            this.n = n;
        }

        public static Type read(HInput input) throws IOException {
            return SERIALIZER.read(input);
        }

        @Override
        public int getNumber() {
            return n;
        }
    }
}
