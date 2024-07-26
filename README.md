# FP-AOR-BACKEND

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



Installation

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


Usage

To use the FPBackend application, follow these instructions:

Start the Server
Ensure that the JBoss WildFly server is running and that the application has been successfully deployed. This server must be up and running to host your application.
Access the API
You can access the application's API at the specified URL. For HTTPS, it is typically accessible at https://localhost:8443/FPBackend/api. Use a REST client or browser to interact with the API endpoints.

Testing
Run the unit tests to verify that the application functions as expected. Ensure that all tests pass to confirm the correctness of your application.
