# ⚖️ Nyaya Track

> **A Premium Judicial Case Monitoring & Backlog Analysis System built entirely in Core Java.**

![Nyaya Track Logo/Banner](https://via.placeholder.com/800x200.png?text=Nyaya+Track)

Nyaya Track is a professional-grade desktop application designed to streamline the tracking of judicial cases. Its primary purpose is to help legal administrations, judges, and clerks manage court case backlogs efficiently. 

It features a completely custom **Java 2D-rendered UI** (no third-party Look and Feel libraries), robust Role-Based Access Control (RBAC), Multithreading for heavy background tasks, and seamless MySQL database integration.

---

## ✨ Key Features

### 🛡️ Role-Based Access Control (RBAC)
- **Administrator**: Full system access. Manage all cases, configure the system, manage User accounts, oversee system-wide analytics, and perform destructive actions (like case deletion).
- **Judge**: Specialized dashboard. Judges can only view cases explicitly assigned to them, update case statuses, and view personal backlog analytics.
- **Clerk**: Core operational role. Clerks can register new cases, update statuses, assign cases, and export system reports, but cannot add users or delete records.

### 🎨 Premium Custom UI (Java Swing Graphics2D)
- **No External UI Libraries**: The entire user interface, including gradients, hover animations, drop shadows, and pill badges, is built from scratch using pure Java `Graphics2D`.
- **Modern Dashboard Design**: Features a persistent animated sidebar, unified color tokens, and styled responsive tables.
- **Native Data Visualization**: Includes custom-built Pie Charts and Bar Charts rendered natively, eliminating the need for libraries like JFreeChart.

### ⚙️ Core Technical Implementation
- **Strict Java Tech Stack**: Contains 0% HTML/CSS/JS. Strictly relies on Core Java (AWT/Swing) for UI and JDBC for the backend.
- **Multithreaded Architecture**: Utilizes `SwingWorker` and custom `ExecutorService` patterns to ensure the UI remains smooth and responsive during long database calls and file export operations.
- **Object-Oriented Design**: Thorough implementation of Encapsulation, Polymorphism (e.g., UI adapting based on the abstract `User` subclasses), and custom Exception handling.

---

## 🛠️ Technology Stack

- **Frontend**: Java Swing + AWT Graphics2D (Pure Java 11+)
- **Backend**: Core Java 
- **Database**: MySQL 8.x
- **Connectivity**: JDBC (`mysql-connector-j`)

---

## 🚀 Getting Started

### Prerequisites
1. **Java Development Kit (JDK) 11** or higher.
2. **MySQL Server** installed and running.
3. The MySQL JDBC Driver (already included in `lib/` directory or you can source it yourself).

### Database Setup
1. Log into your MySQL instance as root (or an admin user).
2. Execute the provided schema file:
   ```bash
   mysql -u root -p < sql/schema.sql
   ```
   *(This creates the `judicial_system_db`, necessary tables, and inserts sample data)*.
3. Fix the default passwords if needed by running:
   ```bash
   mysql -u root -p < sql/fix_passwords.sql
   ```

### Project Configuration
Update your database credentials in the source code:
1. Open `src/dao/DBConnection.java`
2. Update the `PASSWORD` field to match your local MySQL root password:
   ```java
   private static final String PASSWORD = "YOUR_PASSWORD_HERE";
   ```

### Compilation & Launch
To compile and run the project from the root directory:

```bash
# Compile all Java files into the 'out/' directory
javac -d out -cp "lib/*" src/model/*.java src/exception/*.java src/dao/*.java src/service/*.java src/util/*.java src/thread/*.java src/ui/*.java src/main/*.java

# Run the Application
java -cp "out;lib/*" main.Main
```

---

## 🔐 Default Demo Accounts

If you imported the sample SQL data, the following accounts are available for testing:

| Role | Username | Password |
|------|----------|----------|
| **Administrator** | `admin1` | `admin123` |
| **Judge** | `judge_sharma` | `judge123` |
| **Clerk** | `clerk_raj` | `clerk123` |

---

## 📁 Project Structure

```text
Nyaya Track/
├── lib/                     # JDBC driver (.jar)
├── sql/                     # Database schemas and patches
├── reports/                 # Generated export files (CSV/TXT)
└── src/
    ├── dao/                 # Data Access Objects (JDBC logic)
    ├── exception/           # Custom exception classes
    ├── main/                # Application entry point
    ├── model/               # Core entities (User, Case, Admin, Judge, etc.)
    ├── service/             # Business logic & validation layer
    ├── thread/              # Background Task Handlers
    ├── ui/                  # All custom Swing/Graphics2D components
    └── util/                # Exporters, loggers, and hashing utilities
```

---

## 📜 License
This project was built as an advanced core Java demonstration. Feel free to fork and modify for educational or professional use.
