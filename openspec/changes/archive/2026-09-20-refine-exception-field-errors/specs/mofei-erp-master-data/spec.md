# Spec Delta

## MODIFIED Requirements

### Requirement: Four Master Data CRUD Operations
The system SHALL provide comprehensive CRUD (Create, Read, Update, Delete) RESTful endpoints and UI views for Employee (員工), Product (產品), Customer (客戶), and Order (訂單與訂單明細). Specifically for Employee creation, the employee ID SHALL be allocated by the system without user input, and the system SHALL validate that employee ID, phone, and email are unique before persisting. Specifically for Product creation and modification, the product ID SHALL be allocated by the system without user input, product price SHALL be an integer unit (大於或等於 0 的整數), and the system SHALL validate that the product name is unique (產品名稱不重複). When any database constraint (including CHECK format constraints, FOREIGN KEY constraints, PRIMARY KEY constraints, or UNIQUE KEY constraints), parameter, type, format, or business validation fails during creation or modification across master data modules, the system SHALL reject the request with HTTP 400 and explicitly inform the user which specific field contains an error or duplication (e.g. "產品名稱已存在: [名稱]", "電子郵件格式不正確", "客戶電話已存在: [電話]", "客戶統一編號格式不正確", "負責員工編號不存在"), and SHALL NEVER return a generic HTTP 500 internal server error nor a vague message like "資料已存在，請檢查輸入內容是否重複".

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

#### Scenario: Catching database unique key constraints into HTTP 400 field error messages
- **WHEN** any master data creation or modification causes a database unique constraint violation (such as UK_product_name, UK_customer_tax_id, or UK_employee_phone)
- **THEN** the system catches the violation and returns HTTP 400 with a clear field duplication error message, never exposing HTTP 500 internal server error to the user

#### Scenario: Catching database check and foreign key constraints into explicit field error messages
- **WHEN** any master data creation or modification violates a database CHECK constraint (such as CK_customer_email, CK_customer_phone, CK_customer_tax_id) or FOREIGN KEY constraint (such as FK_customer_employee)
- **THEN** the system catches the constraint violation and returns HTTP 400 explicitly identifying the invalid field and reason (e.g. "電子郵件格式不正確" or "負責員工編號不存在"), and never returns a generic "資料已存在，請檢查輸入內容是否重複" message
