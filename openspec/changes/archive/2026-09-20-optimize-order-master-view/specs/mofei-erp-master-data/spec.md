# Spec Delta

## ADDED Requirements

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
