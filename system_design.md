### basic system design
<br>

# page 0 : login page 

- login with pin (employee id)
- if (manager)
    - page1: oerder
    - page2: order history
        - opt1: list view
        - opt2: view trend >> based on this, generate inventory suggestions
        - opt3: refund (optional)
    - page3: employee Management
    - page4: inventory
- if (cashier)
    - page1: order
    - page2: order history (list view only)

<br>

# page 1 : order (manager & cashier)

- left panel
    -- Retrieve menu information (menu name, price, etc.)
    -- Customization? (Milk, flavor, additional notes, etc.)

- right panel
    -- insert new row into customer_order table
    -- need to show
        -- Items in the order
        -- current total price
        -- ??

- payment page? 
    - get order info from previous page (pass the pk of customer_order?)
        - Items in the order
        - current total price
        - ??
    - Payment options: Cash / Card
    - Order status (processing, payment approved, payment denied, order placed, refunded ... etc)

<br>

# page 2 : order history

- opt1 : list view (cashier & manager)
    - show all orders in selected date
- opt2 : trend view
    - show a trend chart for the selected date and time period
    - button (or input box) : to select date and time range
- opt3 : refund
    - process refund (change status)

<br>

# page 3 : employee (management) page
- add / remove cashier
- Display employee information (pin, number of orders handled, etc.)

<br>

# page 4 : inventory page

- show basic chart
    - name
    - Quantity in stock
    - Associated menu items (using a junction table)
    - category
    - recommended order (needs to be generated)

    - button : place order
