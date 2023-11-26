package restaurant;

import java.util.Locale.Category;

/**
 * RUHungry is a fictitious restaurant. 
 * You will be running RUHungry for a day by seating guests, 
 * taking orders, donation requests and restocking the pantry as necessary.
 * 
 * Compiling and executing:
 * 1. use the run or debug function to run the driver and test your methods 
 * 
 * @author Mary Buist
 * @author Kushi Sharma
*/

public class RUHungry {
    
    /*
     * Instance variables
     */

    // Menu: two parallel arrays. The index in one corresponds to the same index in the other.
    private String[] categoryVar; // array where containing the name of menu categories (e.g. Appetizer, Dessert).
    private MenuNode[] menuVar;   // array of lists of MenuNodes where each index is a category.
    
    // Stock: hashtable using chaining to resolve collisions.
    private StockNode[] stockVar;  // array of linked lists of StockNodes (use hashfunction to organize Nodes: id % stockVarSize)
    private int stockVarSize;

    // Transactions: orders, donations, restock transactions are recorded 
    private TransactionNode transactionVar; // refers to the first front node in linked list

    // Queue keeps track of people who've left the restaurant
    private Queue<People> leftQueueVar;  

    // Tables
    private People[] tables;        // array for people who are currently sitting
    private int[][]  tablesInfo;    // 2-D integer array where the first row contains how many seats there are at each table (each index)
                                    // and the second row contains "0" or "1", where 1 is the table is not available and 0 the opposite

    /*
     * Default constructor
     */
    public RUHungry () {
        categoryVar    = null;
        menuVar        = null;
        stockVar       = null;
        stockVarSize   = 0;
        transactionVar = null;
        leftQueueVar   = null;
        tablesInfo     = null;
        tables         = null;
    }

    /*
     * Get/Set methods
     */
    public MenuNode[] getMenu() { return menuVar; }
    public String[] getCategoryArray() { return categoryVar;}
    public StockNode[] getStockVar() { return stockVar; } 
    public TransactionNode getFrontTransactionNode() { return transactionVar; } 
    public TransactionNode resetFrontNode() {return transactionVar = null;} // method to reset the transactions for a new day
    public Queue<People> getLeftQueueVar() { return leftQueueVar; } 
    public int[][] getTablesInfo() { return tablesInfo; }

    /*
     * Menu methods
     */

    /**
     * 
     * This method populates the two parallel arrays menuVar and categoryVar.
     * 
     * Each index of menuVar corresponds to the same index in categoryVar (a menu category like Appetizers).
     * If index 0 at categoryVar is Appetizers then menuVar at index 0 contains MenuNodes of appetizer dishes.
     * 
     * 1. read the input file:
     *      a) the first number corresponds to the number of categories (aka length of menuVar and categoryVar)
     *      b) the next line states the name of the category (populate CategoryVar as you read each category name)
     *      c) the next number represents how many dishes are in that category - this will be the size of the linked list in menuVar for this category
     *      d) the next line states the name of the dish
     *      e) the first number in the next line represents how many ingredient IDs there are
     *      f) the next few numbers (all in the 100s) are each the ingredient ID
     * 
     * 2. As you read through the input file:
     *      a) populate the categoryVar array
     *      b) populate menuVar depending on which index (aka which category) you are in
     *          i) make a dish object (with filled parameters -- don't worry about "price" and "profit" in the dish object for right now)
     *          ii) create menuNode and insert at the front of menuVar (NOTE! there will be multiple menuNodes in one index)
     * 
     * @param inputFile - use menu.in file which contains all the dishes
     */

