package me.modmuss50.fastbuild.wrapper;

import me.modmuss50.fastbuild.FastBuild;

import java.util.List;

/**
 * Created by Mark on 11/09/2015.
 */
public class RunFastBuild {

    public void start(List<String> args) throws Throwable {
        FastBuild build = new FastBuild();
        build.start(args);
    }
}
