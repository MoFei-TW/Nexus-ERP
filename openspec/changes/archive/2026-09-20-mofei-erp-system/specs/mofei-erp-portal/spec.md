# Spec Delta

## Purpose

Defines the portal layout, dynamic clock, navigation, footer, offline styling, and operational dashboard for MOFEI ERP.

## ADDED Requirements

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