    public void menu(String inputFile) {

        StdIn.setFile(inputFile);

        // Read the number of categories
        int numCategories = StdIn.readInt();

        // Initialize arrays
        categoryVar = new String[numCategories];
        menuVar = new MenuNode[numCategories];

        // Read category information and populate arrays
        for (int i = 0; i < numCategories; i++) {
            // Read category name
            categoryVar[i] = StdIn.readString();

            // Initialize the linked list for the category
            menuVar[i] = null;

            // Read the number of dishes in the category
            int numDishes = StdIn.readInt();

            // Read dish information and populate the linked list
            for (int j = 0; j < numDishes; j++) {
                // Read dish name
                String Name = StdIn.readString();

                // Consume the rest of the line
                String name = StdIn.readLine();

                String dishName = Name + name;

                // Read the number of ingredient IDs
                int numIngredients = StdIn.readInt();

                // Read ingredient IDs
                int[] ingredientIDs = new int[numIngredients];
                for (int k = 0; k < numIngredients; k++) {
                    ingredientIDs[k] = StdIn.readInt();
                }

                // Create Dish object
                Dish dish = new Dish(categoryVar[i], dishName, ingredientIDs);

                // Create MenuNode and insert at the front of menuVar
                menuVar[i] = new MenuNode(dish, menuVar[i]);
            }
        }
    }

    /** 
     * Find and return the MenuNode that contains the dish with dishName in the menuVar.
     * 
     *      ** GIVEN METHOD **
     *      ** DO NOT EDIT **
     * 
     * @param dishName - the name of the dish
     * @return the dish object corresponding to searched dish, null if dishName is not found.
     */

    public MenuNode findDish ( String dishName ) {

        MenuNode menuNode = null;

        // Search all categories since we don't know which category dishName is at
        for ( int category = 0; category < menuVar.length; category++ ) {

            MenuNode ptr = menuVar[category]; // set ptr at the front (first menuNode)
            
            while ( ptr != null ) { // while loop that searches the LL of the category to find the itemOrdered
                if ( ptr.getDish().getDishName().equals(dishName) ) {
                    return ptr;
                } else{
                    ptr = ptr.getNextMenuNode();
                }
            }
        }
        return menuNode;
    }

    /**
     * Find integer that corresponds to the index in menuVar and categoryVar arrays that has that category
     *              
     *      ** GIVEN METHOD **
     *      ** DO NOT EDIT **
     *
     * @param category - the category name
     * @return index of category in categoryVar
     */

    public int findCategoryIndex ( String category ) {
        int index = 0;
        for ( int i = 0; i < categoryVar.length; i++ ){
            if ( category.equalsIgnoreCase(categoryVar[i]) ) {
                index = i;
                break;
            }
        }
        return index;
    }

    /*
     * Stockroom methods
     */

    /**
     * PICK UP LINE OF THE METHOD:
     * *can I insert myself into your life? cuz you always help me sort 
     * out my problems and bring stability to my mine*
     * 
     * ***********
     * This method adds a StockNode into the stockVar hashtable.
     * 
     * 1. get the id of the given newNode and use a hash function to get the index at which the
     *    newNode is being inserted.
     * 
     * HASH FUNCTION: id % stockVarSize
     * 
     * 2. insert at the front of the linked list at the specific index
     * 
     * @param newNode - StockNode that needs to be inserted into StockVar
     */

    public void addStockNode ( StockNode newNode ) {

        // Get the ID of the newNode
        int id = newNode.getIngredient().getID();

        // Use the hash function to get the index
        int index = id % stockVarSize;
    
        // Insert at the front of the linked list at the specific index
        newNode.setNextStockNode(stockVar[index]);
        stockVar[index] = newNode;

    }

    /**
     * This method deletes an ingredient (aka the specific stockNode) from stockVar.
     * 
     * 1. find the node that corresponds to the ingredient (based on the ingredientName)
     * 
     * 2. delete the node from stockVar
     *      a) you have to visit each index and look at each node in the corresponding linked list 
     *      b) this is NOT efficient. Hashtables are not good if you can't use the key to find the item.
     * 
     * @param ingredientName - name of the ingredient
     */

    public void deleteStockNode ( String ingredientName ) {

    // Iterate through the stockVar array
    for (int i = 0; i < stockVarSize; i++) {
        StockNode current = stockVar[i];

        // Check if the linked list at the current index is not null
        if (current != null) {
            // Handle the case where the first node has the matching ingredientName
            if (current.getIngredient().getName().equals(ingredientName)) {
                stockVar[i] = current.getNextStockNode();
                return; // Node found and deleted, exit the method
            }

            // Iterate through the rest of the linked list
            StockNode prev = current;
            current = current.getNextStockNode();

            while (current != null) {
                // Check if the current node has the matching ingredientName
                if (current.getIngredient().getName().equals(ingredientName)) {
                    // Found the node with matching ingredientName, delete it
                    prev.setNextStockNode(current.getNextStockNode());
                    return; // Node found and deleted, exit the method
                }

                // Move to the next node
                prev = current;
                current = current.getNextStockNode();
            }
        }
    }
    }

