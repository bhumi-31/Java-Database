package com.database.query.ast;

/**
 * Represents a binary operation expression (e.g., column = value).
 */
public class BinaryExpression extends Expression {
    private final Expression left;
    private final String operator;
    private final Expression right;
    
    public BinaryExpression(Expression left, String operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
    
    public Expression getLeft() {
        return left;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public Expression getRight() {
        return right;
    }
    
    @Override
    public Object evaluate() {
        Object leftValue = left.evaluate();
        Object rightValue = right.evaluate();
        
        // Basic comparison operations
        switch (operator) {
            case "=":
                return leftValue != null && leftValue.equals(rightValue);
            case "!=":
            case "<>":
                return leftValue == null || !leftValue.equals(rightValue);
            case "<":
                return compareValues(leftValue, rightValue) < 0;
            case "<=":
                return compareValues(leftValue, rightValue) <= 0;
            case ">":
                return compareValues(leftValue, rightValue) > 0;
            case ">=":
                return compareValues(leftValue, rightValue) >= 0;
            case "AND":
                return isTrue(leftValue) && isTrue(rightValue);
            case "OR":
                return isTrue(leftValue) || isTrue(rightValue);
            default:
                throw new UnsupportedOperationException("Operator not supported: " + operator);
        }
    }
    
    @SuppressWarnings("unchecked")
    private int compareValues(Object left, Object right) {
        if (left == null && right == null) return 0;
        if (left == null) return -1;
        if (right == null) return 1;
        
        if (left instanceof Comparable && right instanceof Comparable) {
            return ((Comparable<Object>) left).compareTo(right);
        }
        
        throw new IllegalArgumentException("Cannot compare non-comparable values");
    }
    
    private boolean isTrue(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return value != null;
    }
    
    @Override
    public String toSQL() {
        return left.toSQL() + " " + operator + " " + right.toSQL();
    }
}
