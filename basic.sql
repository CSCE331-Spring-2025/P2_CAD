
CREATE TABLE Employee (
    Employee_ID INT PRIMARY KEY,
    First_name CHAR(10) NOT NULL,
    Last_name CHAR(10) NOT NULL,
    Position CHAR(10) NOT NULL
);

CREATE TABLE customer_order (
    Order_ID INT PRIMARY KEY,
    Time TIMESTAMP NOT NULL,
    Total_Price FLOAT NOT NULL,
    Employee_ID INT,
    FOREIGN KEY (Employee_ID) REFERENCES Employee(Employee_ID)
);

CREATE TABLE Customer (
    Customer_ID INT PRIMARY KEY,
    Phone_number BIGINT NOT NULL,
    Order_ID INT,
    FOREIGN KEY (Order_ID) REFERENCES customer_order(Order_ID)
);

CREATE TABLE Inventory (
    Inventory_ID INT PRIMARY KEY,
    Current_Number INT NOT NULL,
    Name CHAR(20) NOT NULL,
    Supply_Order_History INT
);

CREATE TABLE Menu_Item (
    Menu_ID INT PRIMARY KEY,
    Price FLOAT NOT NULL,
    Menu_Inventory INT,
    FOREIGN KEY (Menu_Inventory) REFERENCES Inventory(Inventory_ID)
);

CREATE TABLE C_M_Junction (
    ID INT PRIMARY KEY,
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


/* 
// Delete the tables
// try several times

DROP Table M_I_Junction;
DROP TABLE C_M_Junction;
DROP TABLE Menu_Item;
DROP TABLE Inventory;
DROP TABLE Employee;
DROP TABLE Customer; 
DROP TABLE customer_order;

*/