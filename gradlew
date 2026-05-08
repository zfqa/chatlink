#!/bin/sh
DIRNAME=$(dirname "$0")
JAVA_HOME=/c/Users/10906/.jdks/ms-17.0.17
exec "$JAVA_HOME/bin/java" -classpath "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
