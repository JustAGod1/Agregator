package ru.justagod.agregator.launcher.helper;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import java.util.Locale;

public final class CommonHelper {
    private static final String[] SCRIPT_ENGINE_ARGS = {"-strict"};
    private static final NashornScriptEngineFactory SCRIPT_ENGINE_FACTORY = new NashornScriptEngineFactory();

    private CommonHelper() {
    }

    public static String low(String s) {
        return s.toLowerCase(Locale.US);
    }

    public static ScriptEngine newScriptEngine() {
        return SCRIPT_ENGINE_FACTORY.getScriptEngine(SCRIPT_ENGINE_ARGS);
    }

    public static Thread newThread(String name, boolean daemon, Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(daemon);
        if (name != null) {
            thread.setName(name);
        }
        return thread;
    }

    public static String replace(String source, String... params) {
        for (int i = 0; i < params.length; i += 2) {
            source = source.replace('%' + params[i] + '%', params[i + 1]);
        }
        return source;
    }
}
