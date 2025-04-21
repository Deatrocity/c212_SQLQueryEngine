import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * this is the IO utility class
 */
public class IO {

    /**
     * Reads the table's data from a csv file
     *
     * Implement the following algorithm
     *
     * Open the csv file from the folder (corresponding to the tablename)
     *   For each line in the csv file
     *     Parse the line to get attribute values
     *     Create a new tuple with the schema of the table
     *     Set the tuple values to the attribute values
     *     Add the tuple to the table
     * Close file
     *
     * Return table
     * @param tablename name of the table (also the CSV file name, without extension)
     * @param schema schema describing the structure of the table
     * @param folder directory where the file is stored
     * @return Table populated with Tuples from the CSV
     */
    public static ITable readTable(String tablename, ISchema schema, String folder) {
        // Create a new Table with the given schema
        ITable table = new Table(tablename, schema);

        try (BufferedReader reader = new BufferedReader(
                new FileReader(folder + "/" + tablename + ".csv"))) {

            String line;
            // Read each line (tuple) from the file
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");

                // Create a tuple for this line
                Tuple tuple = new Tuple(schema);

                // Set each value in the tuple according to its schema-defined type
                for (int i = 0; i < values.length; i++) {
                    String type = schema.getType(i);
                    String val = values[i].trim();

                    Object parsed;
                    switch (type) {
                        case "Integer" -> parsed = Integer.parseInt(val); // Convert string to Integer
                        default -> parsed = val; // Keep as String by default
                    }

                    // Set the parsed value at index i
                    tuple.setValue(i, parsed);
                }

                // Add the tuple to the table
                table.addTuple(tuple);
            }

        } catch (IOException e) {
            // Any I/O error is reported to standard error
            System.err.println("Error reading table '" + tablename + "': " + e.getMessage());
        }

        return table;
    }


    /**
     * Writes the tables' data to a csv file
     *
     * Implement the following algorithm
     *
     * Open the csv file from the folder (corresponding to the tablename)
     * Clear all file content
     * For each tuple in table
     *   Write the tuple values to the file in csv format
     *
     * @param table the table to write
     * @param folder the folder where CSV is stored
     */
    public static void writeTable(ITable table, String folder) {
        String path = folder + "/" + table.getName() + ".csv";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            // Loop over each tuple in the table
            for (ITuple tuple : table.getTuples()) {
                Object[] values = tuple.getValues();

                // Write each value, separated by commas
                for (int i = 0; i < values.length; i++) {
                    writer.write(values[i].toString());

                    // Add comma unless it's the last value
                    if (i < values.length - 1) {
                        writer.write(",");
                    }
                }

                // Write a newline after each tuple
                writer.newLine();
            }

        } catch (IOException e) {
            // Print any error to standard error
            System.err.println("Error writing table '" + table.getName() + "': " + e.getMessage());
        }
    }

    /**
     * Prints the table to console (mainly used to print the output of the select query)
     *
     * Implements the following algorithm
     *
     * Print the attribute names from the schema as tab separated values
     * For each tuple in the table
     *   Print the values in tab separated format
     *
     *
     * @param table the table to print
     * @param schema the schema for formatting attribute names and types
     */
    public static void printTable(ITable table, ISchema schema) {
        // Print attribute names in order (tab-separated)
        Map<Integer, String> attrs = schema.getAttributes();
        int attrCount = attrs.size();

        for (int i = 0; i < attrCount; i++) {
            String attrName = schema.getName(i);
            System.out.print(attrName);
            if (i < attrCount - 1) {
                System.out.print("\t");
            }
        }
        System.out.println();

        // Print each tuple
        for (ITuple tuple : table.getTuples()) {
            Object[] values = tuple.getValues();
            for (int i = 0; i < values.length; i++) {
                System.out.print(values[i]);
                if (i < values.length - 1) {
                    System.out.print("\t");
                }
            }
            System.out.println();
        }
    }


    /**
     * Writes a tuple to a csv file
     *
     * Implements the following algorithm
     *
     * Open the csv file from the folder (corresponding to the tablename)
     * Append the tuple (as array of strings) in the csv format to the file
     *
     * @param tableName the table to append to
     * @param values the tuple values to write
     * @param folder the folder where CSV is stored
     */
    public static void writeTuple(String tableName, Object[] values, String folder) {
        String path = folder + "/" + tableName + ".csv";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, true))) {
            // Write each value separated by commas
            for (int i = 0; i < values.length; i++) {
                writer.write(values[i].toString());

                // Add comma unless it's the last value
                if (i < values.length - 1) {
                    writer.write(",");
                }
            }

            // End the line after each tuple
            writer.newLine();

        } catch (IOException e) {
            System.err.println("Error writing tuple to '" + tableName + "': " + e.getMessage());
        }
    }

    /**
     * Reads and parses the schema, creates schema objects and (empty) tables and adds them to the provided database
     * The schema is stored in a text file:
     *
     * Implements the following algorithm
     *
     * Open the schema file
     * For each line
     *   Parse the line to get the table name, attribute names and attribute types
     *   Create an attribute map of (index, att_name:att_type) pairs
     *   For each attribute
     *     Store the index and name:type pair in the map (index represents the position of attribute in the schema)
     *   Create a new schema object with this attribute map
     *   Add the schema object to the database
     *   Create a new table object with the table name and the schema object
     *   Add the table to the database
     *
     * @param schemaFileName name of the schema file (e.g., "schema.txt")
     * @param folderName folder containing the schema and CSV files
     * @param db the Database object to populate
     */
    public static void readSchema(String schemaFileName, String folderName, Database db) {
        try (BufferedReader reader = new BufferedReader(new FileReader(folderName + "/" + schemaFileName))) {
            String line;

            // Read each schema line
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip empty lines
                if (line.isEmpty()) continue;

                int parenStart = line.indexOf("(");
                int parenEnd = line.indexOf(")");

                // Extract table name
                String tableName = line.substring(0, parenStart).trim();

                // Extract attribute list: attrName:Type
                String[] attrTokens = line.substring(parenStart + 1, parenEnd).split(",");
                Map<Integer, String> attrMap = new HashMap<>();

                for (int i = 0; i < attrTokens.length; i++) {
                    attrMap.put(i, attrTokens[i].trim()); // e.g., sid:String
                }

                // Create schema and empty table
                ISchema schema = new Schema(attrMap);
                ITable table = new Table(tableName, schema);

                // Add both to the database
                db.addSchema(schema);
                db.addTable(table);
            }

        } catch (IOException e) {
            System.err.println("Error reading schema file: " + e.getMessage());
        }
    }
}
