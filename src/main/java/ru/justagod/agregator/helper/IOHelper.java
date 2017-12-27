package ru.justagod.agregator.helper;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import ru.justagod.agregator.launcher.helper.JVMHelper;
import ru.justagod.agregator.launcher.helper.LogHelper;
import ru.justagod.agregator.misc.MaterialBundle;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public final class IOHelper {

    public static final String LAYER_PATH = "layer/";
    public static final String WORKING_DIR = System.getProperty("user.home") + "/.agregator";
    public static final List<MaterialBundle> IMAGES = new LinkedList<MaterialBundle>();
    public static final String jvmMustdie32Dir = "jre-8u92-win32";
    public static final String jvmMustdie64Dir = "jre-8u92-win64";
    public static final String jvmLinux32Dir = "jre-8u92-linux32";
    public static final String jvmLinux64Dir = "jre-8u92-linux64";
    public static final String jvmMacOSXDir = "jre-8u92-macosx";
    public static final String jvmUnknownDir = "jre-8u92-unknown";

    public static final String jvmDirName;

    static {
        IMAGES.add(new MaterialBundle("hellrock"));

        File file = new File(WORKING_DIR);
        if (!file.exists()) {
            file.mkdir();
            try {
                Files.setAttribute(Paths.get(WORKING_DIR), "dos:hidden", true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        switch (JVMHelper.OS_TYPE) {
            case MUSTDIE: {
                jvmDirName = JVMHelper.OS_BITS == 32 ? jvmMustdie32Dir : JVMHelper.OS_BITS == 64 ? jvmMustdie64Dir : jvmUnknownDir;
                break;
            }
            // 64-bit Mustdie
            case LINUX: {
                jvmDirName = JVMHelper.OS_BITS == 32 ? jvmLinux32Dir : // 32-bit Linux
                        JVMHelper.OS_BITS == 64 ? jvmLinux64Dir : jvmUnknownDir;
                break;
            }// 64-bit Linux
            case MACOSX: {
                jvmDirName = JVMHelper.OS_BITS == 64 ? jvmMacOSXDir : jvmUnknownDir;
                break; // 64-bit MacOSX
            }
            default: {
                jvmDirName = jvmUnknownDir;
                LogHelper.warning("Unknown OS: '%s'", JVMHelper.OS_TYPE.name);
                break;
            }
            // Unknown OS
        }
    }

    public static URL getURL(String path) throws NoSuchFileException {
        URL url = ClassLoader.getSystemResource(path);
        if (url == null) {
            throw new NoSuchFileException(path);
        }
        return url;
    }

    public static BufferedReader createReader(String path) throws IOException {
        return new BufferedReader(new InputStreamReader(createInput(path)));
    }

    public static InputStream createInput(String path) throws IOException {
        return getURL(path).openStream();
    }

    public static URL getLayerURL(String path) throws NoSuchFileException {
        return getURL(LAYER_PATH + path);
    }

    public static BufferedReader createLayerReader(String path) throws IOException {
        return createReader(LAYER_PATH + path);
    }

    public static Parent loadFXML(String path) throws IOException {
        return loadFXML(getLayerURL(path));
    }

    public static Parent loadFXML(URL url) throws IOException {
        FXMLLoader loader = new FXMLLoader(url);
        return loader.load();
    }

    public static MaterialBundle getRandomTexture() {
        Random random = new Random();
        int index = random.nextInt(IMAGES.size());
        return IMAGES.get(index);
    }

}
