#!/bin/sh

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )" # get the path to the current folder

# Dynamically find Java 11 installation
JAVA_HOME=$(/usr/libexec/java_home -v 11)
JAVA_PATH="$JAVA_HOME/bin"

# Check if Java 11 was found
if [ -z "$JAVA_HOME" ]; then
  echo "Error: Java 11 not found on this system"
  exit 1
fi

echo "Using Java 11 at: $JAVA_PATH"

# compile the class
$JAVA_PATH/javac -d . TestInterface.java

# create the jar
$JAVA_PATH/jar cvf TestInterface.jar -C . com/givainc/test

# clean up build files
rm -rf "$SCRIPT_DIR/com"