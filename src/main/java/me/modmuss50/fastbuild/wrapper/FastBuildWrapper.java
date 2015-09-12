package me.modmuss50.fastbuild.wrapper;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class FastBuildWrapper {
    public static File fastBuildCache;
    public static File wrapperCache;
    public static boolean isModmussJenkins = false;

    public static void main(String[] args) throws Throwable {
        for (String arg : args) {
            if(arg.startsWith("-modmuss50Jenkins")){
                isModmussJenkins = true;
            }
        }
        String url = "http://modmuss50.me/fastbuild/";
        if(isModmussJenkins){
            url = url.replace("modmuss50.me", "localhost");
        }
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
            FileUtils.copyURLToFile(new URL(url + "version.txt"), fastBuildVersionInfoFile);
            FileUtils.copyURLToFile(new URL(url + "FastBuild.jar"), fastBuildJar);
            log("Done...");
        } else {
            FileUtils.copyURLToFile(new URL(url + "version.txt"), fastBuildVersionInfoTemp);

            BufferedReader tempReader = new BufferedReader(new FileReader(fastBuildVersionInfoTemp));
            String sCurrentLine;
            StringBuilder tempText = new StringBuilder();
            while ((sCurrentLine = tempReader.readLine()) != null) {
                tempText.append(sCurrentLine);
            }
            tempReader.close();

            BufferedReader localReader = new BufferedReader(new FileReader(fastBuildVersionInfoFile));
            StringBuilder localText = new StringBuilder();
            while ((sCurrentLine = localReader.readLine()) != null) {
                localText.append(sCurrentLine);
            }
            localReader.close();

            if (!localText.toString().equals(tempText.toString())) {
                fastBuildJar.delete();
                log("Updating FastBuild.. " + localText.toString() + " > " + tempText.toString());
                FileUtils.copyURLToFile(new URL(url + "FastBuild.jar"), fastBuildJar);

                fastBuildVersionInfoFile.delete();
                FileUtils.moveFile(fastBuildVersionInfoTemp, fastBuildVersionInfoFile);
                log("Done...");
            } else {
                log("Fastbuild is up to date");
            }
        }
        if (!fastBuildJar.exists()){
            log("Fastbuid jar was not found!!");
            fastBuildVersionInfoFile.delete();
        }
        if(fastBuildVersionInfoTemp.exists()){
            fastBuildVersionInfoTemp.delete();
        }
        ArrayList<String> newArgs = new ArrayList<String>();
        newArgs.add("java");
        newArgs.add("-jar");
        newArgs.add(fastBuildJar.getAbsolutePath());
        for(String arg : args){
            newArgs.add(arg + " ");
        }
        newArgs.add("-wrapper_v1");

        log("Starting fastbuild proccess");
        ProcessBuilder pb = new ProcessBuilder(newArgs);
        Process p = pb.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s = "";
        while((s = in.readLine()) != null){
            System.out.println(s);
        }
        int status = p.waitFor();
        System.out.println("Exited with status: " + status);

        System.exit(0);
    }

    public static void log(String string){
        System.out.println("FASTBUILD-WRAPPER: " + string);
    }

}
