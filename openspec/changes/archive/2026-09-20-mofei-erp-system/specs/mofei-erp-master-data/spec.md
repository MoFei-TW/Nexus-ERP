# Spec Delta

## Purpose

Defines CRUD operations, querying, pagination, and data masking for the four master data modules (Employee, Product, Customer, Order).

## ADDED Requirements

### Requirement: Four Master Data CRUD Operations
The system SHALL provide comprehensive CRUD (Create, Read, Update, Delete) RESTful endpoints and UI views for Employee (員工), Product (產品), Customer (客戶), and Order (訂單與訂單明細).

#### Scenario: Creating a master data entry
- **WHEN** an authorized user submits valid master data details via the UI creation form
- **THEN** the system validates parameters, persists the new record into the database, and returns the created entity

#### Scenario: Updating a master data entry
- **WHEN** an authorized user submits modifications for an existing record
- **THEN** the system applies the updates to the corresponding database record and reflects the new state in the view

### Requirement: Composite and Fuzzy Search
The system SHALL provide composite search filtering by master data ID (編號) and Name (名稱), as well as real-time client-side and server-side fuzzy search on text inputs.

#### Scenario: Searching by composite criteria
- **WHEN** a user enters an ID and a partial Name into the search filter
- **THEN** the system returns only records that match both the specified ID and the name pattern

#### Scenario: Real-time fuzzy text search
- **WHEN** a user types characters into the fuzzy search input box
- **THEN** the table dynamically filters and displays matching records matching any relevant text attribute

### Requirement: Standard Pagination Mechanism
The system SHALL display master data records in paginated tables fixed at 10 items per page, providing previous and next page navigation controls.

#### Scenario: Navigating pages
- **WHEN** a dataset contains more than 10 records and the user clicks the "Next Page" button
- **THEN** the view updates to display the subsequent 10 records along with current page indicator information

### Requirement: Sensitive Data Masking and Filtering
The system SHALL automatically filter and conceal sensitive fields (such as passwords, salt hashes, and private tokens) from all DTO responses and frontend table renderings.

#### Scenario: Rendering employee records
- **WHEN** an employee list is fetched and displayed in the frontend view
- **THEN** all non-sensitive fields are displayed, while password and salt fields are completely omitted from the payload and DOM
