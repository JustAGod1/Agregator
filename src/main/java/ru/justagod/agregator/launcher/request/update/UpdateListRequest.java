package ru.justagod.agregator.launcher.request.update;

import ru.justagod.agregator.launcher.Launcher;
import ru.justagod.agregator.launcher.helper.IOHelper;
import ru.justagod.agregator.launcher.request.Request;
import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.HOutput;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class UpdateListRequest extends Request<Set<String>> {
    public UpdateListRequest(Launcher.Config config) {
        super(config);
    }

    public UpdateListRequest() {
        this(null);
    }

    @Override
    public Type getType() {
        return Type.UPDATE_LIST;
    }

    @Override
    protected Set<String> requestDo(HInput input, HOutput output) throws IOException {
        int count = input.readLength(0);

        // Read all update dirs names
        Set<String> result = new HashSet<>(count);
        for (int i = 0; i < count; i++) {
            result.add(IOHelper.verifyFileName(input.readString(255)));
        }

        // We're done. Make it unmodifiable and return
        return Collections.unmodifiableSet(result);
    }
}
