
CREATE TABLE Employee (
    Employee_ID INT PRIMARY KEY,
    First_name VARCHAR(50) NOT NULL,
    Last_name VARCHAR(50) NOT NULL,
    Position VARCHAR(50) NOT NULL
);

CREATE TABLE Customer (
    Customer_ID INT PRIMARY KEY,
    Phone_number BIGINT NOT NULL
);

CREATE TABLE customer_order (
    Order_ID INT PRIMARY KEY,
    Time TIMESTAMP NOT NULL,
    Total_Price FLOAT NOT NULL,
    Employee_ID INT,
    Customer_ID INT,
    FOREIGN KEY (Employee_ID) REFERENCES Employee(Employee_ID),
    FOREIGN KEY (Customer_ID) REFERENCES customer(Customer_ID)
);

CREATE TABLE Inventory (
    Inventory_ID INT PRIMARY KEY,
    Current_Number INT NOT NULL,
    Name VARCHAR(50) NOT NULL,
    Supply_Order_History INT
);

CREATE TABLE Menu_Item (
    Menu_ID INT PRIMARY KEY,
    Price FLOAT NOT NULL,
    name VARCHAR(50), 
    Menu_Inventory INT,
    FOREIGN KEY (Menu_Inventory) REFERENCES Inventory(Inventory_ID)
);

CREATE TABLE C_M_Junction (
    ID VARCHAR(50) PRIMARY KEY,
    Menu_ID INT,
    Order_ID INT,
    FOREIGN KEY (Menu_ID) REFERENCES Menu_Item(Menu_ID),
    FOREIGN KEY (Order_ID) REFERENCES customer_order(Order_ID)
);

-- Menu / Inventory Junction Table
CREATE TABLE M_I_Junction (
    MI_ID INT PRIMARY KEY,
    Menu_ID INT,
    Inventory_ID INT,
    FOREIGN KEY (Menu_ID) REFERENCES Menu_Item(Menu_ID),
    FOREIGN KEY (Inventory_ID) REFERENCES Inventory(Inventory_ID)
);



/* Updates teh inventory and provide a suggestion for the manager*/
UPDATE inventory i
SET inventory_suggestion = COALESCE(s.suggested_amount, 0)
FROM (
    SELECT 
        mij.inventory_id,
        CASE 
            WHEN SUM(co.total_price) < 10 THEN SUM(co.total_price) * 1
            WHEN SUM(co.total_price) < 100 THEN SUM(co.total_price) * 2
            ELSE SUM(co.total_price) * 3
        END AS suggested_amount
    FROM customer_order co
    JOIN menu_item mi ON co.order_id = mi.menu_id  -- Link orders to menu items
    JOIN m_i_junction mij ON mi.menu_id = mij.menu_id  -- Map menu items to inventory
    GROUP BY mij.inventory_id
) s
WHERE i.inventory_id = s.inventory_id;
/* 
-- Delete the tables
-- try several times

DROP Table M_I_Junction;
DROP TABLE C_M_Junction;
DROP TABLE Menu_Item;
DROP TABLE Inventory;
DROP TABLE customer_order;
DROP TABLE Employee;
DROP TABLE Customer; 

*/