    /**
     * This method finds an ingredient from StockVar (given the ingredientID)
     * 
     * 1. find the node based upon the ingredient ID (you can go to the specific index using the hash function!)
     *      (a) this is an efficient search as it looks only at the linked list which the key hash to
     * 2. find and return the node
     *  
     * @param ingredientID - the ID of the ingredient
     * @return the StockNode corresponding to the ingredientID, null otherwise
     */
   
    public StockNode findStockNode (int ingredientID) {

    // Calculate the hash index using the ingredientID
    int index = ingredientID % stockVarSize;

    // Get the linked list at the calculated index
    StockNode current = stockVar[index];

    // Iterate through the linked list to find the node with the matching ingredientID
    while (current != null) {
        if (current.getIngredient().getID() == ingredientID) {
            // Found the node with matching ingredientID, return it
            return current;
        }

        // Move to the next node
        current = current.getNextStockNode();
    }

    // Node with matching ingredientID not found
    return null;

    }

    /**
     * This method is to find an ingredient from StockVar (given the ingredient name).
     * 
     *      ** GIVEN METHOD **
     *      ** DO NOT EDIT **
     * 
     * @param ingredientName - the name of the ingredient
     * @return the specific ingredient StockNode, null otherwise
     */

    public StockNode findStockNode (String ingredientName) {
        
        StockNode stockNode = null;
        
        for ( int index = 0; index < stockVar.length; index ++ ){
            
            StockNode ptr = stockVar[index];
            
            while ( ptr != null ){
                if ( ptr.getIngredient().getName().equalsIgnoreCase(ingredientName) ){
                    return ptr;
                } else {  
                    ptr = ptr.getNextStockNode();
                }
            }
        }
        return stockNode;
    }

    /**
     * This method updates the stock amount of an ingredient.
     * 
     * 1. you will be given the ingredientName **OR** the ingredientID:
     *      a) the ingredientName is NOT null: find the ingredient and add the given stock amount to the
     *         current stock amount
     *      b) the ingredientID is NOT -1: find the ingredient and add the given stock amount to the
     *         current stock amount
     * 
     * (FOR FUTURE USE) SOMETIMES THE STOCK AMOUNT TO ADD MAY BE NEGATIVE (TO REMOVE STOCK)
     * 
     * @param ingredientName - the name of the ingredient
     * @param ingredientID - the id of the ingredient
     * @param stockAmountToAdd - the amount to add to the current stock amount
     */
    
    public void updateStock (String ingredientName, int ingredientID, int stockAmountToAdd) {
        
    StockNode stockNode;

    // Find the stock node based on the provided information
    if (ingredientName != null) {
        stockNode = findStockNode(ingredientName);
    } else if (ingredientID != -1) {
        stockNode = findStockNode(ingredientID);
    } else {
        // Both ingredientName and ingredientID are not provided, invalid input
        return;
    }

    // Update the stock amount if the stock node is found
    if (stockNode != null) {
        int currentStock = stockNode.getIngredient().getStockLevel();
        stockNode.getIngredient().setStockLevel(currentStock + stockAmountToAdd);
    }
    }

    /**
     * PICK UP LINE OF THE METHOD:
     * *are you a single ‘for’ loop? cuz i only have i’s for you*
     * 
     * ***********
     * This method goes over menuVar to update the price and profit of each dish,
     * using the stockVar hashtable to lookup for ingredient's costs.
     * 
     * 1. For each dish in menuVar, add up the cost for each ingredient (found in stockVar), 
     *    and multiply the total by 1.2 to get the final price.
     *      a) update the price of each dish
     *  HINT! --> you can use the methods you've previously made!
     * 
     * 2. Calculate the profit of each dish by getting the totalPrice of ingredients and subtracting from 
     *    the price of the dish itself.
     * 
     */

