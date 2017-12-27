package ru.justagod.agregator.launcher.request;

import ru.justagod.agregator.launcher.Launcher;
import ru.justagod.agregator.launcher.helper.VerifyHelper;
import ru.justagod.agregator.launcher.serialize.HInput;
import ru.justagod.agregator.launcher.serialize.HOutput;

public abstract class CustomRequest<T> extends Request<T> {
    public CustomRequest(Launcher.Config config) {
        super(config);
    }

    public CustomRequest() {
        this(null);
    }

    @Override
    public final Type getType() {
        return Type.CUSTOM;
    }

    @Override
    protected final T requestDo(HInput input, HOutput output) throws Exception {
        output.writeASCII(VerifyHelper.verifyIDName(getName()), 255);
        output.flush();

        // Custom launcher.request redirect
        return requestDoCustom(input, output);
    }

    public abstract String getName();

    protected abstract T requestDoCustom(HInput input, HOutput output);
}
