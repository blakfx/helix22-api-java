#!/bin/bash

# Test the java wrappers
# Must run this file from /helix/ directory within Docker
CUR_DIR=$(pwd)
if [ "$CUR_DIR" != "/helix" ]; then
    echo "You must run this file from /helix directory within Docker"
    exit 1
fi


if [ "$#" -ne 1 ]; then
    echo "Usage: run <username>, where username is the user to run the demo as.";
    exit 2
fi

HOST=10.10.0.56
PORT=5567
USER=$1

echo "Running the Java wrappers demonstration"

# Create a sample message (replacing any existent one)
echo "Creating client_text.txt..."
echo "Hello there, world" > client_text.txt

# Run the code
echo "Creating symlink to client JAR"
DEMO_JAR="java/demo/helix-java-api-demo-1.0.0-SNAPSHOT.jar"
echo "Launching chat client..."
# Simple encrypt + decrypt action
LD_LIBRARY_PATH="/native_dep/:$LD_LIBRARY_PATH" java -Djava.library.path="/native_dep/" -jar ${DEMO_JAR} -s $HOST -p $PORT -u $USER -f client_text.txt
