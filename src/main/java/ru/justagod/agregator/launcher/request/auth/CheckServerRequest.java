package ru.justagod.agregator.launcher.request.auth;

import ru.justagod.agregator.launcher.Launcher;
import ru.justagod.agregator.launcher.client.PlayerProfile;
import ru.justagod.agregator.launcher.helper.VerifyHelper;
import ru.justagod.agregator.launcher.request.Request;
import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.HOutput;

import java.io.IOException;

public final class CheckServerRequest extends Request<PlayerProfile> {
    private final String username;
    private final String serverID;

    public CheckServerRequest(Launcher.Config config, String username, String serverID) {
        super(config);
        this.username = VerifyHelper.verifyUsername(username);
        this.serverID = JoinServerRequest.verifyServerID(serverID);
    }

    public CheckServerRequest(String username, String serverID) {
        this(null, username, serverID);
    }

    @Override
    public Type getType() {
        return Type.CHECK_SERVER;
    }

    @Override
    protected PlayerProfile requestDo(HInput input, HOutput output) throws IOException {
        output.writeASCII(username, 16);
        output.writeASCII(serverID, 41); // 1 char for minus sign
        output.flush();

        // Read response
        readError(input);
        return input.readBoolean() ? new PlayerProfile(input) : null;
    }
}
