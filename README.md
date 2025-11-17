🐞 iWorks Bug Tracker Application

A lightweight internal web application for iWorks employees to submit and manage:

Bug reports

Feature requests

General comments

The system supports file uploads, PostgreSQL persistence, admin reporting tools, CSV export, and (optional) automated email notifications. Intended strictly for internal iWorks use.

📌 Features
✔ Employee Submission Form

The bug-report form allows employees to submit:

Name

iworkscorp.com–restricted email (validated)

Employee role

Browser selection

Optional event date/time

Report type: Bug, Feature Request, Comment

Description (255 chars max)

Optional screenshot upload

All fields include inline validation.
Screenshots are stored in /uploads/.

✔ Backend Services (Spring Boot)

REST API built with Spring Boot 3.x

Data stored in PostgreSQL

Automatic timestamping (created_at)

Screenshot storage (local filesystem)

CSV export

Optional email notifications (if SMTP credentials provided)

✔ Admin / Reporting Page (New)

A secure internal admin endpoint enables:

🔍 Issue Filtering

By issue type (Bug / Feature Request / Comment / All)

By resolved state (resolved, unresolved, all)

By date range (created_at)

📄 Pagination

10 issues per page

Sorted with:

unresolved issues first (newest to oldest)

resolved issues next (newest to oldest)

✔ Bulk Resolution Updates

Admins can:

Mark issues as resolved/unresolved

Provide:

Resolved By

Resolution Description

Resolution timestamp (resolved_at) saved automatically

Un-resolving an issue:

Clears resolution fields

Prompts the user to confirm (handled in UI)

📤 CSV Export

Exports all filtered issues (ignoring pagination) to downloadable CSV.

🏗 Technology Stack
Component	Tech
Backend	Java 17+ (Spring Boot 3.x)
Database	PostgreSQL
Build	Maven
UI	Static HTML/CSS/JS served from Spring Boot
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
│    ├── admin.html      (future UI)
│    ├── css/
│    └── images/
├── application.properties (ignored in Git)
└── application-example.properties

/uploads/   ← stored screenshots  
/reports/   ← CSV export directory

🚀 Getting Started
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

# Optional email settings
spring.mail.host=
spring.mail.username=
spring.mail.password=

4. Set Up PostgreSQL
   CREATE DATABASE bugtracker;
   GRANT ALL PRIVILEGES ON DATABASE bugtracker TO YOUR_DB_USER;

5. Start the Application
   mvn spring-boot:run


Or in IntelliJ → Run BugtrackerApplication

6. Use the Employee Submission Form

Navigate to:

http://localhost:8080/bug-report.html


Reported issues will appear in:

PostgreSQL database

/uploads/ (screenshots)

/reports/bug_reports.csv

📊 Admin Reporting Tools (New)

Admin API endpoints:

Endpoint	Description
GET /api/admin/issues	Search/filter/paginate issues
POST /api/admin/issues/bulk-update	Apply resolution updates
GET /api/admin/issues/export	CSV export of all matching issues

A future HTML UI (admin.html) will provide a full dashboard.

📧 Email Notifications (Optional)

Configure SMTP:

spring.mail.host=
spring.mail.username=
spring.mail.password=
bugtracker.notification.email=alerts@iworkscorp.com


The app will send an email when new reports are submitted.

🔒 Security Notes

Application intended for internal iWorks network only

Email must end with @iworkscorp.com

application.properties is Git-ignored — credentials are never committed

For production, environment variables or secrets manager recommended

🧹 Future Enhancements / Roadmap
🎯 Confirmed Upcoming Features

Full HTML Admin Dashboard

Text search for description & resolution fields

Logical delete / archive functionality

Visualization tools (charts, weekly stats)

Role-based access control for admins

Refined email templates

💡 Possible Enhancements

Docker support

S3 or secure cloud storage for screenshots

Excel (.xlsx) export

API tokens for secure automation

📜 License

Internal iWorks use only.
All rights reserved.