package ru.justagod.agregator.launcher.client;

import ru.justagod.agregator.launcher.helper.IOHelper;
import ru.justagod.agregator.launcher.helper.SecurityHelper;
import ru.justagod.agregator.launcher.helper.VerifyHelper;
import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.HOutput;
import ru.justagod.agregator.launcher.serialize.stream.StreamObject;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;

public final class PlayerProfile extends StreamObject {
    public final UUID uuid;
    public final String username;
    public final Texture skin, cloak;

    public PlayerProfile(HInput input) throws IOException {
        uuid = input.readUUID();
        //input.readASCII(16);
        username = VerifyHelper.verifyUsername(input.readASCII(16));
        skin = input.readBoolean() ? new Texture(input) : null;
        cloak = input.readBoolean() ? new Texture(input) : null;
    }

    public PlayerProfile(UUID uuid, String username, Texture skin, Texture cloak) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.username = VerifyHelper.verifyUsername(username);
        this.skin = skin;
        this.cloak = cloak;
    }

    public static PlayerProfile newOfflineProfile(String username) {
        return new PlayerProfile(offlineUUID(username), username, null, null);
    }

    public static UUID offlineUUID(String username) {
        return UUID.nameUUIDFromBytes(IOHelper.encodeASCII("OfflinePlayer:" + username));
    }

    @Override
    public void write(HOutput output) throws IOException {
        output.writeUUID(uuid);
        output.writeASCII(username, 16);

        // Write textures
        output.writeBoolean(skin != null);
        if (skin != null) {
            skin.write(output);
        }
        output.writeBoolean(cloak != null);
        if (cloak != null) {
            cloak.write(output);
        }
    }

    public static final class Texture extends StreamObject {
        public final String url;
        public final byte[] digest;

        public Texture(String url, byte[] digest) {
            this.url = IOHelper.verifyURL(url);
            this.digest = Objects.requireNonNull(digest, "digest");
        }

        public Texture(String url) throws IOException {
            this.url = IOHelper.verifyURL(url);
            digest = SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, new URL(url));
        }

        public Texture(HInput input) throws IOException {
            this.url = IOHelper.verifyURL(input.readASCII(2048));
            this.digest = input.readByteArray(SecurityHelper.CRYPTO_MAX_LENGTH);
        }

        @Override
        public void write(HOutput output) throws IOException {
            output.writeASCII(url, 2048);
            output.writeByteArray(digest, SecurityHelper.CRYPTO_MAX_LENGTH);
        }
    }
}
