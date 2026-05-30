# BilUthyrning
BilUthyrningssystem System Summary
Java Code Architecture
Main Components:

CarRentalGUI.java - JavaFX application entry point, initializes Spring context and builds UI
Model Layer - JPA entities: Car and Booking with proper relationships
Service Layer - Business logic: CarService and BookingService handle operations
Repository Layer - Spring Data JPA repositories for database access
API Layer - REST controllers: BookingController and CarController for HTTP endpoints
DataInitializer - CommandLineRunner that populates initial data
Key Features:

Car selection with pricing calculation
Booking creation with date validation
Booking cancellation
Conflict detection for overlapping bookings
Price calculation based on daily rates and rental duration
Database Connection
Technology Stack:

Database: SQLite (file-based, lightweight)
ORM: Hibernate/JPA with Spring Data JPA
Dialect: org.hibernate.community.dialect.SQLiteDialect
Connection: JDBC via sqlite-jdbc driver
Configuration (application.properties):

spring.datasource.url=jdbc:sqlite:biltuthyrning.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update
Schema:

cars table: id, model, year, engine, daily_rate
bookings table: id, car_id, customer_name, start_date, end_date, total_price, status
Foreign key relationship: bookings.car_id → cars.id
SQLite-Specific Configurations:

Disabled batching and fetch optimization
Disabled getGeneratedKeys (workaround for SQLite limitations)
Used IDENTITY strategy for auto-increment IDs
Modern REST Services
REST API Endpoints (BookingController):

GET /api/bookings

Returns all bookings
GET /api/bookings/{id}

Returns specific booking by ID
POST /api/bookings

Creates new booking
Request body: BookingRequest with carId, customerName, startDate, endDate
Returns created booking
DELETE /api/bookings/{id}

Cancels booking by ID
GET /api/bookings/cars/{carId}/availability

Checks car availability for date range
Query params: start, end (ISO date format)
Returns JSON with availability status
Modern Features:

Spring Boot 3.x with Jakarta EE
RESTful API design
JSON serialization/deserialization
Date formatting with ISO standards
Exception handling with proper HTTP status codes
Integration:

GUI uses service layer directly
REST API provides alternative access for web/mobile clients
Both share same business logic and data access layers
This is a modern, layered architecture following Spring Boot best practices with separation of concerns and multiple access patterns (GUI + REST API).
