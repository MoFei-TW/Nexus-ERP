# Spec Delta

## MODIFIED Requirements

### Requirement: Four Master Data CRUD Operations
The system SHALL provide comprehensive CRUD (Create, Read, Update, Delete) RESTful endpoints and UI views for Employee (員工), Product (產品), Customer (客戶), and Order (訂單與訂單明細). Specifically for Employee creation, the employee ID SHALL be allocated by the system without user input, and the system SHALL validate that employee ID, phone, and email are unique before persisting. Specifically for Product creation and modification, the product ID SHALL be allocated by the system without user input, product price SHALL be an integer unit (大於或等於 0 的整數), and the system SHALL validate that the product name is unique (產品名稱不重複). Specifically for Customer creation and modification, the customer ID SHALL be allocated by the system without user input, and the system SHALL validate that customer ID, phone, tax ID (統一編號, if provided), and email (if provided) are unique before persisting. When any unique key constraint, parameter, type, format, or business validation fails during creation or modification across master data modules, the system SHALL reject the request with HTTP 400 and explicitly inform the user which specific field contains an error or duplication (e.g. "產品名稱已存在: [名稱]", "客戶電話已存在: [電話]", "客戶統一編號已存在: [統編]", "客戶電子郵件已存在: [信箱]", "客戶編號已存在: [編號]"), and SHALL NEVER return a generic HTTP 500 internal server error.

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

#### Scenario: Creating a product with unique product name validation
- **WHEN** an authorized user submits a product creation request with a product name that already exists in the system
- **THEN** the system rejects the request with HTTP 400 and an explicit error message stating "產品名稱已存在" instead of a 500 internal server error

#### Scenario: Updating a product with duplicate product name validation
- **WHEN** an authorized user attempts to update a product to use a product name that belongs to another existing product
- **THEN** the system rejects the request with HTTP 400 and an explicit error message stating "產品名稱已存在" instead of a 500 internal server error

#### Scenario: Creating a customer with system-allocated ID and uniqueness validation
- **WHEN** an authorized user submits a customer creation request
- **THEN** the system automatically generates a customer ID without accepting user input, validates that customer phone, tax ID (if provided), and email (if provided) are unique, and persists the record

#### Scenario: Rejecting customer creation on duplicate phone, tax ID, or email
- **WHEN** an authorized user attempts to create a customer with an existing phone number, tax ID, or email address
- **THEN** the system rejects the request with HTTP 400 and an explicit error message stating which field is duplicate (e.g. "客戶電話已存在: [電話]", "客戶統一編號已存在: [統編]", or "客戶電子郵件已存在: [信箱]") instead of a 500 internal server error

#### Scenario: Updating a customer with uniqueness validation
- **WHEN** an authorized user submits modifications for an existing customer keeping their own phone, tax ID, and email or providing new unique values
- **THEN** the system applies the updates and persists the record

#### Scenario: Rejecting customer modification on duplicate phone, tax ID, or email
- **WHEN** an authorized user attempts to update a customer with a phone number, tax ID, or email address that already belongs to another customer
- **THEN** the system rejects the request with HTTP 400 and an explicit error message stating which field is duplicate (e.g. "客戶電話已存在: [電話]", "客戶統一編號已存在: [統編]", or "客戶電子郵件已存在: [信箱]") instead of a 500 internal server error

#### Scenario: Catching database unique key constraints into HTTP 400 field error messages
- **WHEN** any master data creation or modification causes a database unique constraint violation (such as UK_product_name, UK_customer_tax_id, or UK_employee_phone)
- **THEN** the system catches the violation and returns HTTP 400 with a clear field duplication error message, never exposing HTTP 500 internal server error to the user

## ADDED Requirements

### Requirement: Customer Module Presentation and Field Completeness
The system SHALL display all customer database fields in the Customer master data view module (http://localhost:8080/customer) excluding the updated time (`updatedAt`). Specifically, the data table SHALL present customer ID, customer name, contact phone, email, company name, tax ID, country, city/district, detailed address, assigned employee, and created time. The customer creation and modification modal forms SHALL synchronize with all database fields excluding updated time, providing input fields for country (defaulting to "台灣"), and providing a dropdown selection for the assigned employee dynamically populated from employee master data.

#### Scenario: Rendering customer table with complete fields excluding updated time
- **WHEN** an authorized user views the customer master data page
- **THEN** the table displays customer ID, customer name, contact phone, email, company name, tax ID, country, city/district, detailed address, assigned employee, and created time, and omits the updated time column

#### Scenario: Synchronizing customer creation and modification form fields
- **WHEN** an authorized user opens the customer creation or edit modal
- **THEN** the form contains customer ID (system-allocated / disabled), customer name, contact phone, email, company name, tax ID, country (defaulting to "台灣"), city, district, detailed address, and assigned employee dropdown selector, without exposing an updated time field
