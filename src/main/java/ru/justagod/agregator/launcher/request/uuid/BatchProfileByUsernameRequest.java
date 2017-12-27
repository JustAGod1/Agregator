package ru.justagod.agregator.launcher.request.uuid;

import ru.justagod.agregator.launcher.Launcher;
import ru.justagod.agregator.launcher.client.PlayerProfile;
import ru.justagod.agregator.launcher.helper.IOHelper;
import ru.justagod.agregator.launcher.helper.VerifyHelper;
import ru.justagod.agregator.launcher.request.Request;
import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.HOutput;

import java.io.IOException;
import java.util.Arrays;

public final class BatchProfileByUsernameRequest extends Request<PlayerProfile[]> {
    public static final int MAX_BATCH_SIZE = 128;
    private final String[] usernames;

    public BatchProfileByUsernameRequest(Launcher.Config config, String... usernames) throws IOException {
        super(config);
        this.usernames = Arrays.copyOf(usernames, usernames.length);
        IOHelper.verifyLength(this.usernames.length, MAX_BATCH_SIZE);
        for (String username : this.usernames) {
            VerifyHelper.verifyUsername(username);
        }
    }

    public BatchProfileByUsernameRequest(String... usernames) throws IOException {
        this(null, usernames);
    }

    @Override
    public Type getType() {
        return Type.BATCH_PROFILE_BY_USERNAME;
    }

    @Override
    protected PlayerProfile[] requestDo(HInput input, HOutput output) throws IOException {
        output.writeLength(usernames.length, MAX_BATCH_SIZE);
        for (String username : usernames) {
            output.writeASCII(username, 16);
        }
        output.flush();

        // Read profiles response
        PlayerProfile[] profiles = new PlayerProfile[usernames.length];
        for (int i = 0; i < profiles.length; i++) {
            profiles[i] = input.readBoolean() ? new PlayerProfile(input) : null;
        }

        // Return result
        return profiles;
    }
}
