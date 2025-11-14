CREATE TABLE bug_reports (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    role VARCHAR(100) NOT NULL,
    browser VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    screenshot_path VARCHAR(500)
);
