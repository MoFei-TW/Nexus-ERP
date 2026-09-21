# mofei-erp-master-data Specification

## Purpose

Defines CRUD operations, querying, pagination, and data masking for the four master data modules (Employee, Product, Customer, Order).

## Requirements

### Requirement: Four Master Data CRUD Operations
The system SHALL provide comprehensive CRUD (Create, Read, Update, Delete) RESTful endpoints and UI views for Employee (員工), Product (產品), Customer (客戶), and Order (訂單與訂單明細). Specifically for Employee creation, the employee ID SHALL be allocated by the system without user input, and the system SHALL validate that employee ID, phone, and email are unique before persisting. Specifically for Product creation and modification, the product ID SHALL be allocated by the system without user input, product price SHALL be an integer unit (大於或等於 0 的整數), the person in charge (employee ID) SHALL provide a dropdown menu synchronized with employee master data, and the system SHALL validate that the product name is unique (產品名稱不重複). Specifically for Customer creation and modification, the customer ID SHALL be allocated by the system without user input, and the system SHALL validate that customer ID, phone, tax ID (統一編號, if provided), and email (if provided) are unique before persisting. When any database constraint (including CHECK format constraints, FOREIGN KEY constraints, PRIMARY KEY constraints, or UNIQUE KEY constraints), parameter, type, format, or business validation fails during creation or modification across master data modules, the system SHALL reject the request with HTTP 400 and explicitly inform the user which specific field contains an error or duplication (e.g. "產品名稱已存在: [名稱]", "電子郵件格式不正確", "客戶電話已存在: [電話]", "客戶統一編號格式不正確", "負責員工編號不存在", "客戶編號已存在: [編號]"), and SHALL NEVER return a generic HTTP 500 internal server error nor a vague message like "資料已存在，請檢查輸入內容是否重複".

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

#### Scenario: Creating a product with system-allocated ID, integer price, employee selection, and unique name validation
- **WHEN** an authorized user submits a product creation request with integer price, a selected employee from the synced dropdown, and a unique product name
- **THEN** the system automatically generates a unique product ID without accepting user input, validates that the price is an integer greater than or equal to 0, verifies the employee exists, ensures the product name is unique, and persists the record

#### Scenario: Rejecting product creation or update on duplicate product name
- **WHEN** an authorized user submits a product creation or modification request with a product name that already exists in the system
- **THEN** the system rejects the request with HTTP 400 and an explicit error message stating "產品名稱已存在" instead of a 500 internal server error

#### Scenario: Updating a product with integer price, employee selection, and read-only product ID
- **WHEN** an authorized user submits product modifications with an integer price and a selected employee
- **THEN** the system keeps the product ID unchanged, validates the integer price and employee existence, and updates the record

#### Scenario: Rejecting product creation or update with detailed field error message
- **WHEN** an authorized user attempts to create or update a product with invalid input (such as empty product name, non-integer or negative price, or non-existent employee ID)
- **THEN** the system rejects the request with HTTP 400 and returns an error message explicitly stating which field has an invalid input

#### Scenario: Rejecting product creation or update on unreadable payload or type mismatch with explicit field error message
- **WHEN** an authorized user submits a product request payload with a type mismatch or malformed field value
- **THEN** the system rejects the request with HTTP 400 and returns a clear error message identifying the erroneous field instead of a generic 500 internal server error

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

#### Scenario: Catching database check and foreign key constraints into explicit field error messages
- **WHEN** any master data creation or modification violates a database CHECK constraint (such as CK_customer_email, CK_customer_phone, CK_customer_tax_id) or FOREIGN KEY constraint (such as FK_customer_employee)
- **THEN** the system catches the constraint violation and returns HTTP 400 explicitly identifying the invalid field and reason (e.g. "電子郵件格式不正確" or "負責員工編號不存在"), and never returns a generic "資料已存在，請檢查輸入內容是否重複" message

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

### Requirement: Employee Module Presentation and Ordering
The system SHALL display the Employee master data list sorted by employee ID in ascending order (由小到大) and render the system role as plain text without web badge tags.

#### Scenario: Displaying employee table with ascending order and plain role text
- **WHEN** an authorized user navigates to the employee master data page
- **THEN** the table displays records sorted by employee ID in ascending order, and the system role column renders plain text instead of `<span class="badge">` tags

### Requirement: Product Module Presentation and Ordering
The system SHALL display the Product master data list sorted by product ID in ascending order (由小到大依序排列).

