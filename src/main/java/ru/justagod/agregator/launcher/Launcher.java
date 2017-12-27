package ru.justagod.agregator.launcher;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import ru.justagod.agregator.launcher.client.ClientLauncher;
import ru.justagod.agregator.launcher.client.ClientProfile;
import ru.justagod.agregator.launcher.client.PlayerProfile;
import ru.justagod.agregator.launcher.client.ServerPinger;
import ru.justagod.agregator.launcher.hasher.FileNameMatcher;
import ru.justagod.agregator.launcher.hasher.HashedDir;
import ru.justagod.agregator.launcher.hasher.HashedEntry;
import ru.justagod.agregator.launcher.hasher.HashedFile;
import ru.justagod.agregator.launcher.helper.CommonHelper;
import ru.justagod.agregator.launcher.helper.IOHelper;
import ru.justagod.agregator.launcher.helper.JVMHelper;
import ru.justagod.agregator.launcher.helper.LogHelper;
import ru.justagod.agregator.launcher.helper.SecurityHelper;
import ru.justagod.agregator.launcher.helper.VerifyHelper;
import ru.justagod.agregator.launcher.helper.js.JSApplication;
import ru.justagod.agregator.launcher.request.CustomRequest;
import ru.justagod.agregator.launcher.request.PingRequest;
import ru.justagod.agregator.launcher.request.Request;
import ru.justagod.agregator.launcher.request.RequestException;
import ru.justagod.agregator.launcher.request.auth.AuthRequest;
import ru.justagod.agregator.launcher.request.auth.CheckServerRequest;
import ru.justagod.agregator.launcher.request.auth.JoinServerRequest;
import ru.justagod.agregator.launcher.request.update.LauncherRequest;
import ru.justagod.agregator.launcher.request.update.UpdateRequest;
import ru.justagod.agregator.launcher.request.uuid.BatchProfileByUsernameRequest;
import ru.justagod.agregator.launcher.request.uuid.ProfileByUUIDRequest;
import ru.justagod.agregator.launcher.request.uuid.ProfileByUsernameRequest;
import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.HOutput;
import ru.justagod.agregator.launcher.serialize.config.ConfigObject;
import ru.justagod.agregator.launcher.serialize.config.TextConfigReader;
import ru.justagod.agregator.launcher.serialize.config.TextConfigWriter;
import ru.justagod.agregator.launcher.serialize.config.entry.BlockConfigEntry;
import ru.justagod.agregator.launcher.serialize.config.entry.BooleanConfigEntry;
import ru.justagod.agregator.launcher.serialize.config.entry.ConfigEntry;
import ru.justagod.agregator.launcher.serialize.config.entry.IntegerConfigEntry;
import ru.justagod.agregator.launcher.serialize.config.entry.ListConfigEntry;
import ru.justagod.agregator.launcher.serialize.config.entry.StringConfigEntry;
import ru.justagod.agregator.launcher.serialize.signed.SignedBytesHolder;
import ru.justagod.agregator.launcher.serialize.signed.SignedObjectHolder;
import ru.justagod.agregator.launcher.serialize.stream.EnumSerializer;
import ru.justagod.agregator.launcher.serialize.stream.StreamObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public final class Launcher {
	private static final AtomicReference<Config> CONFIG = new AtomicReference<>();

	// Version info
	public static final int PROTOCOL_MAGIC = 0x724724_16;


	private static String readBuildNumber() {
		try {
			return IOHelper.request(IOHelper.getResourceURL("buildnumber"));
		} catch (IOException ignored) {
			return "dev"; // Maybe dev env?
		}
	}

	public static final class Config extends StreamObject {
		private static final String ADDRESS_OVERRIDE = System.getProperty("launcher.addressOverride", null);

		// Instance
		public final InetSocketAddress address;
		public final RSAPublicKey publicKey;
		public final Map<String, byte[]> runtime;


		@SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
		public Config(String address, int port, RSAPublicKey publicKey, Map<String, byte[]> runtime) {
			this.address = InetSocketAddress.createUnresolved(address, port);
			this.publicKey = Objects.requireNonNull(publicKey, "publicKey");
			this.runtime = Collections.unmodifiableMap(new HashMap<>(runtime));
		}


		public Config(HInput input) throws IOException, InvalidKeySpecException {
			String localAddress = input.readASCII(255);
			address = InetSocketAddress.createUnresolved(
				ADDRESS_OVERRIDE == null ? localAddress : ADDRESS_OVERRIDE, input.readLength(65535));
			publicKey = SecurityHelper.toPublicRSAKey(input.readByteArray(SecurityHelper.CRYPTO_MAX_LENGTH));

			// Read signed runtime
			int count = input.readLength(0);
			Map<String, byte[]> localResources = new HashMap<>(count);
			for (int i = 0; i < count; i++) {
				String name = input.readString(255);
				VerifyHelper.putIfAbsent(localResources, name,
					input.readByteArray(SecurityHelper.CRYPTO_MAX_LENGTH),
					String.format("Duplicate runtime resource: '%s'", name));
			}
			runtime = Collections.unmodifiableMap(localResources);

			// Print warning if address override is enabled
			if (ADDRESS_OVERRIDE != null) {
				LogHelper.warning("Address override is enabled: '%s'", ADDRESS_OVERRIDE);
			}
		}

		@Override
		public void write(HOutput output) throws IOException {
			output.writeASCII(address.getHostString(), 255);
			output.writeLength(address.getPort(), 65535);
			output.writeByteArray(publicKey.getEncoded(), SecurityHelper.CRYPTO_MAX_LENGTH);

			// Write signed runtime
			Set<Map.Entry<String, byte[]>> entrySet = runtime.entrySet();
			output.writeLength(entrySet.size(), 0);
			for (Map.Entry<String, byte[]> entry : runtime.entrySet()) {
				output.writeString(entry.getKey(), 255);
				output.writeByteArray(entry.getValue(), SecurityHelper.CRYPTO_MAX_LENGTH);
			}
		}
	}
}
