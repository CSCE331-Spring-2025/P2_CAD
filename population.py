import random
import datetime

# Constants (Adjust these based on team size)
alpha_weeks = 39  # Number of weeks of sales history
beta_sales = 750_000  # Total sales in dollars
phi_peaks = 1  # Number of peak days
delta_menu_items = 20  # Number of menu items
epsilon_queries = 15  # Number of required queries
theta_special_queries = 4  # Number of special queries

# Generate order timestamps
def generate_timestamps(weeks=alpha_weeks):
    start_date = datetime.datetime.now() - datetime.timedelta(weeks=weeks)
    timestamps = []
    for i in range(weeks * 7):  # Generate multiple orders per day
        for _ in range(random.randint(5, 15)):  # More orders per day for higher sales
            timestamps.append((start_date + datetime.timedelta(days=i, hours=random.randint(8, 20), minutes=random.randint(0, 59))).strftime('%Y-%m-%d %H:%M:%S'))
    return timestamps

# Generate menu items and inventory
menu_items = [f"MenuItem_{i}" for i in range(1, delta_menu_items + 1)]
inventory_items = [f"InventoryItem_{i}" for i in range(1, delta_menu_items * 3)]  # Multiple ingredients per menu item

# SQL File Writing
with open("populate_database.sql", "w") as f:
    # Insert Inventory
    for i, item in enumerate(inventory_items, 1):
        f.write(f"INSERT INTO Inventory (Inventory_ID, Current_Number, Name) VALUES ({i}, {random.randint(50, 500)}, '{item}');\n")
    
    # Insert Menu Items
    for i, item in enumerate(menu_items, 1):
        price = round(random.uniform(15, 50), 2)  # Higher price range to achieve target sales
        inventory_id = random.randint(1, len(inventory_items))
        f.write(f"INSERT INTO Menu_Item (Menu_ID, Price, Menu_Inventory) VALUES ({i}, {price}, {inventory_id});\n")
    
    # Insert Orders
    timestamps = generate_timestamps()
    total_sales = 0
    order_id = 1
    while total_sales < beta_sales:
        timestamp = random.choice(timestamps)
        order_total = round(random.uniform(20, 100), 2)  # Higher order values
        total_sales += order_total
        f.write(f"INSERT INTO customer_order (Order_ID, Time, Total_Price) VALUES ({order_id}, '{timestamp}', {order_total});\n")
        
        # Link Orders to Menu Items
        for _ in range(random.randint(1, 5)):
            menu_id = random.randint(1, delta_menu_items)
            f.write(f"INSERT INTO C_M_Junction (ID, Menu_ID, Order_ID) VALUES ({order_id}, {menu_id}, {order_id});\n")
        
        order_id += 1
    
    # Insert Employees
    for emp_id in range(1, 11):
        f.write(f"INSERT INTO Employee (Employee_ID, First_name, Last_name, Position) VALUES ({emp_id}, 'Emp{emp_id}', 'Last{emp_id}', 'Cashier');\n")
    
    # Insert Customers
    for cust_id in range(1, order_id // 2):
        phone = random.randint(1000000000, 9999999999)
        order_ref = random.randint(1, order_id - 1)
        f.write(f"INSERT INTO Customer (Customer_ID, Phone_number, Order_ID) VALUES ({cust_id}, {phone}, {order_ref});\n")
    
    print(f"Generated SQL file with approximately {total_sales} in sales over {alpha_weeks} weeks.")
