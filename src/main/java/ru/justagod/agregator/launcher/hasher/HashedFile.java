package ru.justagod.agregator.launcher.hasher;

import ru.justagod.agregator.launcher.helper.IOHelper;
import ru.justagod.agregator.launcher.helper.SecurityHelper;
import ru.justagod.agregator.launcher.helper.VerifyHelper;
import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.HOutput;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

public final class HashedFile extends HashedEntry {
    private static final byte[] DUMMY_HASH = new byte[0];

    // Instance
    public final long size;
    private final byte[] digest;

    public HashedFile(long size, byte[] digest) {
        this.size = VerifyHelper.verifyLong(size, VerifyHelper.L_NOT_NEGATIVE, "Illegal size: " + size);
        this.digest = Arrays.copyOf(digest, digest.length);
    }

    public HashedFile(Path file, long size, boolean hash) throws IOException {
        this(size, hash ? SecurityHelper.digest(SecurityHelper.DigestAlgorithm.MD5, file) : DUMMY_HASH);
    }

    public HashedFile(HInput input) throws IOException {
        this(input.readVarLong(), input.readByteArray(SecurityHelper.CRYPTO_MAX_LENGTH));
    }

    @Override
    public Type getType() {
        return Type.FILE;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public void write(HOutput output) throws IOException {
        output.writeVarLong(size);
        output.writeByteArray(digest, SecurityHelper.CRYPTO_MAX_LENGTH);
    }

    public boolean isSame(HashedFile o) {
        return size == o.size && Arrays.equals(digest, o.digest);
    }

    public boolean isSame(Path file) throws IOException {
        return isSame(new HashedFile(file, IOHelper.readAttributes(file).size(), true));
    }

    public boolean isSameDigest(byte[] digest) {
        return Arrays.equals(this.digest, digest);
    }
}
