# Spec Delta: mofei-erp-master-data

## MODIFIED Requirements

### Requirement: Four Master Data CRUD Operations
The system SHALL provide comprehensive CRUD (Create, Read, Update, Delete) RESTful endpoints and UI views for Employee (員工), Product (產品), Customer (客戶), and Order (訂單與訂單明細). Specifically for Employee creation, the employee ID SHALL be allocated by the system without user input, and the system SHALL validate that employee ID, phone, and email are unique before persisting.

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

## ADDED Requirements

### Requirement: Employee Module Presentation and Ordering
The system SHALL display the Employee master data list sorted by employee ID in ascending order (由小到大) and render the system role as plain text without web badge tags.

#### Scenario: Displaying employee table with ascending order and plain role text
- **WHEN** an authorized user navigates to the employee master data page
- **THEN** the table displays records sorted by employee ID in ascending order, and the system role column renders plain text instead of `<span class="badge">` tags