    public void updatePriceAndProfit() {

        for (int i = 0; i < menuVar.length; i++) {
            MenuNode currentMenuNode = menuVar[i];
    
            while (currentMenuNode != null) {
                // Get the Dish from the MenuNode
                Dish currentDish = currentMenuNode.getDish();
    
                // Calculate the total cost of ingredients
                double totalIngredientCost = 0.0;
                int[] ingredientIDs = currentDish.getStockID();
    
                for (int ingredientID : ingredientIDs) {
                    StockNode stockNode = findStockNode(ingredientID);
    
                    if (stockNode != null) {
                        totalIngredientCost += stockNode.getIngredient().getCost();
                    }
                }
    
                // Update the price of the dish
                double finalPrice = totalIngredientCost * 1.2;
                currentDish.setPriceOfDish(finalPrice);
    
                // Calculate and update the profit of the dish
                double profit = finalPrice - totalIngredientCost;
                currentDish.setProfit(profit);
    
                // Move to the next MenuNode in the linked list
                currentMenuNode = currentMenuNode.getNextMenuNode();
            }
        }
    }

    /**
     * PICK UP LINE OF THE METHOD:
     * *are you a decimal? cuz the thought of you 
     * always floats in my head and the two of use would make double*
     * 
     * ************
     * This method initializes and populates stockVar which is a hashtable where each index contains a 
     * linked list with StockNodes.
     * 
     * 1. set and read the inputFile (stock.in):
     *      a) first integer (on line 1) is the size of StockVar *** update stockVarSize AND create the stockVar array ***
     *      b) first integer of next line represents the ingredientID
     *          i) example: 101 on line 2
     *      c) use StdIn.readChar() to get rid of the space between the id and the name
     *      d) the string that follows is the ingredient name (NOTE! --> there are spaces between certain strings)
     *          i) example: Lettuce
     *      e) the double on the next line corresponds to the ingredient's cost
     *          i) example: 3.12 on line 3
     *      f) the next integer is the stock amount for that ingredient
     *          i) example: 30 on line 3
     * 
     * 2. create a Ingredient object followed by a StockNode then add to stockVar
     *      HINT! --> you may use previous methods written to help you!
     * 
     * @param inputFile - the input file with the ingredients and all their information (stock.in)
     */

    public void createStockHashTable (String inputFile){
        
        StdIn.setFile(inputFile);

        // Read the size of StockVar
        stockVarSize = StdIn.readInt();
    
        // Create the stockVar array
        stockVar = new StockNode[stockVarSize];
    
        while (!StdIn.isEmpty()) {
            // Read the ingredientID
            int ingredientID = StdIn.readInt();
    
            // Read the space between ID and name
            StdIn.readChar();
    
            // Read the ingredient name
            String ingredientName = StdIn.readLine();
    
            // Read the cost of the ingredient
            double cost = StdIn.readDouble();

            StdIn.readChar();
    
            // Read the stock amount for the ingredient
            int stockAmount = StdIn.readInt();
    
            // Create an Ingredient object
            Ingredient ingredient = new Ingredient(ingredientID, ingredientName, stockAmount, cost);
    
            // Create a StockNode
            StockNode stockNode = new StockNode(ingredient, null);
    
            // Add the StockNode to stockVar
            addStockNode(stockNode);
        }
    }

    /*
     * Transaction methods
     */

    /**
     * This method adds a TransactionNode to the END of the transactions linked list.
     * The front of the list is transactionVar.
     *
     * 1. create a new TransactionNode with the TransactionData paramenter.
     * 2. add the TransactionNode at the end of the linked list transactionVar.
     * 
     * @param data - TransactionData node to be added to transactionVar
     */

    public void addTransactionNode ( TransactionData data ) { // method adds new transactionNode to the end of LL
       
    // Create a new TransactionNode with the provided TransactionData
    TransactionNode newNode = new TransactionNode(data, null);

    // If transactionVar is null, set the new node as the front
    if (transactionVar == null) {
        transactionVar = newNode;
    } else {
        // Otherwise, traverse the list to find the end and add the new node
        TransactionNode current = transactionVar;
        while (current.getNext() != null) {
            current = current.getNext();
        }
        current.setNext(newNode);
    }
    }

