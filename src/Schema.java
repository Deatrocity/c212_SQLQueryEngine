import java.util.Map;

/**
 * the schema is stored as a map of (index, name:type) pairs
 */
public class Schema implements ISchema {

    private Map<Integer, String> attributes;

    /**
     * constructor
     * @param attributes map of index to "name:type" strings
     */
    public Schema(Map<Integer, String> attributes) {
        this.attributes = attributes;
    }

    /**
     * getter
     * @return the full attribute map.
     */
    @Override
    public Map<Integer, String> getAttributes() {
        return this.attributes;
    }

    /**
     * splits the name:type to return the attribute name
     * @param index position of the attribute
     * @return name of the attribute
     */
    @Override
    public String getName(int index) {
        String pair = attributes.get(index);
        return pair.split(":")[0].trim(); // get name before :
    }

    /**
     * splits the name:type to return the attribute type
     * @param index position of the attribute
     * @return type of the attribute (e.g., "String", "Integer")
     */

    @Override
    public String getType(int index) {
        String pair = this.attributes.get(index);
        return pair.split(":")[1].trim(); // get type after :
    }
}