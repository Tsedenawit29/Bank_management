# Bank_management
Banking Management System
A robust and secure backend application for managing banking operations, including user accounts, deposits, withdrawals, and transfers. This system is built with Spring Boot and utilizes PostgreSQL as its database, containerized with Docker for easy setup and deployment.

✨ Features
User & Account Management: Secure user registration, account creation (Checking, Savings), and staff approval for pending accounts.

Financial Operations: Seamless deposits, withdrawals (with insufficient funds checks), and atomic transfers between accounts.

Account Status: Functionality to freeze and unfreeze accounts.

Transaction History: Comprehensive logging and retrieval of transaction details, with filtering options.

Data Integrity: Ensures unique account numbers and transaction reference IDs.

Robust Error Handling: Custom exceptions for various banking scenarios (e.g., insufficient funds, frozen accounts).

🚀 Technologies Used
Backend: Java 17+ with Spring Boot (3.x)

Database: PostgreSQL (managed via Docker)

ORM: Spring Data JPA / Hibernate

Utilities: Lombok, java.time API, UUID for unique identifiers.

🛠️ Setup Instructions
To get this project running locally, ensure you have Java Development Kit (JDK) 17+, Maven, and Docker & Docker Compose installed.

Clone the Repository:
Navigate to your desired directory and clone the project.

Database Setup:
The project uses PostgreSQL, configured via a docker-compose.yml file. Start the database container using docker-compose up -d. This will set up a bank_db database with default credentials.

Configure Spring Boot:
Update src/main/resources/application.properties with your database connection details (matching the Docker Compose setup). Ensure spring.jpa.hibernate.ddl-auto is set appropriately for schema management.

Build and Run:
From the project root, build the application with mvn clean install and then run it using mvn spring-boot:run. The application will start on http://localhost:8080.

💡 API Endpoints (Examples)
The application exposes RESTful APIs for banking operations:

Account Creation: POST /api/accounts/create/{userId}

Deposit Funds: POST /api/accounts/deposit

Withdraw Funds: POST /api/accounts/withdraw

Transfer Funds: POST /api/accounts/transfer

Get Account Details: GET /api/accounts/details/{username}

Get Transaction History: GET /api/transactions/user/{username} (with optional date filters)

Account Actions: PUT /api/accounts/approve/{accountId}, PUT /api/accounts/freeze/{accountId}

🤝 Contributing
Contributions, bug reports, and feature suggestions are highly encouraged. Please fork the repository, create a new branch, make your changes, and open a Pull Request.
