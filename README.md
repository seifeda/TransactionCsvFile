# Transaction CSV Upload API

This Spring Boot application reads transaction records from a CSV file and saves them into an Oracle database.

## Features
- Upload CSV file via REST API
- Validate and parse transactions
- Save valid transactions to Oracle DB

## Technologies Used
- Java 17
- Spring Boot
- Oracle Database
- Maven

## Usage
1. Start the application
2. Send a POST request to `/api/upload` with your CSV file
3. Records will be stored in the database

## Configuration
Database credentials are stored in a `.env` file:


