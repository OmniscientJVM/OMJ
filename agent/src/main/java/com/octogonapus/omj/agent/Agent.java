package com.octogonapus.omj.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

public class Agent {

    public static void premain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer(new OMJClassFileTransformer());

        try {
            // Extract the agent-lib jar from our jar and let the instrumented jvm load from it
            instrumentation.appendToSystemClassLoaderSearch(new JarFile(AgentLibJarExtractor.extractJar()));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}