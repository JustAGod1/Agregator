package ru.justagod.agregator.launcher.request.uuid;

import ru.justagod.agregator.launcher.Launcher;
import ru.justagod.agregator.launcher.client.PlayerProfile;
import ru.justagod.agregator.launcher.helper.VerifyHelper;
import ru.justagod.agregator.launcher.request.Request;
import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.HOutput;

import java.io.IOException;

public final class ProfileByUsernameRequest extends Request<PlayerProfile> {
    private final String username;

    public ProfileByUsernameRequest(Launcher.Config config, String username) {
        super(config);
        this.username = VerifyHelper.verifyUsername(username);
    }

    public ProfileByUsernameRequest(String username) {
        this(null, username);
    }

    @Override
    public Type getType() {
        return Type.PROFILE_BY_USERNAME;
    }

    @Override
    protected PlayerProfile requestDo(HInput input, HOutput output) throws IOException {
        output.writeASCII(username, 16);
        output.flush();

        // Return profile
        return input.readBoolean() ? new PlayerProfile(input) : null;
    }
}
