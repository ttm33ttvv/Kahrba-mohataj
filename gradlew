#!/bin/sh

# Gradle start-up script for POSIX systems

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
app_path=$0

# Need this for daisy-chained symlinks.
while
    APP_HOME=${app_path%"${app_path##*/}"}  # leaves a trailing /; empty if no leading path
    [ -h "$app_path" ]
do
    ls=$(ls -ld "$app_path")
    link=${ls#*' -> '}
    case $link in             #(
      /*)   app_path=$link ;; #(
      *)    app_path=$APP_HOME$link ;;
    esac
done

# This is normally unused
# shellcheck disable=SC2034
APP_BASE_NAME=${0##*/}
APP_HOME=$( cd "${APP_HOME:-./}" && pwd -P ) || exit

# Use the maximum available file descriptors if possible
if [ -f /proc/sys/fs/file-max ]; then
    ulimit -n "$(cat /proc/sys/fs/file-max)"
fi

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Collect all arguments for the java command, stacking in reverse order:
#   * args from the command line
#   * the main class name
#   * -classpath
#   * -D...appname settings
#   * --module-path (only if needed)
#   * DEFAULT_JVM_OPTS, JAVA_OPTS, and GRADLE_OPTS environment variables.

# For Cygwin or MSYS, switch paths to Windows format before running java
if [ "$(uname -o)" = "Cygwin" ] || [ "$(uname -o)" = "Msys" ]; then
    APP_HOME=$(cygpath --path --mixed "$APP_HOME")
    CLASSPATH=$(cygpath --path --mixed "$CLASSPATH")

    # Restore the Cygwin/MSYS path after the above path conversion
    if [ -n "$MSYSTEM" ]; then
        MSYSROOT=$(cd "/" && pwd)
        APP_HOME="$MSYSROOT$APP_HOME"
    fi
fi

# Collect arguments as a single string
GRADLE_OPTS="$GRADLE_OPTS -Dorg.gradle.appname=$APP_BASE_NAME"

# Use the java executable
if [ -z "$JAVA_HOME" ] ; then
    JAVA_HOME=$(dirname "$(readlink -f "$(which java || true)")" 2>/dev/null || true)
fi

if [ ! -x "$JAVA_HOME/bin/java" ] ; then
    JAVA_CMD="java"
else
    JAVA_CMD="$JAVA_HOME/bin/java"
fi

if [ ! -x "$JAVA_CMD" ] ; then
    echo "Error: JAVA_HOME is not defined correctly." >&2
    echo "  We cannot execute $JAVA_CMD" >&2
    exit 1
fi

# Find the Gradle wrapper jar
if [ -n "$CLASSPATH" ] ; then
    CLASSPATH="$CLASSPATH"
else
    CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
fi

# Execute Gradle
exec "$JAVA_CMD" \
  -Dorg.gradle.appname="$APP_BASE_NAME" \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
