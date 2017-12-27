package ru.justagod.agregator.launcher.serialize.stream;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import ru.justagod.agregator.launcher.helper.VerifyHelper;
import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.HOutput;
import ru.justagod.agregator.launcher.serialize.stream.EnumSerializer.Itf;

public final class EnumSerializer<E extends Enum<?> & Itf> {
	private final Map<Integer, E> map = new HashMap<>(16);

	public EnumSerializer(Class<E> clazz) {
		for (Field field : clazz.getFields()) {
			if (!field.isEnumConstant()) {
				continue;
			}

			// Add to map
			Itf itf;
			try {
				itf = (Itf) field.get(null);
			} catch (IllegalAccessException e) {
				throw new InternalError(e);
			}
			VerifyHelper.putIfAbsent(map, itf.getNumber(), clazz.cast(itf),
				"Duplicate number for enum constant " + field.getName());
		}
	}

	public E read(HInput input) throws IOException {
		int n = input.readVarInt();
		return VerifyHelper.getMapValue(map, n, "Unknown enum number: " + n);
	}

	public static void write(HOutput output, Itf itf) throws IOException {
		output.writeVarInt(itf.getNumber());
	}

	@FunctionalInterface
	public interface Itf {
		int getNumber();
	}
}
