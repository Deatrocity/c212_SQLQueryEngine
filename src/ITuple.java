public interface ITuple {
    void setValue(int index, Object value);       // Set value at index
    <T> T getValue(int index);                    // Get value at index (with type casting)
    Object[] getValues();                         // Get all values as array
    void setValues(Object[] values);              // Set all values from array
}