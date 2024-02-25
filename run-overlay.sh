#!/bin/bash

# Directory containing this script
DIR="$( cd "$( dirname "$0" )" && pwd )"

# Path to the JAR file
JAR_PATH="$DIR/build/libs/overlay-dijkstras.jar"

# List of machines
MACHINE_LIST="$DIR/machine_list"

# Registry host and port
HOST=atlanta
PORT=5000

# Calculate total lines in Java files
LINES=$(find "$DIR" -name "*.java" -print0 | xargs -0 cat | wc -l)
echo "Project has $LINES lines"

# Clean and build project
echo "Cleaning and building project..."
gradle clean
gradle build

# Launch Registry
echo "Launching Registry on $HOST..."
ssh -t "$HOST" "cd '$DIR' && java -cp '$JAR_PATH' csx55.overlay.node.Registry $PORT &"

# Sleep for 1 second to ensure Registry is up
sleep 1

# Launch Messaging Nodes
echo "Launching Messaging Nodes on machines from $MACHINE_LIST..."
SCRIPT="java -cp '$JAR_PATH' csx55.overlay.node.MessagingNode $HOST $PORT"

while IFS= read -r machine; do
   echo "Launching Messaging Node on $machine..."
   ssh -n "$machine" "cd '$DIR' && $SCRIPT &"
done < "$MACHINE_LIST"