    /**
     * PICK UP LINE OF THE METHOD:
     * *are you the break command? cuz everything else stops when I see you*
     * 
     * *************
     * This method checks if there's enough in stock to prepare a dish.
     * 
     * 1. use findDish() method to find the menuNode node for dishName
     * 
     * 2. retrieve the Dish, then traverse ingredient array within the Dish
     * 
     * 3. return boolean based on whether you can sell the dish or not
     * HINT! --> once you determine you can't sell the dish, break and return
     * 
     * @param dishName - String of dish that's being requested
     * @param numberOfDishes - int of how many of that dish is being ordered
     * @return boolean
     */

    public boolean checkDishAvailability ( String dishName, int numberOfDishes ){
        
    // Find the menuNode node for dishName
    MenuNode menuNode = findDish(dishName);

    // Check if the menuNode is found
    if (menuNode != null) {
        // Retrieve the Dish from the menuNode
        Dish dish = menuNode.getDish();

        // Traverse the ingredient array within the Dish
        int[] stockIDs = dish.getStockID();
        for (int stockID : stockIDs) {
            // Find the StockNode for the ingredient
            StockNode stockNode = findStockNode(stockID);

            // Check if the StockNode is found
            if (stockNode != null) {
                // Check if there's enough stock for the requested number of dishes
                if (stockNode.getIngredient().getStockLevel() < numberOfDishes) {
                    return false; // Not enough stock for the dish
                }
            } else {
                return false; // StockNode not found for the ingredient
            }
        }

        return true; // Enough stock to prepare the dish
    }

    return false; // Dish not found in the menu
    }

    /**
     * PICK UP LINE OF THE METHOD:
     * *if you were a while loop and I were a boolean, we could run 
     * together forever because I’ll always stay true to you*
     * 
     * ***************
     * This method simulates a customer ordering a dish. Use the checkDishAvailability() method to check whether the dish can be ordered.
     * 
     * If the dish can be prepared
     *      - create a TransactionData object of type "order" where the item is the dishName, the amount is the quantity being ordered, and profit is the dish profit multiplied by quantity.
     *      - then add the transaction as a successful transaction (call addTransactionNode()) and updates the stock accordingly.
     * 
     * If the dish cannot be prepared
     *      - create a TransactionData object of type "order" where the item is the dishName, the amount is the quantity being ordered, and profit is 0 (zero).
     *      - then add the transaction as an UNsuccessful transaction and,
     *      - simulate the customer trying to order other dishes in the same category linked list:
     *          - if the dish that comes right after the dishName can be prepared, great. If not, try the next one and so on.
     *          - you might have to traverse through the entire category searching for a dish that can be prepared. If you reach the end of the list, start from the beginning until you have visited EVERY dish in the category.
     *          - It is possible that no dish in the entire category can be prepared.
     *          - Note: the next dish the customer chooses is always the one that comes right after the one that could not be prepared. 
     * 
     * 
     * @param dishName - String of dish that's been ordered
     * @param quantity - int of how many of that dish has been ordered
     */

