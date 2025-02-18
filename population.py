import random
import datetime

# Constants (Adjust these based on team size)
alpha_weeks = 39  # Number of weeks of sales history
beta_sales = 750_000  # Total sales in dollars
phi_peaks = 1  # Number of peak days
delta_menu_items = 20  # Number of menu items
epsilon_queries = 15  # Number of required queries
theta_special_queries = 4  # Number of special queries

# employee
employees = [
    (1, 'Alice', 'Johnson', 'Manager'),
    (2, 'Bob', 'Smith', 'Cashier'),
    (3, 'Charlie', 'Brown', 'Cashier'),
    (4, 'David', 'Wilson', 'Cashier'),
    (5, 'Eve', 'Davis', 'Cashier'),
    (6, 'Frank', 'Miller', 'Cashier')
]

# Generate order timestamps
def generate_timestamps(weeks=alpha_weeks):
    start_date = datetime.datetime.now() - datetime.timedelta(weeks=weeks)
    timestamps = []
    for i in range(weeks * 7):  # Generate multiple orders per day
        for _ in range(random.randint(5, 15)):  # More orders per day for higher sales
            timestamps.append(start_date + datetime.timedelta(days=i, hours=random.randint(8, 20), minutes=random.randint(0, 59)))
    return timestamps

# Generate menu items and inventory
menu_items = [f"MenuItem_{i}" for i in range(1, delta_menu_items + 1)]
inventory_items = [f"InventoryItem_{i}" for i in range(1, delta_menu_items * 3)]  # Multiple ingredients per menu item

# SQL File Writing with Bulk Inserts and Transaction Control
with open("populate_database.sql", "w") as f:
    # Wrap everything in a single transaction
    f.write("BEGIN;\n")
    
    # Insert Inventory (Bulk)
    inventory_inserts = []
    for i, item in enumerate(inventory_items, 1):
        inventory_inserts.append(f"({i}, {random.randint(50, 500)}, '{item}')")
    f.write(f"INSERT INTO Inventory (Inventory_ID, Current_Number, Name) VALUES {', '.join(inventory_inserts)};\n")
    
    # Insert Menu Items (Bulk)
    menu_inserts = []
    for i, item in enumerate(menu_items, 1):
        price = round(random.uniform(15, 50), 2)
        inventory_id = random.randint(1, len(inventory_items))
        menu_inserts.append(f"({i}, {price}, {inventory_id})")
    f.write(f"INSERT INTO Menu_Item (Menu_ID, Price, Menu_Inventory) VALUES {', '.join(menu_inserts)};\n")

    # insert employee info
    f.write("-- SQL script to insert employees into Employee table\n")
    for emp_id, first_name, last_name, position in employees:
        sql = f"INSERT INTO Employee (Employee_ID, First_name, Last_name, Position) VALUES ({emp_id}, '{first_name}', '{last_name}', '{position}');\n"
        f.write(sql)
    
    # Insert Orders (Bulk)
    order_inserts = []
    order_details = []
    timestamps = generate_timestamps()
    total_sales = 0
    order_id = 1

    # employee id list
    employee_id_list = [1,2,3,4,5,6]

    while total_sales < beta_sales:
        timestamp = random.choice(timestamps)
        order_total = round(random.uniform(20, 100), 2)
        total_sales += order_total
        employee_id = random.choice(employee_id_list)
        order_inserts.append(f"({order_id}, '{timestamp.isoformat()}', {order_total}, {employee_id})")
        
        # Link Orders to Menu Items
        for _ in range(random.randint(1, 5)):
            menu_id = random.randint(1, delta_menu_items)
            order_details.append(f"({order_id}, {menu_id}, {order_id})")
        
        order_id += 1
    
    # Insert all orders in bulk
    f.write(f"INSERT INTO customer_order (Order_ID, Time, Total_Price) VALUES {', '.join(order_inserts)};\n")
    
    # Insert Junction Table (Bulk)
    f.write(f"INSERT INTO C_M_Junction (ID, Menu_ID, Order_ID) VALUES {', '.join(order_details)};\n")
    
    
    # Insert Customers (Bulk)
    customer_inserts = []
    for cust_id in range(1, order_id // 2):
        phone = random.randint(1000000000, 9999999999)
        order_ref = random.randint(1, order_id - 1)
        customer_inserts.append(f"({cust_id}, {phone}, {order_ref})")
    f.write(f"INSERT INTO Customer (Customer_ID, Phone_number, Order_ID) VALUES {', '.join(customer_inserts)};\n")
    
    # Commit Transaction
    f.write("COMMIT;\n")
    
    print(f"Generated SQL file with approximately {total_sales} in sales over {alpha_weeks} weeks.")