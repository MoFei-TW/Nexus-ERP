# mofei-erp-portal Specification

## Purpose

Defines the portal layout, dynamic clock, navigation, footer, offline styling, and operational dashboard for MOFEI ERP.

## Requirements

### Requirement: Global Layout and Public Templates
The system SHALL provide a shared global layout (`layout.html`) featuring a top navigation bar (Navbar) with the corporate logo loaded from `workspace/images/logo/2026-09-10-00-logo by MF.jpg`, a dynamic real-time clock displaying `YYYY-MM-DD HH:mm:ss`, navigation buttons linking to the 4 master data modules and dashboard, and a footer displaying `@2026 DataBase by MOFEI`.

#### Scenario: Rendering layout with dynamic clock
- **WHEN** a user visits any page in the ERP system
- **THEN** the page renders the consistent Navbar with logo, links, a live clock updating every second, and the copyright footer

### Requirement: Operational Dashboard
The system SHALL provide a home dashboard view displaying real-time metrics, counts, and status monitoring statistics for the 4 master data domains (Employee, Product, Customer, Order).

#### Scenario: Viewing dashboard statistics
- **WHEN** a user navigates to the dashboard home page
- **THEN** the system displays aggregated statistics cards reflecting current counts and key statuses across employees, products, customers, and orders
 
### Requirement: Pure Native Offline Styling Architecture
The system SHALL operate 100% offline without external CDN dependencies or Node.js/npm build steps, adopting a minimalist black-and-white-gray visual theme with zero extraneous color rendering, built exclusively with native HTML5, CSS3, and ES6+ Vanilla JavaScript.

#### Scenario: Offline execution without external network
- **WHEN** the application is loaded in an isolated network environment with no internet access
- **THEN** all visual styles, scripts, fonts, and assets load locally with full functionality and correct minimalist styling

### Requirement: View Template Typography and Text Layout Integrity
The system SHALL ensure that all view templates (`layout.html`, `dashboard.html`, `employee.html`, `product.html`, `customer.html`, `order.html`, `login.html`) prevent unwanted or awkward text line breaks. All table headers (`th`), action buttons (`.btn`), status badges (`.badge`), form field labels (`label`), pagination status indicators, and navbar user greeting elements SHALL enforce non-wrapping (`white-space: nowrap`) and coherent typographic alignment, preventing single-character or fragmented line breaks.

#### Scenario: Rendering table headers and action buttons without line breaks
- **WHEN** an authorized user views any master data table (Employee, Product, Customer, Order)
- **THEN** all table column headers and action buttons are displayed on a single line without awkward line breaks or character wrapping

#### Scenario: Rendering form field labels and status badges without line breaks
- **WHEN** an authorized user opens any create or edit modal dialog, or views status badges
- **THEN** field labels (including required indicators and format hints) and status badges maintain full single-line integrity without line breaking

### Requirement: Complete Table Column and Header Rendering
The system SHALL render all table column titles and data cell contents completely without truncation, clipping, or column squishing. The data table container (`.table-card`) SHALL provide responsive horizontal scrolling (`overflow-x: auto`) whenever table contents exceed the viewport width, and all column headers (`th`) SHALL retain their full text and minimum widths.

#### Scenario: Viewing data tables on various viewport widths
- **WHEN** a user views a table with multiple columns (such as Customer or Order master tables) on a constrained or standard viewport
- **THEN** all column header names (such as "訂單總額 (NT$)", "配送地址快照", "所在地區與地址") are rendered completely without text truncation or word breaking, with horizontal scrolling enabled on the table container

#### Scenario: Dynamic table content rendering
- **WHEN** data records are loaded and rendered dynamically via `MofeiTable`
- **THEN** table data cells render complete text without clipping, and action columns remain fully visible with non-wrapping buttons

### Requirement: Complete Input Control and Placeholder Message Rendering
The system SHALL render all input controls, select controls, and placeholder text messages completely without truncation or clipping. Search fields and modal input controls SHALL maintain adequate minimum widths (`min-width`) and flexible growth, ensuring that placeholder hints (e.g., "搜尋客戶編號、姓名、統編、電話或地址...") and default values are fully readable without being cut off.

#### Scenario: Rendering search bar input placeholders
- **WHEN** a user visits any master data page with a search and filter bar
- **THEN** all search input boxes, particularly keyword fuzzy search inputs, provide sufficient width so that placeholder text strings are rendered completely without truncation

#### Scenario: Rendering modal form inputs and format hints
- **WHEN** a user opens a create or edit modal form in any master data module
- **THEN** all input boxes, select dropdowns, and placeholder instructions render completely within the modal viewport without text clipping
