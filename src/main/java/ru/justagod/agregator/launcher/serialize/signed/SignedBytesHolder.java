package ru.justagod.agregator.launcher.serialize.signed;

import java.io.IOException;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import ru.justagod.agregator.launcher.helper.SecurityHelper;
import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.HOutput;
import ru.justagod.agregator.launcher.serialize.stream.StreamObject;

public class SignedBytesHolder extends StreamObject {
	protected final byte[] bytes;
	private final byte[] sign;

	public SignedBytesHolder(HInput input, RSAPublicKey publicKey) throws IOException, SignatureException {
		this(input.readByteArray(0), input.readByteArray(-SecurityHelper.RSA_KEY_LENGTH), publicKey);
	}

	public SignedBytesHolder(byte[] bytes, byte[] sign, RSAPublicKey publicKey) throws SignatureException {
		SecurityHelper.verifySign(bytes, sign, publicKey);
		this.bytes = Arrays.copyOf(bytes, bytes.length);
		this.sign = Arrays.copyOf(sign, sign.length);
	}

	public SignedBytesHolder(byte[] bytes, RSAPrivateKey privateKey) {
		this.bytes = Arrays.copyOf(bytes, bytes.length);
		sign = SecurityHelper.sign(bytes, privateKey);
	}

	@Override
	public final void write(HOutput output) throws IOException {
		output.writeByteArray(bytes, 0);
		output.writeByteArray(sign, -SecurityHelper.RSA_KEY_LENGTH);
	}

	public final byte[] getBytes() {
		return Arrays.copyOf(bytes, bytes.length);
	}

	public final byte[] getSign() {
		return Arrays.copyOf(sign, sign.length);
	}
}
