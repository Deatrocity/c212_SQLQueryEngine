/**
 * Custom exception
 */
public class InvalidQueryException extends Exception {
    /**
     * Constructor with message.
     * @param message error description
     */
    public InvalidQueryException(String message) {
        super(message);
    }
}
