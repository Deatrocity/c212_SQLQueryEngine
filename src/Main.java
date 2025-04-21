import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Database db = new Database("db", "schema.txt");
        // Populate the database from CSV files
        db.populateDB();

        System.out.print("$ ");

        while (true) {
            String query = scanner.nextLine();
            runQuery(query, db);
            System.out.print("$ ");
        }
    }

    /**
     * Runs the given query on the database
     *
     * Implements the following algorithm
     *
     * Determine the type of query (from select, insert or delete)
     * If select query
     *   Select data
     *   Print results
     * Else if insert query
     *   Insert data
     * Else if delete is given
     *   Delete data
     *
     * @param query query the SQL query to execute
     * @param db db the database object to operate on
     */
    public static void runQuery(String query, Database db) {
        try {
            // Normalize query string
            String lowered = query.toLowerCase().trim();

            // If query is a SELECT, fetch and print results
            if (lowered.startsWith("select")) {
                ITable result = db.selectData(query);
                IO.printTable(result, result.getSchema());

                // If query is an INSERT, update the database
            } else if (lowered.startsWith("insert")) {
                db.insertData(query);

                // If query is a DELETE, remove data
            } else if (lowered.startsWith("delete")) {
                db.deleteData(query);

                // Unrecognized query type
            } else {
                System.err.println("Unknown query type.");
            }

            // Catch and report invalid query errors
        } catch (InvalidQueryException e) {
            System.err.println("Invalid Query: " + e.getMessage());

            // Catch and report any other unexpected errors
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}