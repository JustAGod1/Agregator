package ru.justagod.agregator.launcher.request.uuid;

import ru.justagod.agregator.launcher.Launcher;
import ru.justagod.agregator.launcher.client.PlayerProfile;
import ru.justagod.agregator.launcher.request.Request;
import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.HOutput;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public final class ProfileByUUIDRequest extends Request<PlayerProfile> {
    private final UUID uuid;

    public ProfileByUUIDRequest(Launcher.Config config, UUID uuid) {
        super(config);
        this.uuid = Objects.requireNonNull(uuid, "uuid");
    }

    public ProfileByUUIDRequest(UUID uuid) {
        this(null, uuid);
    }

    @Override
    public Type getType() {
        return Type.PROFILE_BY_UUID;
    }

    @Override
    protected PlayerProfile requestDo(HInput input, HOutput output) throws IOException {
        output.writeUUID(uuid);
        output.flush();

        // Return profile
        return input.readBoolean() ? new PlayerProfile(input) : null;
    }
}
