#!/bin/sh

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )" # get the path to the current folder

JAVA_PATH="/usr/lib/jvm/java-21-openjdk/bin"

# compile the class
$JAVA_PATH/javac -d . TestInterface.java

# create the jar
$JAVA_PATH/jar cvf TestInterface.jar -C . com/givainc/test

# clean up build files
rm -rf "$SCRIPT_DIR/com"
