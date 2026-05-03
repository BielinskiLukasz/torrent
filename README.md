# Torrent P2P File Transfer (TCP Implementation)

![Status](https://img.shields.io/badge/status-finished-brightgreen)
![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Java](https://img.shields.io/badge/Java-8-orange?logo=openjdk)
![Protocol](https://img.shields.io/badge/Protocol-TCP-blue)
![Architecture](https://img.shields.io/badge/Architecture-P2P%20%2F%20Client--Server-lightgrey)
![License](https://img.shields.io/badge/License-MIT-yellow)

A robust Peer-to-Peer (P2P) file-sharing application implemented in Java, utilizing the **TCP protocol** for reliable data transmission. Developed as a high-performance networking project for **PJATK**, it features both direct Host-to-Host (H2H) and Multi-Host (MH) coordination models.

## 📌 Project Status

This project is **feature-complete** and considered **finished**.  
The application is stable, fully functional, and ready for use in both H2H and MH modes.

Further improvements listed below are optional ideas for future development.

## 🚀 Key Features

* **Reliable Data Transfer:** Built entirely on TCP to guarantee packet delivery and data integrity.
* **Connection Resilience (Auto-Resume):** Automatically resumes interrupted transfers if a peer reconnects within a configurable grace period.
* **Multi-Source Swarming:** The `multiple_pull` feature downloads different file segments from multiple peers simultaneously.
* **Integrity Verification:** Uses checksums to validate files during listing and after transfer.
* **Flexible Architecture:**
  * **Host-to-Host (H2H):** Direct decentralized communication.
  * **Multi-Host (MH):** Centralized coordination via a tracker server.

## 🛠 Technical Specifications

* **Language:** Java (Multi-threaded)
* **Networking:** Socket Programming (TCP/IP)
* **Architecture:** Hybrid Client-Server / P2P
* **File Handling:** Supports files up to **2047 MB** (32-bit signed integer limitation).
* **Configuration:** Externalized settings via `config/Config.java` and `utils/Logger.java`.

## ⚙️ Configuration

The application is highly customizable before compilation:
* **`config/Config.java`** — global paths, shared directories, timeouts, retry intervals.
* **`utils/Logger.java`** — logging verbosity (Debug/Info/Error).

---

# ⚙️ Building & Running (Maven + JAR)

### **Requirements**
- Java 8+
- Maven 3.6+
- Windows (for test scripts)

### **1. Build the project**

The project generates two independent JARs:

- `torrentServer.jar` — tracker (MH mode)
- `torrentClient.jar` — client (H2H & MH)

Build using Maven:

```bash
mvn clean package
```

Generated files:

```
target/torrentServer.jar
target/torrentClient.jar
```

---

### **2. Running the application**

#### **Tracker (Multi-Host)**

```bash
java -jar target/torrentServer.jar
```

#### **Client (Multi-Host)**

```bash
java -jar target/torrentClient.jar <ID>
```

Example:

```bash
java -jar target/torrentClient.jar 2
```

#### **Client (Host-to-Host)**

Client 1:

```bash
java -jar target/torrentClient.jar 1
```

Client 2 connecting to 1:

```bash
java -jar target/torrentClient.jar 2 1
```

---

# 🧪 Testing (H2H / MH)

The project includes `.bat` scripts to reproduce test scenarios from SKJ labs.

---

## 🔄 Reset test environment

Before each test:

```bat
reset_dirs.bat
```

This script:

- clears directories `TORrent_1`, `TORrent_2`, `TORrent_3`
- restores test files from `files/`

---

## 🧩 Multi-Host (MH) Testing

Start full MH environment:

```bat
run_MH.bat
```

This launches:

1. tracker  
2. client 1  
3. client 2  
4. client 3  

### **Available commands (for client 2)**

| # | Functionality | Command | Description |
|---|---------------|---------|-------------|
| 1 | List files | `list` | Retrieves file list + MD5 checksums |
| 2 | Pull | `pull 1 01.jpg` | Downloads file from client 1 |
| 3 | Push | `push 3 02.jpg` | Uploads file to client 3 |
| 4 | Auto-resume | *(no command)* | Restart client before timeout |
| 5 | Swarming | `multiple_pull 03.jpg` | Downloads file from multiple peers |

### **Testing auto-resume**

Works for:

- `pull`
- `push`
- `multiple_pull`

To test:

1. Start a large transfer  
2. Kill the client  
3. Restart it **before the resume timeout** from `Config.java`

For `multiple_pull`, disconnecting one peer is enough — others finish the job.

---

## 🔗 Host-to-Host (H2H) Testing

Start H2H environment:

```bat
run_H2H.bat
```

This launches:

- client 1  
- client 2 (connected to 1)

### **Available commands**

| # | Functionality | Command |
|---|---------------|---------|
| 1 | List files | `list` |
| 2 | Pull | `pull 04.jpg` |
| 3 | Push | `push 05.jpg` |
| 4 | Auto-resume | *(no command)* |

---

## 🔁 Reset after each test

```bat
reset_dirs.bat
```

---

## 💡 Future Ideas (Optional Enhancements)

- [ ] Transition to 64-bit offsets for files > 2GB.
- [ ] Implement Regex-based API validation for more robust command parsing.
- [ ] Add a graphical dashboard using JavaFX.

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

*Project developed for the Networking course (SKJ) at the Polish-Japanese Academy of Information Technology.*
