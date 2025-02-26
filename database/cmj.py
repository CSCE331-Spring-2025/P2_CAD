import random
import datetime

# Constants
alpha_weeks = 39  # Sales history in weeks
beta_sales = 750_000  # Target total sales
phi_peaks = 1  # Number of peak days

# Number of days to distribute sales
total_days = alpha_weeks * 7  
peak_day = random.randint(1, total_days)  # Randomly select a peak day

# Employee list
employees = [1, 2, 3, 4, 5, 6]

# Menu items with prices
menu_items = {
    1: 15.37, 2: 15.83, 3: 10.80, 4: 13.26, 5: 12.91, 6: 10.43, 7: 10.29,
    8: 11.85, 9: 12.00, 10: 11.61, 11: 15.09, 12: 13.67, 13: 13.26, 14: 13.42,
    15: 14.36, 16: 15.84, 17: 11.97, 18: 14.41, 19: 10.34, 20: 15.32
}

# Generate order timestamps
start_date = datetime.datetime.now() - datetime.timedelta(weeks=alpha_weeks)

# SQL file writing
with open("cmj.sql", "w") as f:
    f.write("BEGIN;\n")

    total_sales = 0
    order_id = 1
    order_details = []

    # Sales distribution across days
    daily_sales = {day: beta_sales / total_days for day in range(total_days)}
    daily_sales[peak_day] *= 2  # Make one day a peak day

    while order_id <= 12_436:
        order_date = start_date + datetime.timedelta(days=random.randint(0, total_days - 1))
        employee_id = random.choice(employees)
        num_items = random.randint(1, min(5, len(menu_items)))  # Ensure unique selection is possible

        order_total = 0
        order_entries = []
        chosen_menus = set()  # Track selected menu items

        for _ in range(num_items):
            available_menus = list(set(menu_items.keys()) - chosen_menus)
            if not available_menus:
                break  # No more unique items left

            menu_id = random.choice(available_menus)
            chosen_menus.add(menu_id)  # Mark as used
            price = menu_items[menu_id]
            quantity = random.randint(1, 3)  # Random quantity per menu item

            order_entries.append(f"({order_id}, {menu_id}, {quantity})")
            order_total += price * quantity

        # Append order details
        order_details.extend(order_entries)

        # Update sales tracking
        total_sales += order_total
        order_id += 1

    # Insert data into C_M_Junction
    f.write(f"INSERT INTO C_M_Junction (Order_ID, Menu_ID, Quantity) VALUES {', '.join(order_details)};\n")

    # Commit transaction
    f.write("COMMIT;\n")

    print(f"Generated SQL file with {total_sales:.2f} in sales over {total_days} days and {order_id-1} orders.")
