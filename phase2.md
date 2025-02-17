# Project 2: Phase 2 Description


* links
[GitHub Release](https://canvas.tamu.edu/courses/354258/assignments/2490845?module_item_id=12150624)
[Database correctness](https://canvas.tamu.edu/courses/354258/assignments/2490844?module_item_id=12150623)

The second phase of the project will consist of building the database by creating the ordering history and inventory data and inserting the data into the schema you designed in Phase 1.

Your goal will be to implement and verify the validity of your design.
You will demo the database in lab with your teaching assistant.
You must be present for the demo in lab to get credit.
You will also write an individual status report describing your process and contribution to this phase.



## Requirements
Your database system will be evaluated using a series of SQL queries to determine its correctness and proximity to the design document.

If you had to make changes to your database design, you will need to describe and justify these changes during your demo.
Be sure to keep your design documents up to date.

## Data
You will create data to be stored in your database:

Create at least α weeks of sales history -- starting about one year ago and ending about today -- to store in your database that in total have approximately $β million in sales.
Include φ peak(s) days where sales are significantly higher, which typically occur at the start of the regular semester. You might also consider peak days like game days versus away games for football or relevant special holidays.
Create inventory items for at least δ different menu items. Remember that a given menu item will have multiple ingredients.
You will also need other items such as cups, straws, napkins, bags, and so on.
You are strongly encouraged to use scripting (e.g., Python) to generate `.sql` files that contain sequences of SQL commands to populate the database. You should end up with several thousand insert statements (if not tens or hundreds of thousands of them). Save all of these scripts so you can recreate anything at any time.


## Queries
You will also create at least ε SQL queries that can be run as an input file to verify the low-level design and interactions in your database, but you are encouraged to build additional SQL queries for extra practice.

These queries should have some examples of a desired question that can be answered through this query.
They should also demonstrate that your database has been correctly populated and you have supplied the additional data as requested above.
You will submit this query list on Canvas and demo these queries in lab.

## Special Queries
Every team must include the first θ special queries which will be used to verify the seeding of the database:

* Special Query #1: "Weekly Sales History"

* pseudocode: select count of orders grouped by week
about: given a specific week, how many orders were placed?
example: "week 1 has 98765 orders"
Special Query #2: "Realistic Sales History"

* pseudocode: select count of orders, sum of order total grouped by hour
about: given a specific hour of the day, how many orders were placed and what was the total sum of the orders?
example: e.g., "12pm has 12345 orders totaling $86753"
Special Query #3: "Peak Sales Day"

* pseudocode: select top 10 sums of order total grouped by day in descending order by order total
about: given a specific day, what was the sum of the top 10 order totals?
example: "30 August has $12345 of top sales"
Special Query #4: "Menu Item Inventory"

* pseudocode: select count of inventory items from inventory and menu grouped by menu item
about: given a specific menu item, how many items from the inventory does that menu item use?
example: "classic milk tea uses 12 items"
Special Query #5: "Best of the Worst"

* pseudocode: select bottom sum of order total, top count of menu items by day grouped by week
about: given a specific week, what day had the lowest sales, what were the sales numbers, and what was the top seller that day?
example: "12 December has lowest sales of $4321 for week 18 with match milk tea as top seller"
These θ special queries count towards the requirement of the minimum ε required SQL queries (i.e., teams must have at least (ε − θ) more queries).


Workload Balancing
The following table defines the values of the variables for each team size, in order to balance for team size.  

Workload Balancing

* SYMBOL	TEAM OF 4	DESCRIPTION
* α	        39		number of weeks
* β	      ~750K		amount of total sales
* φ	        1		number of peak days
* δ	        16		number of menu items
* ε	        12		number of required queries
* θ	        3		first nth special queries
