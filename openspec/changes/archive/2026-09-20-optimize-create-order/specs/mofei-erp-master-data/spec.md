# Spec Delta

## ADDED Requirements

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
