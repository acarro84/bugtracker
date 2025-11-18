🐞 iWorks Bug Tracker Application

A lightweight internal web application for iWorks employees to submit and manage:

Bug Reports

Feature Requests

General Comments

The system supports file uploads, PostgreSQL persistence, logical delete/archive, admin reporting tools, CSV export, and optional SMTP email notifications.
Intended strictly for internal iWorks use.

📌 Features
✔ Employee Submission Form

Allows employees to submit:

Name

iworkscorp.com–restricted email (validated)

Employee role

Browser selection

Optional event date/time

Report type (Bug / Feature Request / Comment)

Description (255 chars max)

Optional screenshot upload

Screenshots are stored in uploads/.

✔ Backend Services (Spring Boot)

Spring Boot 3.x (Java 17)

REST API architecture

PostgreSQL persistence

Automatic timestamps (created_at)

Screenshot storage → local filesystem

CSV export to reports/

Optional email notifications (SMTP)

✔ Admin Reporting Dashboard

Supports:

🔍 Issue Filtering

By type (Bug / Feature Request / Comment / All)

By resolved state (resolved / unresolved / all)

By date range (created_at)

Ability to view logically deleted items (archive)

📄 Pagination

10 issues per page

Sorted by:

unresolved first (newest → oldest)

then resolved (newest → oldest)

✔ Bulk Resolution Updates

Admins may:

Mark resolved/unresolved

Add Resolved By

Add resolution comments

Automatically sets resolved_at

Un-resolving an issue clears resolution fields

🗄 Logical Delete / Archive

Only allowed on resolved issues

Archived records retain:

deleted flag

deleted_at timestamp

Archived items remain available for admin review

📤 CSV Export

Exports all issues matching filters (ignores pagination).

🏗 Technology Stack
Component	Tech
Backend	Java 17 (Spring Boot 3.x)
Database	PostgreSQL
Build	Maven
UI	Static HTML/CSS/JS
Email	Jakarta Mail (optional)
Storage	Local filesystem (uploads/, reports/)
📂 Project Structure
src/main/java/com/iworks/bugtracker/
├── controller/
├── model/
├── repository/
├── service/
└── config/

src/main/resources/
├── static/
│    ├── bug-report.html
│    ├── admin-reports.html
│    ├── css/
│    └── images/
├── application.properties (Git-ignored)
└── application-example.properties

/uploads/   <- stored screenshots (local)
/reports/   <- CSV export directory (local)

🚀 Getting Started (Local Development)
1. Install Required Software

Java 17+

Maven

PostgreSQL

IntelliJ IDEA (recommended)

2. Clone the Repository
   git clone https://github.com/YOUR_ORG/bugtracker.git
   cd bugtracker

3. Create Your Local Config File

Copy the template:

cp src/main/resources/application-example.properties \
src/main/resources/application.properties


Update:

spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASS

# Optional SMTP
spring.mail.host=
spring.mail.username=
spring.mail.password=

4. Set Up PostgreSQL
   CREATE DATABASE bugtracker;
   GRANT ALL PRIVILEGES ON DATABASE bugtracker TO YOUR_DB_USER;

5. Run the Application

Option A – IntelliJ
Run BugtrackerApplication.

Option B – Maven

mvn spring-boot:run

6. Submission Form

Open:
http://localhost:8080/bug-report.html

Files are saved to:

PostgreSQL DB

/uploads

/reports/bug_reports.csv

🐳 Running with Docker (Recommended for Dev/Demo)

The project includes:

Dockerfile

docker-compose.yml

.dockerignore

1. Build and Run Containers
   docker compose build
   docker compose up


Services:

🐳 bugtracker-app

Spring Boot app

Accessible at http://localhost:8080

🐳 bugtracker-postgres

PostgreSQL 16

Data persisted via named Docker volume

Named Volumes

bugtracker-postgres-data → DB data

bugtracker-uploads → /uploads

bugtracker-reports → /reports

These ensure:

DB persists across restarts

Uploaded screenshots persist

CSV reports persist

2. Stopping Containers
   docker compose down


Remove ALL data (DB + uploads + reports):

docker compose down -v

⚙️ Environment Variables (Docker & Production)
Env Variable	Description
SPRING_DATASOURCE_URL	JDBC DB URL
SPRING_DATASOURCE_USERNAME	DB user
SPRING_DATASOURCE_PASSWORD	DB password
BUGTRACKER_UPLOAD_DIR	Screenshot directory
BUGTRACKER_REPORT_DIR	CSV directory
SPRING_MAIL_HOST	SMTP server
SPRING_MAIL_PORT	SMTP port
SPRING_MAIL_USERNAME	SMTP user
SPRING_MAIL_PASSWORD	SMTP password
BUGTRACKER_MAIL_FROM	Email FROM
BUGTRACKER_MAIL_RECIPIENTS	Notification recipients

🔐 Secrets must not be committed.
Use environment variables, AWS Secrets Manager, or SSM Parameter Store.

🏢 Deployment to AWS (Overview)

The backend cannot be hosted in an S3 bucket.
A proper AWS architecture involves:

Compute Options

EC2 running Docker

ECS (Fargate) — best container-native option

Elastic Beanstalk — simplest managed deployment

EKS — use only if Kubernetes is standard internally

Database

Amazon RDS PostgreSQL (recommended)

or Aurora PostgreSQL

Storage

Option A: Docker/EBS volume (current behavior)

Option B (future): Uploads & reports stored in Amazon S3

Code can be upgraded to support this easily

SMTP

Requires outbound access to:

Office 365 SMTP
or

Internal relay server

🔒 Security Notes

Internal iWorks use only

Email restricted to @iworkscorp.com

application.properties is Git-ignored

Secrets handled via environment variables

Docker image runs as a non-root user

Logical deletion implemented for safe archival

🧹 Future Enhancements / Roadmap
🎯 Confirmed Future Enhancements

Text search across description/resolution fields

Visualization tools (charts, analytics, weekly stats)

Improved email notification templates

Enhanced admin permissions / role-based access

Multi-file uploads (screenshots + attachments)

💡 Possible Enhancements

Migrate screenshot storage to Amazon S3

Add Excel (.xlsx) export

Add API tokens for integrations/automation

Add system-wide audit logging

📜 License

Internal iWorks use only.
All rights reserved.