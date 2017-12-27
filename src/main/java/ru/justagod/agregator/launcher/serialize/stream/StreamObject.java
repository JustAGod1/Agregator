package ru.justagod.agregator.launcher.serialize.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ru.justagod.agregator.launcher.helper.IOHelper;
import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.HOutput;

public abstract class StreamObject {
	/* public StreamObject(HInput input) */

	public abstract void write(HOutput output) throws IOException;

	public final byte[] write() throws IOException {
		try (ByteArrayOutputStream array = IOHelper.newByteArrayOutput()) {
			try (HOutput output = new HOutput(array)) {
				write(output);
			}
			return array.toByteArray();
		}
	}

	@FunctionalInterface
	public interface Adapter<O extends StreamObject> {
		O convert(HInput input) throws IOException;
	}
}
