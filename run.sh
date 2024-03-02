#!/bin/bash

# Get the directory of the script
BASE_DIR="$( cd "$( dirname "$0" )" && pwd )"

# Set the directory for compiled classes
BUILD_DIR="$BASE_DIR/build/classes/java/main"

HOST=pierre
PORT=5001


gradle clean
gradle build

gnome-terminal -- bash -c "ssh -t $HOST 'cd $BUILD_DIR; java -cp . csx55.overlay.node.Registry $PORT; bash;'"

sleep 1

COMMAND="cd $BUILD_DIR; java -cp . csx55.overlay.node.ComputeNode $HOST $PORT"

for ((j=0; j<${1:-1}; j++))
do
    RUN='gnome-terminal'
    for i in `cat machine_list`
    do
        echo 'logging into '$i
        SSH='--tab -e "ssh -t '$i' '$COMMAND'"'
        RUN+=" $SSH"
    done
    eval $RUN &
done