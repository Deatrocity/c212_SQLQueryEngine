import java.util.*;

/**
 * The main database class
 * Database as a list of tables, list of schemas and a folder name where the database is stored
 * Database is stored (on the disk) in the form of three csv files and schema text file
 */
class Database {
    private List<ITable> tables;    // List of all tables in the database
    private List<ISchema> schemas;  // List of all schemas (table structures)
    private String folderName;      // Folder where CSV files are stored

    /**
     * Constructor
     * Creates the empty tables and schema lists
     * Reads the schema file to add schemas to the database
     * Populates the database table (with the data read from the csv files)
     * @param folderName folder containing CSV and schema.txt
     * @param schemaFileName name of the schema file (e.g., schema.txt)
     */
    public Database(String folderName, String schemaFileName) {
        this.folderName = folderName;
        this.tables = new ArrayList<>();
        this.schemas = new ArrayList<>();

        // Load schema and create empty tables
        IO.readSchema(schemaFileName, folderName, this);
    }

    /**
     * Adds a table to the database
     * @param table the table to add
     */
    public void addTable(ITable table) {
        this.tables.add(table);
    }

    /**
     * Adds a table schema to the database
     * @param schema the schema to add
     */
    public void addSchema(ISchema schema) {
        this.schemas.add(schema);
    }

    /**
     * Return the list of tables in the database
     * @return list of ITable objects
     */
    public List<ITable> getTables() {
        return this.tables;
    }

    /**
     * Returns the list of schemas in the database
     * @return list of ISchema objects
     */
    public List<ISchema> getSchemas() {
        return this.schemas;
    }

    /**
     * The list of tables in the database is initialized with empty tables in the constructor
     * An empty table has a name and an empty list of tuples
     * This method sets the empty table in the list to the one provided as a parameter
     * @param table the updated table with data
     */
    public void updateTable(ITable table) {
        for (int i = 0; i < this.tables.size(); i++) {
            if (this.tables.get(i).getName().equals(table.getName())) {
                this.tables.set(i, table);
                return;
            }
        }
    }

    /**
     * Populates the database
     *
     * Implements the following algorithm
     *
     * For each table in the db (tables are initially empty)
     *   Get the table's data from the csv file (by calling the read table method)
     *   Update the table (by calling the udpate table method)
     */
    public void populateDB() {
        for (ITable table : tables) {
            ISchema schema = table.getSchema();
            ITable populated = IO.readTable(table.getName(), schema, folderName);
            updateTable(populated);
        }
    }

    /**
     * Insert data into a table based upon the insert query
     * If the query is invalid throws an InvalidQueryException
     *
     * Implements the following algorithm
     *
     * Parse the insert into clause to get the table name, attribute name(s) and value(s)
     * If the query in not valid
     *   Throw an invalid query exception
     *   Exit
     * Create a new tuple with the schema of the table
     * Set the tuple values to the values from the query
     * Open the file corresponding to the table name
     * Append the tuple values (as comma separated values) to the end of the file
     *
     * @param query
     * @throws InvalidQueryException
     */
    public void insertData(String query) throws InvalidQueryException {
        try {
            // Normalize and remove extra spaces
            query = query.trim();

            // Check structure: INSERT INTO tablename (a1, a2, ...) VALUES (v1, v2, ...)
            if (!query.toLowerCase().contains("insert into") || !query.toLowerCase().contains("values")) {
                throw new InvalidQueryException("Query must contain 'INSERT INTO' and 'VALUES'");
            }

            // Split around "VALUES"
            String[] parts = query.split("(?i)values");
            if (parts.length != 2) throw new InvalidQueryException("Invalid VALUES clause");

            // Left of VALUES: INSERT INTO table (columns)
            String left = parts[0].trim();
            String right = parts[1].trim();

            // Extract table name and attributes
            int tableStart = left.toLowerCase().indexOf("insert into") + "insert into".length();
            int parenStart = left.indexOf("(");
            int parenEnd = left.indexOf(")");

            String tableName = left.substring(tableStart, parenStart).trim();
            String[] attributes = left.substring(parenStart + 1, parenEnd).split(",");

            for (int i = 0; i < attributes.length; i++) {
                attributes[i] = attributes[i].trim();
            }

            // Extract values: ('val1', 'val2', ...)
            int valStart = right.indexOf("(");
            int valEnd = right.indexOf(")");
            String[] rawValues = right.substring(valStart + 1, valEnd).split(",");

            if (attributes.length != rawValues.length)
                throw new InvalidQueryException("Number of attributes and values must match");

            Object[] values = new Object[rawValues.length];

            // Get schema and type-check/convert each value
            ISchema schema = null;
            for (ITable t : tables) {
                if (t.getName().equalsIgnoreCase(tableName)) {
                    schema = t.getSchema();
                    break;
                }
            }

            if (schema == null) throw new InvalidQueryException("Table not found: " + tableName);

            for (int i = 0; i < rawValues.length; i++) {
                String val = rawValues[i].trim().replaceAll("^'|'$", ""); // strip single quotes
                for (int j : schema.getAttributes().keySet()) {
                    String name = schema.getName(j);
                    String type = schema.getType(j);

                    if (name.equals(attributes[i])) {
                        if (type.equals("Integer")) {
                            values[i] = Integer.parseInt(val);
                        } else {
                            values[i] = val;
                        }
                    }
                }
            }

            // Append tuple to CSV
            IO.writeTuple(tableName, values, folderName);

        } catch (Exception e) {
            throw new InvalidQueryException("Failed to insert: " + e.getMessage());
        }
    }

