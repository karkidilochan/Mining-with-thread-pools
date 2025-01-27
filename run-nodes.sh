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
    echo "Usage: $0 <registry_host> <registry_port>"
    exit 1
fi

# Launch Messaging Nodes
SCRIPT="cd $BUILD; java -cp . distributed.overlay.node.MessagingNode $HOST $PORT"

for ((j=0; j<${1:-1}; j++))
do
    COMMAND='gnome-terminal'
    for i in `cat machine_list`
    do
        echo 'logging into '$i
        OPTION='--tab -e "ssh -t '$i' '$SCRIPT'"'
        COMMAND+=" $OPTION"
    done
    eval $COMMAND &
done
