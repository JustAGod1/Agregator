package ru.justagod.agregator.launcher.serialize.config;

import java.io.IOException;
import java.util.Objects;

import ru.justagod.agregator.launcher.serialize.HOutput;
import ru.justagod.agregator.launcher.serialize.config.entry.BlockConfigEntry;
import ru.justagod.agregator.launcher.serialize.stream.StreamObject;

public abstract class ConfigObject extends StreamObject {
    public final BlockConfigEntry block;

	protected ConfigObject(BlockConfigEntry block) {
		this.block = Objects.requireNonNull(block, "block");
	}

	@Override
	public final void write(HOutput output) throws IOException {
		block.write(output);
	}

	@FunctionalInterface
	public interface Adapter<O extends ConfigObject> {
		O convert(BlockConfigEntry entry);
	}
}
