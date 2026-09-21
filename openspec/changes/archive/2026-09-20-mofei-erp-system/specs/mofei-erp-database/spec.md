# Spec Delta

## Purpose

Defines the MSSQL database schema, table structures, and entity-field naming consistency for the MOFEI ERP system.

## ADDED Requirements

### Requirement: Database and Table Creation
The system SHALL initialize and maintain a dedicated MSSQL database named `MOFEI` with five core relational tables: Employee (員工主檔), Product (產品主檔), Customer (客戶主檔), Order (訂單主檔), and OrderDetail (訂單明細).

#### Scenario: Database initialization
- **WHEN** the database initialization script runs on the target MSSQL server
- **THEN** the database `MOFEI` is created with tables `employee`, `product`, `customer`, `orders`, and `order_detail` properly structured with primary keys and constraints

### Requirement: Field Naming Consistency
The system SHALL strictly maintain 100% consistency across database column names, backend Entity/DTO properties, and frontend JSON/view field keys.

#### Scenario: Data serialization across layers
- **WHEN** master data records are queried from the database and transmitted to the frontend
- **THEN** the field names in the database, backend Java models, and frontend rendered attributes match identically without transformation discrepancies

### Requirement: Referential Integrity and Employee Deletion Protection
The system SHALL enforce foreign key relationships between orders and master data, and SHALL prevent cascading deletion breaks by supporting employment status control (soft delete / status toggle) for employees.

#### Scenario: Attempting to delete an employee associated with historical orders
- **WHEN** an administrator initiates a deletion on an employee who has associated order records
- **THEN** the system updates the employee's status to inactive (resigned) rather than executing a hard delete, preserving historical order integrity
