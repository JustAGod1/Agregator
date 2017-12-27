package ru.justagod.agregator.launcher.request.update;

import ru.justagod.agregator.launcher.Launcher;
import ru.justagod.agregator.launcher.client.ClientLauncher;
import ru.justagod.agregator.launcher.client.ClientProfile;
import ru.justagod.agregator.launcher.helper.IOHelper;
import ru.justagod.agregator.launcher.helper.JVMHelper;
import ru.justagod.agregator.launcher.helper.LogHelper;
import ru.justagod.agregator.launcher.helper.SecurityHelper;
import ru.justagod.agregator.launcher.request.Request;
import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.HOutput;
import ru.justagod.agregator.launcher.serialize.signed.SignedObjectHolder;

import java.nio.file.Path;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class LauncherRequest extends Request<LauncherRequest.Result> {
    public static final Path BINARY_PATH = IOHelper.getCodeSource(Launcher.class);
    public static final boolean EXE_BINARY = IOHelper.hasExtension(BINARY_PATH, "exe");

    public LauncherRequest(Launcher.Config config) {
        super(config);
    }

    public LauncherRequest() {
        this(null);
    }

    @Override
    public Type getType() {
        return Type.LAUNCHER;
    }

    @Override
    @SuppressWarnings("CallToSystemExit")
    protected Result requestDo(HInput input, HOutput output) throws Exception {
        output.writeBoolean(EXE_BINARY);
        output.flush();
        readError(input);

        // Verify launcher sign
        RSAPublicKey publicKey = config.publicKey;
        byte[] sign = input.readByteArray(-SecurityHelper.RSA_KEY_LENGTH);
        boolean shouldUpdate = false;//!SecurityHelper.isValidSign(BINARY_PATH, sign, publicKey);

        // Update launcher if need
        output.writeBoolean(shouldUpdate);
        output.flush();
        if (shouldUpdate) {
            byte[] binary = input.readByteArray(0);
            SecurityHelper.verifySign(binary, sign, publicKey);

            // Prepare process builder to start new instance (java -jar works for Launch4J's EXE too)
            ProcessBuilder builder = new ProcessBuilder(IOHelper.resolveJavaBin(null).toString(),
                    ClientLauncher.jvmProperty(LogHelper.DEBUG_PROPERTY, Boolean.toString(LogHelper.isDebugEnabled())),
                    "-jar", BINARY_PATH.toString());
            builder.inheritIO();

            // Rewrite and start new instance
            IOHelper.write(BINARY_PATH, binary);
            builder.start();

            // Kill current instance
            JVMHelper.RUNTIME.exit(255);
            throw new AssertionError("Why Launcher wasn't restarted?!");
        }

        // Read clients profiles list
        int count = input.readLength(0);
        List<SignedObjectHolder<ClientProfile>> profiles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            profiles.add(new SignedObjectHolder<>(input, publicKey, ClientProfile.RO_ADAPTER));
        }

        // Return launcher.request result
        return new Result(sign, profiles);
    }

    public static final class Result {
        public final List<SignedObjectHolder<ClientProfile>> profiles;
        private final byte[] sign;

        private Result(byte[] sign, List<SignedObjectHolder<ClientProfile>> profiles) {
            this.sign = Arrays.copyOf(sign, sign.length);
            this.profiles = Collections.unmodifiableList(profiles);
        }

        public byte[] getSign() {
            return Arrays.copyOf(sign, sign.length);
        }
    }
}
