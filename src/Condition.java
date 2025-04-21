/**
 * A condition is of the form operand1 operator operand2, e.g. sid = s1
 */
public class Condition {
    private String operand1;   // Left-hand side of condition (usually an attribute name)
    private String operand2;   // Right-hand side (could be a value or another attribute name)
    private String operator;   // Relational operator (=, !=, <, >, etc.)

    /**
     * constructor
     * @param operand1 the left-hand side of the condition
     * @param operand2 the right-hand side of the condition
     * @param operator the operator (e.g., =, !=, >)
     */
    public Condition(String operand1, String operand2, String operator) {
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operator = operator;
    }

    /**
     * Getters and setters
     */

    public String getOperand1() {
        return this.operand1;
    }

    public void setOperand1(String operand1) {
        this.operand1 = operand1;
    }

    public String getOperand2() {
        return this.operand2;
    }

    public void setOperand2(String operand2) {
        this.operand2 = operand2;
    }

    public String getOperator() {
        return this.operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
