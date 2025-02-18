-- Special Query #1: Weekly Sales History
SELECT DATE_TRUNC('week', Time) AS week, COUNT(Order_ID) AS total_orders
FROM customer_order
GROUP BY week
ORDER BY week;

-- Special Query #2: Realistic Sales History
SELECT EXTRACT(HOUR FROM Time) AS order_hour, COUNT(Order_ID) AS total_orders
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