    /**
     * Selects data from a table (and returns it in the form of a results table)
     * If the query in not valid, throws an InvalidQueryException
     *
     * A query is valid if
     *
     * 1.	It has a select clause (select keyword followed by at least one attribute name)
     * 2.	It has a from clause (from keyword followed by a table name)
     * 3.	All the attribute names in the select clause are in the schema
     * 4.	The table name in the from clause is in the schema
     * 5.	All the attribute names in the where clause (if present) are in the schema
     * 6.	The attribute name in the order by clause (if present) is in the schema
     *
     * Implements the following algorithm
     *
     * Parse the query to get the select, from, where and order by clauses and the attribute and table names and condition
     * If the query is not valid
     *   Throw an invalid query exception
     *   Exit
     * Create a new results schema based with the attributes from the select clause
     * Create a new result table
     * For each tuple in the table
     *   If the tuple matches the where clause condition(s)
     *     Create a new results tuple using the result schema
     *     Set the results tuple values to the current tuple corresponding values
     *     Add the results tuple to the result table
     * Return results table
     *
     *
     * @param query
     * @return
     * @throws InvalidQueryException
     */
    public ITable selectData(String query) throws InvalidQueryException {
        try {
            query = query.trim();

            // Basic validation
            if (!query.toLowerCase().contains("select") || !query.toLowerCase().contains("from")) {
                throw new InvalidQueryException("Missing SELECT or FROM clause.");
            }

            // Parse parts
            String selectPart = query.substring(query.toLowerCase().indexOf("select") + 6, query.toLowerCase().indexOf("from")).trim();
            String afterFrom = query.substring(query.toLowerCase().indexOf("from") + 4).trim();

            String[] selectedAttributes = selectPart.split(",");
            for (int i = 0; i < selectedAttributes.length; i++) {
                selectedAttributes[i] = selectedAttributes[i].trim();
            }

            String tableName;
            String whereClause = null;

            // Handle optional WHERE clause
            if (afterFrom.toLowerCase().contains("where")) {
                String[] fromParts = afterFrom.split("(?i)where");
                tableName = fromParts[0].trim();
                whereClause = fromParts[1].trim();
            } else {
                tableName = afterFrom.trim();
            }

            // Locate the table and schema
            ITable sourceTable = null;
            ISchema sourceSchema = null;

            for (ITable t : tables) {
                if (t.getName().equalsIgnoreCase(tableName)) {
                    sourceTable = t;
                    sourceSchema = t.getSchema();
                    break;
                }
            }

            if (sourceTable == null || sourceSchema == null) {
                throw new InvalidQueryException("Table not found: " + tableName);
            }

            // Validate selected attributes
            List<String> schemaAttrs = new ArrayList<>();
            for (int i : sourceSchema.getAttributes().keySet()) {
                schemaAttrs.add(sourceSchema.getName(i));
            }

            for (String attr : selectedAttributes) {
                if (!schemaAttrs.contains(attr)) {
                    throw new InvalidQueryException("Unknown attribute: " + attr);
                }
            }

            // Build result schema
            Map<Integer, String> newAttrs = new HashMap<>();
            int idx = 0;
            for (int i : sourceSchema.getAttributes().keySet()) {
                String name = sourceSchema.getName(i);
                String type = sourceSchema.getType(i);
                if (Arrays.asList(selectedAttributes).contains(name)) {
                    newAttrs.put(idx++, name + ":" + type);
                }
            }

            ISchema resultSchema = new Schema(newAttrs);
            ITable resultTable = new Table("result", resultSchema);

            // Iterate over all tuples in the source table
            for (ITuple tuple : sourceTable.getTuples()) {

                boolean match = true;

                // If there's a WHERE clause, evaluate the condition
                if (whereClause != null) {
                    // Support simple format: attr = value
                    String[] ops;
                    String operator = null;

                    // Identify which operator is used
                    if (whereClause.contains("!=")) {
                        ops = whereClause.split("!=");
                        operator = "!=";
                    } else if (whereClause.contains(">=")) {
                        ops = whereClause.split(">=");
                        operator = ">=";
                    } else if (whereClause.contains("<=")) {
                        ops = whereClause.split("<=");
                        operator = "<=";
                    } else if (whereClause.contains("=")) {
                        ops = whereClause.split("=");
                        operator = "=";
                    } else if (whereClause.contains(">")) {
                        ops = whereClause.split(">");
                        operator = ">";
                    } else if (whereClause.contains("<")) {
                        ops = whereClause.split("<");
                        operator = "<";
                    } else {
                        throw new InvalidQueryException("Unsupported WHERE operator.");
                    }

                    if (ops.length != 2) {
                        throw new InvalidQueryException("Malformed WHERE clause.");
                    }

                    String lhs = ops[0].trim();
                    String rhs = ops[1].trim().replaceAll("^'|'$", ""); // remove quotes

                    int lhsIndex = -1;
                    String lhsType = null;

                    for (int i : sourceSchema.getAttributes().keySet()) {
                        if (sourceSchema.getName(i).equals(lhs)) {
                            lhsIndex = i;
                            lhsType = sourceSchema.getType(i);
                            break;
                        }
                    }

                    if (lhsIndex == -1) throw new InvalidQueryException("Unknown attribute in WHERE: " + lhs);

                    Object leftVal = tuple.getValue(lhsIndex);

                    // Evaluate condition
                    switch (lhsType) {
                        case "Integer":
                            int lInt = (Integer) leftVal;
                            int rInt = Integer.parseInt(rhs);
                            match = switch (operator) {
                                case "=" -> lInt == rInt;
                                case "!=" -> lInt != rInt;
                                case ">" -> lInt > rInt;
                                case "<" -> lInt < rInt;
                                case ">=" -> lInt >= rInt;
                                case "<=" -> lInt <= rInt;
                                default -> throw new InvalidQueryException("Invalid operator for Integer");
                            };
                            break;

                        case "String":
                            String lStr = (String) leftVal;
                            match = switch (operator) {
                                case "=" -> lStr.equals(rhs);
                                case "!=" -> !lStr.equals(rhs);
                                default -> throw new InvalidQueryException("Only = and != supported for Strings");
                            };
                            break;

                        default:
                            throw new InvalidQueryException("Unsupported type: " + lhsType);
                    }
                }

                if (match) {
                    // Build a result tuple from selected attributes
                    Tuple newTuple = new Tuple(resultSchema);
                    Object[] newVals = new Object[selectedAttributes.length];

                    for (int i = 0; i < selectedAttributes.length; i++) {
                        for (int j : sourceSchema.getAttributes().keySet()) {
                            if (sourceSchema.getName(j).equals(selectedAttributes[i])) {
                                newVals[i] = tuple.getValue(j);
                            }
                        }
                    }

                    newTuple.setValues(newVals);
                    resultTable.addTuple(newTuple);
                }
            }

            return resultTable;

        } catch (Exception e) {
            throw new InvalidQueryException("SELECT failed: " + e.getMessage());
        }
    }

