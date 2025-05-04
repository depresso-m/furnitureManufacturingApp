# Furniture Manufacturing Desktop Application

A Java Swing application for furniture manufacturing management with MongoDB integration.

## Features

- Customer management
- Order management
- Furniture catalog
- Materials management
- Furniture details tracking
- Database connectivity with MongoDB in Docker

## Requirements

- Java 11 or higher
- Maven
- Docker and Docker Compose

## Setup

### 1. Start MongoDB

```bash
docker-compose up -d
```

This will start a MongoDB instance on port 27017.

### 2. Build the application

```bash
mvn clean package
```

### 3. Run the application

```bash
java -jar target/furnitureManufacturingApp-1.0-SNAPSHOT.jar
```

## Database Structure

The application uses the following MongoDB collections:

- **customers**: Information about clients
- **orders**: Client orders with many-to-one relationship to customers
- **furniture**: Furniture catalog
- **materials**: Raw materials for production
- **furnitureDetails**: Additional details about furniture with one-to-one relationship to furniture

## Usage

The application provides a simple user interface with tabs for each entity type. In each tab, you can:

- View all entities in a table
- Add new entities
- Delete entities
- Search for entities based on criteria
- Sort entities based on different fields

For orders and furniture details, there are additional features to manage the relationship between entities.

## Docker

The included `docker-compose.yml` file sets up a MongoDB instance with appropriate volume mappings for data persistence. 