package ru.justagod.agregator.helper;

import javafx.util.Callback;
import ru.justagod.agregator.launcher.Launcher;
import ru.justagod.agregator.launcher.Launcher.Config;
import ru.justagod.agregator.launcher.helper.IOHelper;
import ru.justagod.agregator.launcher.helper.SecurityHelper;
import ru.justagod.agregator.launcher.request.auth.AuthRequest;
import ru.justagod.agregator.misc.data.runtime.LoadedDataEntry;
import ru.justagod.agregator.misc.data.runtime.LoadedEntries;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

public final class RequestHelper {

    public static void goAuth(String login, String password, Callback<AuthRequest.Result, Void> onFinished) throws Exception {
        Config config = getCurrentConfig();
        byte[] encrypted = SecurityHelper.newRSAEncryptCipher(config.publicKey).doFinal(IOHelper.encode(password));
        AuthRequest request = new AuthRequest(config, login, encrypted);
        AuthRequest.Result result = request.request();

        if (onFinished != null) {
            onFinished.call(result);
        }
    }

    private static Config getCurrentConfig() {
        LoadedDataEntry entry = LoadedEntries.getCurrentEntry();
        if (entry == null) {
            throw new NoCurrentEntryException();
        }
        return entry.getConfig();
    }

    private static class NoCurrentEntryException extends RuntimeException {
        public NoCurrentEntryException() {
            super("Please choose any server");
        }
    }
}
