-- Special Query #1: Weekly Sales History
SELECT DATE_TRUNC('week', Time) AS week, COUNT(Order_ID) AS total_orders
FROM customer_order
GROUP BY week
ORDER BY week;

-- Special Query #2: Realistic Sales History
SELECT 
    EXTRACT(HOUR FROM Time) AS order_hour, 
    COUNT(Order_ID) AS total_orders, 
    SUM(Total_Price) AS total_sales
FROM customer_order
GROUP BY order_hour
ORDER BY total_orders DESC;


-- Special Query #3: Peak Sales Day
SELECT DATE(Time) AS day, SUM(Total_Price) AS total_sales
FROM customer_order
GROUP BY day
ORDER BY total_sales DESC
LIMIT 10;

-----------------------------------------------------------------------------

-- #1 Order by day of the week

SELECT TO_CHAR(Time, 'Day') AS order_day, COUNT(Order_ID) AS total_orders
FROM customer_order
GROUP BY order_day
ORDER BY total_orders DESC; 

-- #2 average daily sale

SELECT AVG(daily_total) AS avg_daily_sale
FROM (
    SELECT time::date AS order_date, SUM(total_price) AS daily_total
    FROM customer_order
    GROUP BY time::date
) AS daily_totals;

-- #3 ave revenue for each employee
SELECT e.employee_id, e.first_name, e.last_name, SUM(co.total_price) AS total_revenue
FROM employee e
JOIN customer_order co ON e.employee_id = co.employee_id
GROUP BY e.employee_id, e.first_name, e.last_name
ORDER BY total_revenue DESC;

-- #4 Total Revenue Per Month
SELECT DATE_TRUNC('month', Time) AS month, SUM(Total_Price) AS total_revenue
FROM customer_order
GROUP BY month
ORDER BY month;

-- #5 Daily Order Volume
SELECT DATE(Time) AS order_date, COUNT(Order_ID) AS total_orders
FROM customer_order
GROUP BY order_date
ORDER BY order_date;

-- #6 find lowest inventory items
SELECT Inventory_ID, Name, Current_Number
FROM Inventory
ORDER BY Current_Number ASC
LIMIT 10;