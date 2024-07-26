<img width="659" alt="Captura de ecrã 2024-07-26, às 08 58 51" src="https://github.com/user-attachments/assets/27a34fbf-7b23-4b93-90c7-9062c0cfe4e6"># FP-AOR-BACKEND

FPBackend is a robust backend application developed in Java, designed to provide an efficient API for managing data and processes within an enterprise application. This project utilizes a combination of modern technologies and frameworks, including Jakarta EE for backend API, Hibernate ORM for data persistence, and JWT for authentication and authorization.

Table of Contents

Description
Requirements
Installation
Configuration
Usage
Testing
Dependencies
Project Configuration
Deployment
Contributing
License

## Description
The "Innovation Lab Management" project aims to develop a comprehensive backend solution for managing various aspects of an innovation lab. This project was executed as part of the 11th edition of the "Acertar o Rumo" program at the University of Coimbra, with Critical Software sponsoring and guiding the development.

The backend of this project is designed to support a robust web application for the innovation lab. It handles core functionalities such as user management, authentication, data processing, and integration with external systems. The backend system is built using modern technologies and frameworks to ensure scalability, security, and performance.

This documentation provides a detailed overview of the backend development process, including the technologies used, the architectural design, and the implementation strategies. It also covers the challenges faced during development and the solutions implemented to address them.

Key aspects documented in this report include:

Technologies and Architecture: A description of the technologies employed (e.g., Java, WildFly, Hibernate) and the architectural decisions made to structure the backend system.
Implementation Details: Insights into the development process, including code structure, data models, and integration points.
Challenges and Solutions: An analysis of the difficulties encountered and the approaches taken to resolve them.
This comprehensive documentation aims to provide a clear understanding of the backend system's design and functionality, ensuring that all critical components are well-defined and effectively integrated into the overall project.

## Requirements

Software Requirements
Java Development Kit (JDK):
Version: 11 or later
Description: The backend is built using Java. Ensure that JDK 11 or a later version is installed to compile and run the application.
Apache Maven:
Version: 3.6.0 or later
Description: Maven is used for project management and build automation. Ensure that Maven is installed to manage dependencies and build the project.
WildFly Application Server:
Version: 26 or later
Description: WildFly is used as the Java EE application server. Ensure that WildFly is configured for HTTPS and ready for deployment.
MySQL Database:
Version: 8.0 or later
Description: MySQL is used as the database management system. Ensure that MySQL is installed and properly configured.
Database Connector:
Dependency: mysql-connector-j
Version: 8.3.0
Description: Required for connecting the backend to the MySQL database.
Build Tools:
Tools: Git, IDE (e.g., IntelliJ IDEA, Eclipse)
Description: Git is needed for version control, and an IDE is recommended for development.
Hardware Requirements
Configuration Requirements
HTTPS Configuration:
Description: WildFly must be configured to support HTTPS. This involves creating and installing SSL/TLS certificates.
Database Configuration:
Description: The MySQL database must be set up with the appropriate schema and user permissions. Ensure that the database name, username, and password are correctly configured in the application properties.
Environment Variables:
Description: Set environment variables for sensitive information such as database credentials and API keys.

## Installation

To set up the FPBackend project, follow these steps:

Clone the Repository
Start by cloning the project repository from your version control system. This will download the project files to your local machine.
Install Dependencies
Navigate to the project directory where you have cloned the repository.
Use a build tool like Apache Maven to install the necessary project dependencies. This will ensure that all required libraries and frameworks are available for your project.
Deploy the Application
Deploy the application to the JBoss WildFly server. Ensure that JBoss WildFly is properly set up and running before deploying. The deployment process will package the application and make it available for use on the server.
Run the Application
Start the WildFly server if it's not already running. This server will host your deployed application.
Configuration

Before running the application, you need to configure the following:

Clone and Install Dependencies
Follow the Installation steps to clone the repository and install the dependencies.
Configure WildFly for HTTPS
Ensure that JBoss WildFly is configured to use HTTPS. You need to modify the server configuration file (typically found in the standalone/configuration directory) to include an HTTPS listener. This involves specifying a port for HTTPS and providing the necessary SSL certificates.
Database Configuration
Configure the database settings to connect to your MySQL database. Update the database connection properties in the project's configuration file, typically located in src/main/resources/application.properties. Make sure to set the correct database URL, username, and password.

JBoss Plugin Configuration
Ensure that the JBoss Maven plugin settings are correctly configured in the pom.xml file. This includes specifying the appropriate port for HTTPS, as well as providing the necessary credentials and deployment file name.

To secure your WildFly server with HTTPS, you need an SSL certificate. For development purposes, you can create a self-signed certificate.


## Usage

To use the FPBackend application, follow these instructions:

Start the Server
Ensure that the JBoss WildFly server is running and that the application has been successfully deployed. This server must be up and running to host your application.
Access the API
You can access the application's API at the specified URL. For HTTPS, it is typically accessible at https://localhost:8443/FPBackend/api. Use a REST client or browser to interact with the API endpoints.

## Testing
Run the unit tests to verify that the application functions as expected. Ensure that all tests pass to confirm the correctness of your application.

## Entity-Relationship Diagram

![ER Diagram](path/to/your/er-diagram.png)
