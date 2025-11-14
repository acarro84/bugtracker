🐞 iWorks Bug Tracker Application

A lightweight internal web application for iWorks employees to submit:

Bug reports

Feature requests

General comments

The system supports file uploads, PostgreSQL storage, CSV export, and automated email notifications (optional). Intended for internal use only.

📌 Features
✔ Bug/Feature/Comment submission form

Employee name

iworkscorp.com–restricted email

Role

Browser selection

Optional date/time of event

Description (255 chars max)

Screenshot upload

Inline validation

✔ Backend Services

Spring Boot REST API

Stores reports in PostgreSQL

Saves uploaded screenshots to /uploads

Appends to a CSV file at /reports/bug_reports.csv

Timestamped created_at automatically

Email notifications (optional—requires SMTP credentials)

🏗 Technology Stack
Component	Tech
Backend	Java 17+ (Spring Boot 3.x)
Build	Maven
Database	PostgreSQL
Frontend Form	HTML/CSS/JS served from Spring Boot
Storage	Local folders (uploads/, reports/)
Email	Jakarta Mail (SMTP)
📂 Project Structure
src/main/java/com/iworks/bugtracker/
controller/
model/
repository/
service/
config/

src/main/resources/
static/
bug-report.html
css/
images/
application.properties (ignored in Git)
application-example.properties

uploads/          (generated at runtime)
reports/          (CSV written here)

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

Copy the example template:

cp src/main/resources/application-example.properties \
src/main/resources/application.properties


Then update:

spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASS
spring.mail.* (optional)

4. Set Up PostgreSQL

Log into PostgreSQL:

psql -U postgres


Create the database:

CREATE DATABASE bugtracker;


Grant permissions (optional):

GRANT ALL PRIVILEGES ON DATABASE bugtracker TO YOUR_DB_USER;

5. Start the Application
   mvn spring-boot:run


Or from IntelliJ:
Run → BugtrackerApplication

6. Use the Form

Open:

http://localhost:8080/bug-report.html


Submit a report → it will appear in:

PostgreSQL database

/uploads (any screenshots)

/reports/bug_reports.csv

📧 Email Notifications (Optional)

Email sending is controlled by:

spring.mail.host=
spring.mail.username=
spring.mail.password=


Your IT team must provide SMTP credentials. Once enabled, each submission triggers an email to:

bugtracker.notification.email

🔒 Security Notes

This app is intended for internal network use only

Emails must end in @iworkscorp.com

Do NOT commit real passwords — Git ignores application.properties

Use environment variables or keep credentials local

📜 License

Internal iWorks use only. All rights reserved.

👩‍💻 Development Roadmap

Future enhancements may include:

Admin dashboard to view/search reports

Authentication (SSO or company login)

Export to Excel feature

API endpoints for pulling analytics

Docker support