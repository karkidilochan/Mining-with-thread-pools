# Distributed Task Load Balancer with Thread Pools 🚀

This Java project implements a distributed system to balance computational workloads across nodes in a ring topology. Using custom thread pools, synchronization mechanisms, and task queues, the system ensures efficient task distribution and execution.

---

## Features 🛠️

- **Ring Topology**: Nodes are organized in a ring for efficient communication and load balancing.
- **Custom Thread Pool**: Configurable thread pools (2–16 threads) ensure efficient concurrent task execution.
- **Task Queue**: Tasks are managed using a thread-safe FIFO queue.
- **Load Balancing**: Skewed workloads are dynamically balanced between nodes using push-pull strategies.
- **Proof of Work Simulation**: Tasks include computationally intensive operations akin to mining in blockchain systems.


---

## Project Structure 📂

```plaintext
src/
├── distributed/
│   ├── registry/
│   │   └── Registry.java       # Manages node registration and topology
│   ├── node/
│   │   ├── ComputeNode.java    # Node logic for task execution and load balancing
│   │   ├── ThreadPool.java     # Custom thread pool implementation
│   │   ├── TaskQueue.java      # Thread-safe task queue
│   │   ├── Task.java           # Task definition (proof of work)
```

---

## System Architecture 🏗️

### 1. **Registry**
- Manages node registration and creates the ring topology.
- Collates and displays task statistics.

### 2. **Computational Nodes**
- Generate random tasks (1–1000 per round) using a seeded random number generator.
- Execute tasks using a local thread pool.
- Share task statistics and perform load-balancing maneuvers.

### 3. **Load Balancing**
- Balances workloads across nodes by migrating tasks (push/pull) while avoiding oscillatory behavior.
- Tasks that migrate are locked to their new node.

---

## How It Works ⚙️

1. **Setup**: Nodes register with the registry and form a ring topology.
2. **Task Execution**:
   - Nodes generate tasks and execute them locally.
   - Tasks include SHA-256 proof-of-work simulations to induce computational load.
3. **Load Balancing**:
   - Nodes communicate to share task distribution statistics.
   - Skewed workloads are balanced dynamically.

---

## Usage 🖥️

### Commands
Start the **Registry**:
```bash
java distributed.registry.Registry <portNum>
```

Start a **Compute Node**:
```bash
java distributed.node.ComputeNode <registry-host> <registry-portNum>
```

### Registry Commands
- **Setup Overlay**: `setup-overlay <thread-pool-size>`
- **Start Execution**: `start <number-of-rounds>`

### Example Execution
1. Start the registry:
   ```bash
   java distributed.registry.Registry 8080
   ```
2. Start a compute node:
   ```bash
   java distributed.node.ComputeNode localhost 8080
   ```
3. Configure the overlay and start task execution:
   ```bash
   setup-overlay 8
   start 100
   ```

---

## Outputs 📊

**Registry Output Example**:
```plaintext
Node1 | Generated: 50000 | Pulled: 20000 | Pushed: 15000 | Completed: 55000 | Percent: 11%
Node2 | Generated: 45000 | Pulled: 25000 | Pushed: 10000 | Completed: 60000 | Percent: 12%
...
Total Tasks: 500000
```

**Compute Node Output Example**:
```plaintext
Task{id=1, nonce=42, hash=0000000...}
Task{id=2, nonce=103, hash=0000000...}
```


---

## Highlights 🌟

- Implements foundational distributed system concepts.
- Demonstrates Java multithreading and synchronization.
- Simulates real-world load-balancing scenarios.
- Dynamically adapts to varying workloads across nodes.

---

## Requirements 📋

- **Java**: JDK 8 or higher
- **Build Tool**: Gradle

---

## Author ✍️

Designed as a portfolio project showcasing expertise in distributed systems and concurrent programming.

---

### 📜 License
This project is licensed under the MIT License.
```

You can save this content directly as `README.md` for your GitHub repository. Let me know if you need any additional tweaks or sections!