    public void order ( String dishName, int quantity ){

    // Check if the dish can be prepared
    if (checkDishAvailability(dishName, quantity)) {
        // Dish can be prepared
        MenuNode menuNode = findDish(dishName);
        Dish dish = menuNode.getDish();

        // Calculate profit
        double profit = dish.getProfit() * quantity;

        // Create a TransactionData object
        TransactionData transactionData = new TransactionData("order", dishName, quantity, profit, true);

        // Add the transaction as a successful transaction
        addTransactionNode(transactionData);

        // Update stock accordingly
        int[] stockIDs = dish.getStockID();
        for (int stockID : stockIDs) {
            updateStock(null, stockID, -quantity); // Assuming updateStock() can handle null ingredientName
        }
    } else {
        // Dish cannot be prepared
        // Create a TransactionData object with profit 0
        TransactionData transactionData = new TransactionData("order", dishName, quantity, 0, false);

        // Add the transaction as an unsuccessful transaction
        addTransactionNode(transactionData);

        // Simulate the customer trying to order other dishes in the same category
        MenuNode menuNode = findDish(dishName);
        MenuNode nextMenuNode = menuNode.getNextMenuNode();

        String category = menuNode.getDish().getCategory();
        int id = findCategoryIndex(category);

        MenuNode nextMenu = menuVar[id];

        while (nextMenuNode != null) {
            if (checkDishAvailability(nextMenuNode.getDish().getDishName(), quantity)) {
                Dish prof = nextMenuNode.getDish();
                double profit = prof.getProfit() * quantity;
                // Create a TransactionData object for the successful transaction
                TransactionData successfulTransactionData = new TransactionData("order", nextMenuNode.getDish().getDishName(), quantity, profit, true);
                // Add the transaction as a successful transaction
                addTransactionNode(successfulTransactionData);
                // Update stock accordingly for the successful transaction
                int[] successfulStockIDs = nextMenuNode.getDish().getStockID();
                for (int stockID : successfulStockIDs) {
                    updateStock(null, stockID, -quantity); // Assuming updateStock() can handle null ingredientName
                }
                return; // Exit the method after ordering the next dish
            }
            TransactionData unsuccessfulTransactionData = new TransactionData("order", nextMenuNode.getDish().getDishName(), quantity, 0, false);
            // Add the transaction as an unsuccessful transaction
            addTransactionNode(unsuccessfulTransactionData);
            nextMenuNode = nextMenuNode.getNextMenuNode();
        }

        if (nextMenuNode == null){
        while ( nextMenu != menuNode ) { // while loop that searches the LL of the category to find the itemOrdered
           if (checkDishAvailability(nextMenu.getDish().getDishName(), quantity)) {
                Dish prof = nextMenu.getDish();
                double profit = prof.getProfit() * quantity;
                // Create a TransactionData object for the successful transaction
                TransactionData successfulTransactionData = new TransactionData("order", nextMenu.getDish().getDishName(), quantity, profit, true);
                // Add the transaction as a successful transaction
                addTransactionNode(successfulTransactionData);
                // Update stock accordingly for the successful transaction
                int[] successfulStockIDs = nextMenu.getDish().getStockID();
                for (int stockID : successfulStockIDs) {
                    updateStock(null, stockID, -quantity); // Assuming updateStock() can handle null ingredientName
                }
                return; // Exit the method after ordering the next dish
            }
            // Create a TransactionData object for each unsuccessful transaction
            TransactionData unsuccessfulTransactionData = new TransactionData("order", nextMenu.getDish().getDishName(), quantity, 0, false);
            // Add the transaction as an unsuccessful transaction
            addTransactionNode(unsuccessfulTransactionData);
            nextMenu = nextMenu.getNextMenuNode();
        }
    }
    }
        
        // If you reach here, no dish in the entire category can be prepared
    }

    /**
     * This method returns the total profit for the day
     *
     * The profit is computed by traversing the transaction linked list (transactionVar) 
     * adding up all the profits for the day
     * 
     * @return profit - double value of the total profit for the day
     */

    public double profit () {

        double totalProfit = 0.0;
    
        // Traverse the transaction linked list
        TransactionNode currentTransactionNode = transactionVar;
        while (currentTransactionNode != null) {
            // Add up the profit for each transaction
            totalProfit += currentTransactionNode.getData().getProfit();
            
            // Move to the next transaction node
            currentTransactionNode = currentTransactionNode.getNext();
        }
    
        return totalProfit;
    }

    /**
     * This method simulates donation requests, successful or not.
     * 
     * 1. check whether the profit is > 50 and whether there's enough ingredients in stock.
     * 
     * 2. add transaction to transactionVar
     * 
     * @param ingredientName - String of ingredient that's been requested
     * @param quantity - int of how many of that ingredient has been ordered
     * @return void
     */

