-- ============================================================
--  Judicial Case Monitoring and Backlog Analysis System
--  MySQL Schema — judicial_system_db
-- ============================================================
 
CREATE DATABASE IF NOT EXISTS judicial_system_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
 
USE judicial_system_db;
 
-- ── TABLE: users ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    user_id       INT           NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)   NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    role          ENUM('Admin','Judge','Clerk') NOT NULL,
    extra_info    VARCHAR(100),         -- Judge: specialization, Clerk: court_id
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    PRIMARY KEY (user_id)
) ENGINE=InnoDB;
 
-- ── TABLE: courts ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS courts (
    court_id      INT          NOT NULL AUTO_INCREMENT,
    court_name    VARCHAR(100) NOT NULL,
    location      VARCHAR(100),
    chief_judge_id INT,
    PRIMARY KEY (court_id),
    CONSTRAINT fk_court_judge
        FOREIGN KEY (chief_judge_id) REFERENCES users(user_id)
        ON DELETE SET NULL
) ENGINE=InnoDB;
 
-- ── TABLE: cases ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cases (
    case_id            INT          NOT NULL AUTO_INCREMENT,
    case_number        VARCHAR(50)  NOT NULL UNIQUE,
    title              VARCHAR(255) NOT NULL,
    case_type          VARCHAR(50)  NOT NULL,   -- Civil/Criminal/Family/Property
    petitioner         VARCHAR(100) NOT NULL,
    respondent         VARCHAR(100) NOT NULL,
    filing_date        DATE         NOT NULL,
    hearing_date       DATE,
    resolution_date    DATE,
    status             ENUM('Pending','In Progress','Resolved','Dismissed')
                           NOT NULL DEFAULT 'Pending',
    assigned_judge_id  INT,
    court_id           INT,
    description        TEXT,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (case_id),
    CONSTRAINT fk_case_judge
        FOREIGN KEY (assigned_judge_id) REFERENCES users(user_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_case_court
        FOREIGN KEY (court_id) REFERENCES courts(court_id)
        ON DELETE SET NULL
) ENGINE=InnoDB;
 
-- ── TABLE: case_history ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS case_history (
    history_id  INT       NOT NULL AUTO_INCREMENT,
    case_id     INT       NOT NULL,
    updated_by  INT       NOT NULL,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    old_status  VARCHAR(50),
    new_status  VARCHAR(50),
    remarks     TEXT,
    PRIMARY KEY (history_id),
    CONSTRAINT fk_history_case
        FOREIGN KEY (case_id) REFERENCES cases(case_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_history_user
        FOREIGN KEY (updated_by) REFERENCES users(user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;
 
-- ── INDEXES for analytics queries ─────────────────────────────
CREATE INDEX idx_cases_status      ON cases (status);
CREATE INDEX idx_cases_judge       ON cases (assigned_judge_id);
CREATE INDEX idx_cases_filing_date ON cases (filing_date);
CREATE INDEX idx_history_case      ON case_history (case_id);
 
-- ============================================================
--  SAMPLE DATA
-- ============================================================
 
-- Users (passwords are SHA-256 hashes of: admin123, judge123, clerk123)
INSERT INTO users (username, password_hash, role, extra_info) VALUES
('admin1',  '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
    'Admin', NULL),
('judge_sharma',
    '94358d5abe1d055c1ced4403bb0e397edf8c905b33e03b34ad1b1d3adf2d9cf4',
    'Judge', 'Criminal'),
('judge_patel',
    '94358d5abe1d055c1ced4403bb0e397edf8c905b33e03b34ad1b1d3adf2d9cf4',
    'Judge', 'Civil'),
('clerk_raj',
    'a3630b8b8f6c82d33b0695f77f915e69ed7b0c5214062f8b870219845e069d30',
    'Clerk', '1');
 
-- Courts
INSERT INTO courts (court_name, location, chief_judge_id) VALUES
('District Court Mumbai',     'Mumbai, Maharashtra',   2),
('Sessions Court Pune',       'Pune, Maharashtra',     3),
('Family Court Nagpur',       'Nagpur, Maharashtra',   NULL);
 
-- Cases
INSERT INTO cases (case_number, title, case_type, petitioner, respondent,
                   filing_date, status, assigned_judge_id, court_id, description)
VALUES
('CRM/2021/001', 'State vs Ramesh Kumar',      'Criminal','State of MH',
    'Ramesh Kumar',   '2021-03-15', 'In Progress', 2, 1,
    'IPC Section 302 - Murder case.'),
('CIV/2022/045', 'Property Dispute - Mehta',   'Civil',   'Suresh Mehta',
    'Rekha Mehta',    '2022-07-01', 'Pending',     3, 2,
    'Dispute over ancestral property.'),
('FAM/2023/012', 'Divorce Petition - Sharma',  'Family',  'Priya Sharma',
    'Anil Sharma',    '2023-01-20', 'Pending',     NULL, 3,
    'Mutual consent divorce petition.'),
('CRM/2019/088', 'State vs Bhatt Brothers',    'Criminal','State of MH',
    'Vijay Bhatt',    '2019-08-10', 'In Progress', 2, 1,
    'IPC 420 - Fraud case. Long pending.'),
('CIV/2024/003', 'Land Acquisition Appeal',    'Property','Govt of MH',
    'Farmer Collective','2024-02-28','Pending',    3, 2,
    'Appeal against land acquisition order.'),
('CRM/2022/200', 'Theft Case - Anand Nagar',   'Criminal','State of MH',
    'Unknown',        '2022-11-05', 'Resolved',   2, 1,
    'Case resolved. Accused convicted.'),
('CIV/2020/067', 'Contract Breach - TechCorp', 'Civil',   'TechCorp Ltd',
    'Infra Solutions','2020-04-18', 'In Progress', 3, 2,
    'Commercial contract dispute.');
