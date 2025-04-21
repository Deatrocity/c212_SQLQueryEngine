import java.util.ArrayList;
import java.util.List;

/**
 * A table has a name, a schema and a list of tuples
 */
public class Table implements ITable {
    private String name;                // Table name (matches schema name and CSV file)
    private List<ITuple> tuples;        // List of data rows
    private ISchema schema;             // Schema describing the structure of the table

    /**
     * constructor
     * @param name name of the table
     * @param schema schema associated with the table
     */
    public Table(String name, ISchema schema) {
        this.name = name;
        this.schema = schema;
        this.tuples = new ArrayList<>();
    }

    /**
     * Returns the table name
     * @return String representing the name of the table.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * adds a tuple to the table
     * @param tuple the tuple to add
     */
    @Override
    public void addTuple(ITuple tuple) {
        this.tuples.add(tuple);
    }

    /**
     * Returns the list of tuples
     * @return the list of all tuples (rows).
     */
    @Override
    public List<ITuple> getTuples() {
        return this.tuples;
    }

    /**
     * Returns the table schema
     * @return ISchema as the schema of the table.
     */
    @Override
    public ISchema getSchema() {
        return this.schema;
    }

}
