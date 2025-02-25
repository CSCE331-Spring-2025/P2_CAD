
CREATE TABLE Employee (
    Employee_ID INT PRIMARY KEY,
    First_name VARCHAR(50) NOT NULL,
    Last_name VARCHAR(50) NOT NULL,
    Position VARCHAR(50) NOT NULL,
    pin INT
);

CREATE TABLE Customer (
    Customer_ID INT PRIMARY KEY,
    Phone_number BIGINT NOT NULL
);

CREATE TABLE customer_order (
    Order_ID serial PRIMARY KEY,
    Time TIMESTAMP NOT NULL,
    Total_Price FLOAT NOT NULL,
    Employee_ID INT,
    Customer_ID INT,
    custom VARCHAR(50),
    status VARCHAR(50),
    FOREIGN KEY (Employee_ID) REFERENCES Employee(Employee_ID),
    FOREIGN KEY (Customer_ID) REFERENCES customer(Customer_ID)
);

CREATE TABLE Inventory (
    Inventory_ID INT PRIMARY KEY,
    Current_Number INT NOT NULL,
    Name VARCHAR(50) NOT NULL,
    category VARCHAR(50)
);

CREATE TABLE Menu_Item (
    Menu_ID INT PRIMARY KEY,
    Price FLOAT NOT NULL,
    name VARCHAR(50)
);

CREATE TABLE C_M_Junction (
    Order_ID INT,
    Menu_ID INT,
    Quantity INT NOT NULL,
    PRIMARY KEY (Order_ID, Menu_ID),
    FOREIGN KEY (Menu_ID) REFERENCES Menu_Item(Menu_ID),
    FOREIGN KEY (Order_ID) REFERENCES customer_order(Order_ID)
);

-- Menu / Inventory Junction Table
CREATE TABLE M_I_Junction (
    MI_ID serial PRIMARY KEY,
    Menu_ID INT,
    Inventory_ID INT,
    FOREIGN KEY (Menu_ID) REFERENCES Menu_Item(Menu_ID),
    FOREIGN KEY (Inventory_ID) REFERENCES Inventory(Inventory_ID)
);


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

DROP TRIGGER IF EXISTS trigger_update_total_price ON C_M_Junction;
DROP FUNCTION IF EXISTS update_total_price();

*/


CREATE OR REPLACE FUNCTION update_total_price()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE customer_order
    SET Total_Price = (
        SELECT COALESCE(SUM(mi.Price * cmj.Quantity), 0)  -- 주문이 없을 경우 NULL 방지
        FROM C_M_Junction cmj
        JOIN Menu_Item mi ON cmj.Menu_ID = mi.Menu_ID
        WHERE cmj.Order_ID = NEW.Order_ID
    )
    WHERE Order_ID = NEW.Order_ID;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_total_price
AFTER INSERT OR UPDATE OR DELETE ON C_M_Junction
FOR EACH ROW EXECUTE FUNCTION update_total_price();

