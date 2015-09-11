package me.modmuss50.fastbuild.wrapper;

import me.modmuss50.fastbuild.FastBuild;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class FastBuildWrapper {
    public static File fastBuildCache;
    public static File wrapperCache;

    public static void main(String[] args) throws Throwable {
        File homeDir = new File(System.getProperty("user.home"));
        fastBuildCache = new File(homeDir, ".fastbuild");
        if (!fastBuildCache.exists()) {
            fastBuildCache.mkdir();
        }
        wrapperCache = new File(fastBuildCache, "wrapper");
        if (!wrapperCache.exists()) {
            wrapperCache.mkdir();
        }

        File fastBuildJar = new File(wrapperCache, "FastBuild.jar");
        File fastBuildVersionInfoFile = new File(wrapperCache, "wrapperVersion.info");
        File fastBuildVersionInfoTemp = new File(wrapperCache, "wrapperVersion.info.temp");
        if (!fastBuildJar.exists() || !fastBuildVersionInfoFile.exists()) {
            System.out.println("Fastbuild ");
            //We can reset this
            if (fastBuildVersionInfoFile.exists()) {
                fastBuildVersionInfoFile.delete();
            }
            log("Downloading fastbuild...");
            FileUtils.copyURLToFile(new URL("http://modmuss50.me/fastbuild/version.txt"), fastBuildVersionInfoFile);
            FileUtils.copyURLToFile(new URL("http://modmuss50.me/fastbuild/FastBuild.jar"), fastBuildJar);
            log("Done...");
        } else {
            FileUtils.copyURLToFile(new URL("http://modmuss50.me/fastbuild/version.txt"), fastBuildVersionInfoTemp);

            BufferedReader tempReader = new BufferedReader(new FileReader(fastBuildVersionInfoTemp));
            String sCurrentLine;
            StringBuilder tempText = new StringBuilder();
            while ((sCurrentLine = tempReader.readLine()) != null) {
                tempText.append(sCurrentLine);
            }


            BufferedReader localReader = new BufferedReader(new FileReader(fastBuildVersionInfoFile));
            StringBuilder localText = new StringBuilder();
            while ((sCurrentLine = localReader.readLine()) != null) {
                localText.append(sCurrentLine);
            }
            if (!localText.toString().equals(tempText.toString())) {
                fastBuildJar.delete();
                log("Updating FastBuild.. " + localText.toString() + " > " + tempText.toString());
                FileUtils.copyURLToFile(new URL("http://modmuss50.me/fastbuild/FastBuild.jar"), fastBuildJar);

                fastBuildVersionInfoFile.delete();
                FileUtils.moveFile(fastBuildVersionInfoTemp, fastBuildVersionInfoFile);
                log("Done...");
            } else {
                log("Fastbuild is up to date");
            }
        }
        if(fastBuildVersionInfoTemp.exists()){
            fastBuildVersionInfoFile.delete();
        }
        addToClasspath(fastBuildJar);
        args[args.length +1] = "-wrapper_v1";
        FastBuild.main(args);
    }

    public static void log(String string){
        System.out.println("FASTBUILD-WRAPPER: " + string);
    }

    /**
     * Credit to https://github.com/Slowpoke101/FTBLaunch/blob/master/src/main/java/net/ftb/workers/AuthlibDLWorker.java
     */
    public static boolean addToClasspath(File file) {
        log("Loading external library " + file.getName() + " to classpath");
        try {
            if (file.exists()) {
                addURL(file.toURI().toURL());
            } else {
                log("Error loading jar");
            }
        } catch (Throwable t) {
            if (t.getMessage() != null) {
                log(t.getMessage());
            }
            return false;
        }

        return true;
    }

    /**
     * Credit to https://github.com/Slowpoke101/FTBLaunch/blob/master/src/main/java/net/ftb/workers/AuthlibDLWorker.java
     */
    public static void addURL(URL u) throws IOException {
        URLClassLoader sysloader = (URLClassLoader) FastBuildWrapper.class.getClass().getClassLoader();
        Class sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysloader, u);
        } catch (Throwable t) {
            if (t.getMessage() != null) {
                log(t.getMessage());
            }
            throw new IOException("Error, could not add URL to system classloader");
        }
    }
}
