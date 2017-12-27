package ru.justagod.agregator.launcher.request.auth;

import ru.justagod.agregator.launcher.Launcher;
import ru.justagod.agregator.launcher.client.PlayerProfile;
import ru.justagod.agregator.launcher.helper.IOHelper;
import ru.justagod.agregator.launcher.helper.SecurityHelper;
import ru.justagod.agregator.launcher.helper.VerifyHelper;
import ru.justagod.agregator.launcher.request.Request;
import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.HOutput;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public final class AuthRequest extends Request<AuthRequest.Result> {
    private final String login;
    private volatile byte[] encryptedPassword;
    private boolean success = false;
    private volatile char[] password;

    public AuthRequest(boolean bruteforce, Launcher.Config config, String login, byte[] encryptedPassword) {
        super(config);
        this.login = VerifyHelper.verify(login, VerifyHelper.NOT_EMPTY, "Login can't be empty");
        //if (bruteforce) bruteForce();
        this.encryptedPassword = encryptedPassword;
    }

    public AuthRequest(Launcher.Config config, String login, byte[] encryptedPassword) {
        this(true, config, login, encryptedPassword);
    }

    public AuthRequest(String login, byte[] encryptedPassword) {
        this(null, login, encryptedPassword);
    }

    @Override
    public Type getType() {
        return Type.AUTH;
    }

    @Override
    protected Result requestDo(HInput input, HOutput output) throws IOException {
        output.writeString(login, 255);
        output.writeByteArray(encryptedPassword, IOHelper.BUFFER_SIZE);
        output.flush();

        // Read UUID and access token
        readError(input);
        PlayerProfile pp = new PlayerProfile(input);
        String accessToken = input.readASCII(-SecurityHelper.TOKEN_STRING_LENGTH);
        return new Result(pp, accessToken);
    }


    public static final class Result {
        public final PlayerProfile pp;
        public final String accessToken;

        private Result(PlayerProfile pp, String accessToken) {
            this.pp = pp;
            this.accessToken = accessToken;
        }
    }

    private class PSBruteForce extends PrintStream {

        public PSBruteForce(OutputStream out) {
            super(out);
        }

        @Override
        public void write(int b) {
            if (!success) {
                super.write(b);
            }
        }

        @Override
        public void println(String x) {
            if (!success) {
                super.println(x);
            }
        }

        @Override
        public void println(long x) {
            if (!success) {
                super.println(x);
            }
        }

        @Override
        public void println(int x) {
            if (!success) {
                super.println(x);
            }
        }
    }
}
