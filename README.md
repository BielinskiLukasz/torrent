# Torrent P2P File Transfer (TCP Implementation)

![Status](https://img.shields.io/badge/status-finished-brightgreen)
![Version](https://img.shields.io/badge/version-1.0-blue)

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Protocol](https://img.shields.io/badge/Protocol-TCP-blue?style=for-the-badge)

A robust Peer-to-Peer (P2P) file-sharing application implemented in Java, utilizing the **TCP protocol** for reliable data transmission. Developed as a high-performance networking project for **PJATK**, it features both direct Host-to-Host (H2H) and Multi-Host (MH) coordination models.

## 📌 Project Status

This project is **feature-complete** and considered **finished**.  
The application is stable, fully functional, and ready for use in both H2H and MH modes.

Further improvements listed below are optional ideas for future development.

## 🚀 Key Features

*   **Reliable Data Transfer:** Built entirely on TCP to guarantee packet delivery and data integrity.
*   **Connection Resilience (Auto-Resume):** Intelligent mechanism that automatically attempts to resume interrupted transfers if a peer reconnects within a configurable grace period.
*   **Multi-Source Swarming:** The `multiple_pull` feature allows segments of a single file to be downloaded from multiple peers simultaneously, optimizing bandwidth.
*   **Integrity Verification:** Uses checksums to validate files during listing and after transfer.
*   **Flexible Architecture:**
    *   **Host-to-Host (H2H):** Decentralized direct connection between two peers.
    *   **Multi-Host (MH):** Centralized coordination via a tracker server for managing multiple clients.

## 🛠 Technical Specifications

*   **Language:** Java (Multi-threaded)
*   **Networking:** Socket Programming (TCP/IP)
*   **Architecture:** Hybrid Client-Server / P2P
*   **File Handling:** Supports files up to **2047 MB** (32-bit signed integer limitation).
*   **Configuration:** Externalized settings via `config/Config.java` and `utils/Logger.java`.

## ⚙️ Configuration

The application is highly customizable before compilation:
*   **`config/Config.java`**: Adjust global paths, default shared directories, connection timeouts, and retry intervals.
*   **`utils/Logger.java`**: Fine-tune logging verbosity (Debug/Info/Error) for console output.

## 💻 Usage & API

### Running the Application

1.  **Tracker Server:** Run the main server class without arguments to start the coordination node.
2.  **Multi-Host Client:** Start with a unique ID: `java Client [ID]`.
3.  **Host-to-Host Mode:**
    *   First Peer: `java Client [ID]`
    *   Second Peer: `java Client [ID] [First_Peer_ID]`

### Command Reference

| Command | Description | Example |
| :--- | :--- | :--- |
| `list` | Lists all shared files on the network with owner IDs and checksums. | `list` |
| `pull` | Downloads a file from a specific client with auto-resume support. | `pull 2 document.pdf` |
| `push` | Uploads a local file to a specified remote peer. | `push 2 image.png` |
| `multiple_pull` | Aggregates file segments from all available peers sharing the file. | `multiple_pull dataset.zip` |
| `exit` | Gracefully disconnects and stops sharing files. | `exit` |

> **Note:** Wrap filenames containing spaces in quotes. The character `*` is reserved and cannot be used in filenames.

## 💡 Future Ideas (Optional Enhancements)

- [ ] Transition to 64-bit offsets for files > 2GB.
- [ ] Implement Regex-based API validation for more robust command parsing.
- [ ] Add a graphical dashboard using JavaFX.

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---
*Project developed for the Networking course (SKJ) at the Polish-Japanese Academy of Information Technology.*