    public void donation ( String ingredientName, int quantity ){

    // Find the stock node for the given ingredient
    StockNode stockNode = findStockNode(ingredientName);

    // Check if the profit is > 50 and there's enough stock
    if (profit() > 50 && stockNode != null && stockNode.getIngredient().getStockLevel() >= quantity) {
        // Transaction is successful
        TransactionData data = new TransactionData("donation", ingredientName, quantity, 0, true);
        addTransactionNode(data);

        // Update stock level
        stockNode.getIngredient().updateStockLevel(-quantity);
    } else {
        // Transaction is unsuccessful
        TransactionData data = new TransactionData("donation", ingredientName, quantity, 0, false);
        addTransactionNode(data);
    }
    }

    /**
     * This method simulates restock orders
     * 
     * 1. check whether the profit is sufficient to pay for the total cost of ingredient
     *      a) (how much each ingredient costs) * (quantity)
     *      b) if there is enough profit, adjust stock and profit accordingly
     * 
     * 2. add transaction to transactionVar
     * 
     * @param ingredientName - ingredient that's been requested
     * @param quantity - how many of that ingredient needs to be ordered
     */

    public void restock ( String ingredientName, int quantity ){

     // You may need to implement logic to handle ingredientName
     StockNode stockNode = findStockNode(ingredientName);

     if (stockNode != null) {
         Ingredient ingredient = stockNode.getIngredient();
         if (ingredient != null) {
             double costToRestock = ingredient.getCost() * quantity;
 
             // Check if there is enough profit to restock
             if (profit() >= costToRestock) {
                 // Adjust stock and profit accordingly
                 ingredient.updateStockLevel(quantity);
                 double profit = -costToRestock; // Negative because it's a cost for the restaurant
 
                 // Add a transaction to transactionVar
                 TransactionData transactionData = new TransactionData("restock", ingredientName, quantity, profit, true);
                 addTransactionNode(transactionData);
             } else {
                 // If there isn't enough profit, record the failed transaction
                 TransactionData failedTransaction = new TransactionData("restock", ingredientName, quantity, 0, false);
                 addTransactionNode(failedTransaction);
             }
         }
     }
    }

   /*
    * Seat guests/customers methods
    */

    /**
     * Method to populate tables (which is a 2d integer array) based upon input file
     * 
     * First row of tables[][]: contains the total number of seats available at a table (each table is an index in first row)
     * Second row of tables[][]: initializes all indices to 0
     *      0 --> table is available
     *      1 --> table is occupied
     * 
     *          ** GIVEN METHOD **
     *          ** DO NOT EDIT **
     * 
     * @param inputFile - tables1.in (contains all the tables in the RUHungry restaurant)
     * @return void (aka nothing)
     */

    public void createTables ( String inputFile ) { 

        StdIn.setFile(inputFile);
        int numberOfTables = StdIn.readInt();
        tablesInfo = new int[2][numberOfTables];
        
        for ( int t = 0; t < numberOfTables; t++ ) {
            tablesInfo[0][t] = StdIn.readInt() * StdIn.readInt();
        }
    }

    /**
     * PICK UP LINE OF THE METHOD:
     * *are you a linked list? cuz nothing could stock up to you and 
     * you’re pretty queue(te)*
     * 
     * ***************
     * This method simulates seating guests at tables. You are guaranteed to be able to sit everyone from the waitingQueue eventually.
     * 
     * 1. initialize a tables array for people that are currently sitting
     * 
     * 2. initialize leftQueueVar a People queue that represents the people that have left the restaurant
     * 
     * 3. while there are people waiting to be sat:
     *      - Starting from index 0 (zero), seat the next party in the first available table that fits their party.
     *      - If there is no available table for the next party, kick a party out from the tables array:
     *          1. starting at index 0 (zero), find the first table big enough to hold the next party in line.
     *          2. remove the current party, add them to the leftQueueVar.
     *          3. seat the next party in line.
     * 
     * tableInfo contains the number of seats per table as well as if the table is occupied or not.
     * tables contains the People object (party) currently at the table.
     * 
     * Note: After everyone has been seated (waitingQueue is empty), remove all the parties from tables and add then to the leftQueueVar.
     * 
     * @param waitingQueue - queue containing people waiting to be seated (each element in queue is a People <-- you are given this class!)
     */

    public void seatAllGuests ( Queue<People> waitingQueue ) {

        // WRITE YOUR CODE HERE
    }
}