    /**
     * Delete data from a table
     * If the query in not valid, throws an InvalidQueryException
     *
     * Implements the following algorithm
     *
     * Parse the query to get the from and where clauses
     * Parse the from clause to get the table name
     * If the query in not valid
     *   Throw an invalid query exception
     *   Exit
     * If where clause is not empty
     *   Parse the where clause to get the the condition
     *   For each tuple in the table
     *     If the where clause condition is true
     *       Remove the tuple from the table
     * Else
     *   For each tuple in the table
     *     Remove the tuple from the table
     * Write the table to the file
     *
     * @param query
     * @throws InvalidQueryException
     */
    public void deleteData(String query) throws InvalidQueryException {
        try {
            query = query.trim();

            // Basic check
            if (!query.toLowerCase().startsWith("delete from")) {
                throw new InvalidQueryException("Missing DELETE FROM clause.");
            }

            // Remove "DELETE FROM"
            String clause = query.substring(11).trim();

            String tableName;
            String whereClause = null;

            // Check for WHERE clause
            if (clause.toLowerCase().contains("where")) {
                String[] parts = clause.split("(?i)where");
                tableName = parts[0].trim();
                whereClause = parts[1].trim();
            } else {
                tableName = clause.trim();
            }

            // Locate the table and schema
            ITable targetTable = null;
            ISchema schema = null;

            for (ITable t : tables) {
                if (t.getName().equalsIgnoreCase(tableName)) {
                    targetTable = t;
                    schema = t.getSchema();
                    break;
                }
            }

            if (targetTable == null || schema == null) {
                throw new InvalidQueryException("Table not found: " + tableName);
            }

            List<ITuple> original = new ArrayList<>(targetTable.getTuples());
            List<ITuple> toKeep = new ArrayList<>();

            for (ITuple tuple : original) {
                boolean match = false;

                if (whereClause != null) {
                    // Support format: attr = value
                    String[] ops;
                    String operator = null;

                    if (whereClause.contains("!=")) {
                        ops = whereClause.split("!=");
                        operator = "!=";
                    } else if (whereClause.contains(">=")) {
                        ops = whereClause.split(">=");
                        operator = ">=";
                    } else if (whereClause.contains("<=")) {
                        ops = whereClause.split("<=");
                        operator = "<=";
                    } else if (whereClause.contains("=")) {
                        ops = whereClause.split("=");
                        operator = "=";
                    } else if (whereClause.contains(">")) {
                        ops = whereClause.split(">");
                        operator = ">";
                    } else if (whereClause.contains("<")) {
                        ops = whereClause.split("<");
                        operator = "<";
                    } else {
                        throw new InvalidQueryException("Unsupported WHERE operator.");
                    }

                    if (ops.length != 2) {
                        throw new InvalidQueryException("Malformed WHERE clause.");
                    }

                    String lhs = ops[0].trim();
                    String rhs = ops[1].trim().replaceAll("^'|'$", "");

                    int lhsIndex = -1;
                    String lhsType = null;

                    for (int i : schema.getAttributes().keySet()) {
                        if (schema.getName(i).equals(lhs)) {
                            lhsIndex = i;
                            lhsType = schema.getType(i);
                            break;
                        }
                    }

                    if (lhsIndex == -1) throw new InvalidQueryException("Unknown attribute in WHERE: " + lhs);

                    Object leftVal = tuple.getValue(lhsIndex);

                    switch (lhsType) {
                        case "Integer":
                            int lInt = (Integer) leftVal;
                            int rInt = Integer.parseInt(rhs);
                            match = switch (operator) {
                                case "=" -> lInt == rInt;
                                case "!=" -> lInt != rInt;
                                case ">" -> lInt > rInt;
                                case "<" -> lInt < rInt;
                                case ">=" -> lInt >= rInt;
                                case "<=" -> lInt <= rInt;
                                default -> throw new InvalidQueryException("Invalid operator for Integer");
                            };
                            break;

                        case "String":
                            String lStr = (String) leftVal;
                            match = switch (operator) {
                                case "=" -> lStr.equals(rhs);
                                case "!=" -> !lStr.equals(rhs);
                                default -> throw new InvalidQueryException("Only = and != supported for Strings");
                            };
                            break;

                        default:
                            throw new InvalidQueryException("Unsupported type: " + lhsType);
                    }
                } else {
                    // No WHERE clause: delete everything (i.e., keep nothing)
                    match = true;
                }

                // If it doesn't match the condition, keep it
                if (!match) {
                    toKeep.add(tuple);
                }
            }

            // Replace table's tuples with filtered list
            targetTable.getTuples().clear();
            targetTable.getTuples().addAll(toKeep);

            // Write back updated data to CSV
            IO.writeTable(targetTable, folderName);

        } catch (Exception e) {
            throw new InvalidQueryException("DELETE failed: " + e.getMessage());
        }
    }

}