#### Scenario: Displaying product table with ascending order
- **WHEN** an authorized user navigates to the product master data page (http://localhost:8080/product)
- **THEN** the table displays records sorted by product ID in ascending order

### Requirement: Customer Module Presentation and Ordering
The system SHALL display the Customer master data list sorted by customer ID in ascending order (由小到大).

#### Scenario: Displaying customer table with ascending order by customer ID
- **WHEN** an authorized user navigates to the customer master data page or requests customer pagination data
- **THEN** the records returned and displayed in the table are sorted by customer ID in ascending order (由小到大)

#### Scenario: Customer table ordering during composite or fuzzy search
- **WHEN** a user searches customer records by keyword, customer ID, or customer name
- **THEN** the filtered customer records are displayed sorted by customer ID in ascending order (由小到大)

### Requirement: Customer Module Presentation and Field Completeness
The system SHALL display all customer database fields in the Customer master data view module (http://localhost:8080/customer) excluding the updated time (`updatedAt`). Specifically, the data table SHALL present customer ID, customer name, contact phone, email, company name, tax ID, country, city/district, detailed address, assigned employee, and created time. The customer creation and modification modal forms SHALL synchronize with all database fields excluding updated time, providing input fields for country (defaulting to "台灣"), and providing a dropdown selection for the assigned employee dynamically populated from employee master data.

#### Scenario: Rendering customer table with complete fields excluding updated time
- **WHEN** an authorized user views the customer master data page
- **THEN** the table displays customer ID, customer name, contact phone, email, company name, tax ID, country, city/district, detailed address, assigned employee, and created time, and omits the updated time column

#### Scenario: Synchronizing customer creation and modification form fields
- **WHEN** an authorized user opens the customer creation or edit modal
- **THEN** the form contains customer ID (system-allocated / disabled), customer name, contact phone, email, company name, tax ID, country (defaulting to "台灣"), city, district, detailed address, and assigned employee dropdown selector, without exposing an updated time field

### Requirement: Customer Selection Auto-fill and Order Creation Real-time Total Calculation
The system SHALL automatically populate recipient information (name, phone, and complete shipping address) and the assigned sales employee upon customer selection in the create order modal. Furthermore, the system SHALL calculate and display line item subtotals in real time and maintain a dynamic live-updated estimated order total amount in the create order modal whenever products, quantities, or discounts are modified, added, or removed.

#### Scenario: Auto-populating customer info and sales employee on customer selection
- **WHEN** an authorized user selects a customer from the customer dropdown in the create order modal
- **THEN** the system automatically populates the recipient name with the customer name, the recipient phone with the customer phone, the recipient address with the customer complete address, and selects the assigned sales employee in the employee dropdown

#### Scenario: Real-time calculation of line item subtotal and total amount in create order form
- **WHEN** an authorized user selects a product, changes the quantity, or adjusts the discount (0, 5, 75) for an order line item in the create order modal
- **THEN** the system immediately updates the subtotal for that line item and recalculates the total estimated order amount displayed in the modal

#### Scenario: Updating total amount on adding or removing line items
- **WHEN** an authorized user adds a new line item or removes an existing line item in the create order modal
- **THEN** the system recalculates the total estimated order amount immediately

### Requirement: Order Module Presentation, Ordering, and Form Optimization
The system SHALL display the Order master data list sorted by order ID in ascending order (由小到大), render the order total amount as clean formatted numeric text without HTML tags (such as `<strong>`), provide dropdown selection menus for Customer and Employee during order creation, display explicit column header names for order detail line items, restrict discount choices to `0`, `5`, and `75` via dropdown selection, and support order detail line item modifications and adding new products with automatic subtotal and total amount recalculation.

#### Scenario: Displaying order table with ascending order by order ID
- **WHEN** an authorized user navigates to the order master data page or requests order pagination data
- **THEN** the records returned and displayed in the table are sorted by order ID in ascending order (由小到大)

#### Scenario: Displaying order total amount without HTML tags
- **WHEN** an authorized user views the order master data table
- **THEN** the order total amount column displays clean numeric formatting (e.g. `57,000`) without raw web tags such as `<strong>`

#### Scenario: Creating an order with dropdown selections and visible detail headers
- **WHEN** an authorized user opens the create order modal
- **THEN** the customer ID and employee ID fields are presented as dropdown select elements populated with available customer and employee records, the order detail section clearly displays column headers for product, quantity, and discount, and the discount field provides a dropdown selection restricted to options `0`, `5`, and `75`

#### Scenario: Modifying order details and adding products with total recalculation
- **WHEN** an authorized user opens the order detail modal and modifies line item quantities, adjusts discounts via the dropdown (0, 5, 75), or adds a new product line item, and submits the update
- **THEN** the system updates the order detail records, recalculates line subtotals and the overall order total amount, and persists the updated order state
