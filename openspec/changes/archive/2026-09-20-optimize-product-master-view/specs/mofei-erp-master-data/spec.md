# Spec Delta

## MODIFIED Requirements

### Requirement: Four Master Data CRUD Operations
The system SHALL provide comprehensive CRUD (Create, Read, Update, Delete) RESTful endpoints and UI views for Employee (員工), Product (產品), Customer (客戶), and Order (訂單與訂單明細). Specifically for Employee creation, the employee ID SHALL be allocated by the system without user input, and the system SHALL validate that employee ID, phone, and email are unique before persisting. Specifically for Product creation, the product ID SHALL be allocated by the system without user input. For both Product creation and modification, the product price SHALL be an integer unit (大於或等於 0 的整數), the person in charge (employee ID) SHALL provide a dropdown menu synchronized with employee master data, and when validation fails, the system SHALL explicitly inform the user which specific field contains an error.

#### Scenario: Creating a master data entry
- **WHEN** an authorized user submits valid master data details via the UI creation form
- **THEN** the system validates parameters, persists the new record into the database, and returns the created entity

#### Scenario: Updating a master data entry
- **WHEN** an authorized user submits modifications for an existing record
- **THEN** the system applies the updates to the corresponding database record and reflects the new state in the view

#### Scenario: Creating an employee with system-allocated ID and uniqueness validation
- **WHEN** an authorized user submits an employee creation request
- **THEN** the system automatically generates an employee ID without accepting user input, validates that the employee ID, phone number, and email are unique, and persists the record

#### Scenario: Rejecting employee creation on duplicate phone or email
- **WHEN** an authorized user attempts to create an employee with an existing phone number or email address
- **THEN** the system rejects the request with an error message indicating duplication

#### Scenario: Updating an employee with uniqueness validation
- **WHEN** an authorized user submits modifications for an existing employee keeping their own phone and email or providing new unique values
- **THEN** the system applies the updates and persists the record

#### Scenario: Rejecting employee modification on duplicate employee ID, phone, or email
- **WHEN** an authorized user attempts to update an employee with a phone number, email address, or employee ID that already belongs to another employee
- **THEN** the system rejects the request with a 400 error message indicating duplication (e.g. "員工編號已存在", "員工電話已存在", or "電子郵件已存在")

#### Scenario: Creating a product with system-allocated ID, integer price, and employee selection
- **WHEN** an authorized user submits a product creation request with integer price and a selected employee from the synced dropdown
- **THEN** the system automatically generates a unique product ID without accepting user input, validates that the price is an integer greater than or equal to 0, verifies the employee exists, and persists the record

#### Scenario: Updating a product with integer price, employee selection, and read-only product ID
- **WHEN** an authorized user submits product modifications with an integer price and a selected employee
- **THEN** the system keeps the product ID unchanged, validates the integer price and employee existence, and updates the record

#### Scenario: Rejecting product creation or update with detailed field error message
- **WHEN** an authorized user attempts to create or update a product with invalid input (e.g., negative or non-integer price, empty product name, or non-existent employee ID)
- **THEN** the system rejects the request and returns an error message explicitly stating which field has an invalid input

## ADDED Requirements

### Requirement: Product Module Presentation and Ordering
The system SHALL display the Product master data list sorted by product ID in ascending order (由小到大依序排列).

#### Scenario: Displaying product table with ascending order
- **WHEN** an authorized user navigates to the product master data page (http://localhost:8080/product)
- **THEN** the table displays records sorted by product ID in ascending order
