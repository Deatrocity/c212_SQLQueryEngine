import java.util.HashMap;
import java.util.Map;

/**
 * A tuple is an ordered collection of Objects and their associated types (Integer, Double or String)
 * Objects are stored in an array while types are stored in a map of (index, type)
 *
 */
public class Tuple implements ITuple {
    private Object[] values;                   // Stores actual values (of any type)
    private Map<Integer, Class<?>> typeMap;    // Maps attribute index to expected Java class type (e.g., Integer, String)

    /**
     * The constructor receives a schema and creates the object array and typemap (representing the tuple)
     * The schema has the types of attributes stored as strings ("Integer", "Double", "String")
     * Based upon these types the constructor stores the actual class (Integer.class, Double.class, String.class) to the typemap
     * @param schema the schema used to determine types
     */
    public Tuple(ISchema schema) {
        int size = schema.getAttributes().size();   // number of attributes
        this.values = new Object[size];                  // create an empty array of that size
        this.typeMap = new HashMap<>();

        // Fill typeMap with Java class types based on schema type strings
        for (int i : schema.getAttributes().keySet()) {
            String type = schema.getType(i);

            switch (type) {
                case "Integer" -> typeMap.put(i, Integer.class);
                case "Double"  -> typeMap.put(i, Double.class);
                default        -> typeMap.put(i, String.class);  // default to String
            }
        }
    }

    /**
     * Stores the value at the given index in the (tuple) object
     * The value is converted from the object to its actual class from the type map
     * @param index the attribute index
     * @param value the raw value (as Object or String)
     */
    @Override

    public void setValue(int index, Object value) {
        Class<?> type = typeMap.get(index);

        if (type == Integer.class) {
            values[index] = Integer.parseInt(value.toString());   // convert to Integer
        } else if (type == Double.class) {
            values[index] = Double.parseDouble(value.toString()); // convert to Double
        } else {
            values[index] = value.toString();                     // convert to String
        }
    }

    /**
     * Returns the value at a given index from the tuple object
     * @param index index of the attribute
     * @param <T> expected return type
     * @return the stored value, cast to type T
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(int index) {
        return (T) values[index];
    }

    /**
     * Returns the tuple as an array of Objects
     * @return the full array of stored values
     */
    @Override
    public Object[] getValues() {
        return this.values;
    }

    /**
     * Sets the tuple values to the provided ones
     * The values are converted from objects to their actual classes from the typemap
     * @param values array of new values (must match schema length)
     */
    @Override
    public void setValues(Object[] values) {
        for (int i = 0; i < values.length; i++) {
            setValue(i, values[i]);
        }
    }
}