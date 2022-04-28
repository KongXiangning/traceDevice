#!/bin/bash
java $JAVA_OPTS -server \
-XX:+UnlockExperimentalVMOptions \
-XX:-OmitStackTraceInFastThrow \
-Djava.security.egd=file:/dev/./urandom \
org.springframework.boot.loader.JarLauncher