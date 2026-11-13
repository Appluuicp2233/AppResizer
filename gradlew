#!/bin/sh

APP_HOME=$( cd "${0%/*}" && pwd )
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -n "$JAVA_HOME" ]; then
    if [ -x "$JAVA_HOME/jre/sh/java" ]; then
        JAVACMD=$JAVA_HOME/jre/sh/java
    else
        JAVACMD=$JAVA_HOME/bin/java
    fi
else
    JAVACMD=java
fi

if [ ! -x "$JAVACMD" ]; then
    echo "ERROR: JAVA_HOME is not set and no java command could be found in your PATH."
    exit 1
fi

exec "$JAVACMD" -Dorg.gradle.appname="$APP_BASE_NAME" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
