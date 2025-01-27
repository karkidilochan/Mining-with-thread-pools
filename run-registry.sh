#!/bin/bash

# Get the directory of the script
BASE_DIR="$( cd "$( dirname "$0" )" && pwd )"

# Set the directory for compiled classes
BUILD_DIR="$BASE_DIR/build/classes/java/main"

# Set the hostname of the current device
HOST="$1"

# Set the port
PORT="$2"

# Check if both host and port are provided
if [ -z "$HOST" ] || [ -z "$PORT" ]; then
    echo "Usage: $0 <host> <port>"
    exit 1
fi

gradle clean
gradle build

# Launch the Registry process in a new terminal window via SSH
gnome-terminal -- bash -c "ssh -t $HOST 'cd $BUILD_DIR; java -cp . distributed.overlay.node.Registry $PORT; bash;'"
