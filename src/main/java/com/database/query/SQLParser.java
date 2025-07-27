package com.database.query;

import com.database.query.ast.*;
import java.util.*;

/**
 * Parses SQL statements into Abstract Syntax Trees (AST).
 */
public class SQLParser {
    private List<Token> tokens;
    private int position;
    
    public SQLParser() {
        this.tokens = new ArrayList<>();
        this.position = 0;
    }
    
    /**
     * Parses a SQL string into an AST.
     */
    public SQLStatement parse(String sql) {
        SQLLexer lexer = new SQLLexer(sql);
        this.tokens = lexer.tokenize();
        this.position = 0;
        
        return parseStatement();
    }
    
    private SQLStatement parseStatement() {
        Token currentToken = getCurrentToken();
        
        switch (currentToken.getType()) {
            case SELECT:
                return parseSelectStatement();
            case INSERT:
                return parseInsertStatement();
            case UPDATE:
                return parseUpdateStatement();
            case DELETE:
                return parseDeleteStatement();
            default:
                throw new RuntimeException("Unexpected token: " + currentToken);
        }
    }
    
    private SelectStatement parseSelectStatement() {
        consume(TokenType.SELECT);
        
        // Check for DISTINCT
        boolean distinct = false;
        if (getCurrentToken().getType() == TokenType.DISTINCT) {
            distinct = true;
            consume(TokenType.DISTINCT);
        }
        
        // Parse column list
        List<String> columns = parseColumnList();
        
        // FROM clause
        consume(TokenType.FROM);
        String tableName = consume(TokenType.IDENTIFIER).getValue();
        
        // Optional WHERE clause
        WhereClause whereClause = null;
        if (getCurrentToken().getType() == TokenType.WHERE) {
            consume(TokenType.WHERE);
            Expression condition = parseExpression();
            whereClause = new WhereClause(condition);
        }
        
        // Optional ORDER BY clause
        OrderByClause orderByClause = null;
        if (getCurrentToken().getType() == TokenType.ORDER) {
            consume(TokenType.ORDER);
            consume(TokenType.BY);
            List<String> orderColumns = parseColumnList();
            boolean ascending = true;
            // Note: DESC parsing would be added here
            orderByClause = new OrderByClause(orderColumns, ascending);
        }
        
        return new SelectStatement(columns, tableName, whereClause, orderByClause, distinct);
    }
    
    private SQLStatement parseInsertStatement() {
        consume(TokenType.INSERT);
        consume(TokenType.INTO);
        String tableName = consume(TokenType.IDENTIFIER).getValue();
        
        // For now, return a simple placeholder
        // Full implementation would parse column list and values
        return new SelectStatement(Arrays.asList("*"), tableName, null, null, false) {
            @Override
            public String toSQL() {
                return "INSERT INTO " + tableName + " (placeholder)";
            }
        };
    }
    
    private SQLStatement parseUpdateStatement() {
        consume(TokenType.UPDATE);
        String tableName = consume(TokenType.IDENTIFIER).getValue();
        consume(TokenType.SET);
        
        // For now, return a simple placeholder
        return new SelectStatement(Arrays.asList("*"), tableName, null, null, false) {
            @Override
            public String toSQL() {
                return "UPDATE " + tableName + " SET (placeholder)";
            }
        };
    }
    
    private SQLStatement parseDeleteStatement() {
        consume(TokenType.DELETE);
        consume(TokenType.FROM);
        String tableName = consume(TokenType.IDENTIFIER).getValue();
        
        // Optional WHERE clause
        WhereClause whereClause = null;
        if (getCurrentToken().getType() == TokenType.WHERE) {
            consume(TokenType.WHERE);
            Expression condition = parseExpression();
            whereClause = new WhereClause(condition);
        }
        
        // Return as a specialized SelectStatement for now
        final WhereClause finalWhereClause = whereClause;
        return new SelectStatement(Arrays.asList("*"), tableName, whereClause, null, false) {
            @Override
            public String toSQL() {
                String sql = "DELETE FROM " + tableName;
                if (finalWhereClause != null) {
                    sql += " WHERE " + finalWhereClause.toSQL();
                }
                return sql;
            }
        };
    }
    
    private List<String> parseColumnList() {
        List<String> columns = new ArrayList<>();
        
        if (getCurrentToken().getType() == TokenType.MULTIPLY) {
            consume(TokenType.MULTIPLY);
            columns.add("*");
            return columns;
        }
        
        columns.add(consume(TokenType.IDENTIFIER).getValue());
        
        while (getCurrentToken().getType() == TokenType.COMMA) {
            consume(TokenType.COMMA);
            columns.add(consume(TokenType.IDENTIFIER).getValue());
        }
        
        return columns;
    }
    
    private Expression parseExpression() {
        return parseOrExpression();
    }
    
    private Expression parseOrExpression() {
        Expression left = parseAndExpression();
        
        while (getCurrentToken().getType() == TokenType.OR) {
            String operator = consume(TokenType.OR).getValue();
            Expression right = parseAndExpression();
            left = new BinaryExpression(left, operator, right);
        }
        
        return left;
    }
    
    private Expression parseAndExpression() {
        Expression left = parseComparisonExpression();
        
        while (getCurrentToken().getType() == TokenType.AND) {
            String operator = consume(TokenType.AND).getValue();
            Expression right = parseComparisonExpression();
            left = new BinaryExpression(left, operator, right);
        }
        
        return left;
    }
    
    private Expression parseComparisonExpression() {
        Expression left = parsePrimaryExpression();
        
        TokenType type = getCurrentToken().getType();
        if (type == TokenType.EQUALS || type == TokenType.NOT_EQUALS ||
            type == TokenType.LESS_THAN || type == TokenType.LESS_EQUAL ||
            type == TokenType.GREATER_THAN || type == TokenType.GREATER_EQUAL) {
            
            String operator = consume(type).getValue();
            Expression right = parsePrimaryExpression();
            return new BinaryExpression(left, operator, right);
        }
        
        return left;
    }
    
    private Expression parsePrimaryExpression() {
        Token token = getCurrentToken();
        
        switch (token.getType()) {
            case IDENTIFIER:
                consume(TokenType.IDENTIFIER);
                return new ColumnExpression(token.getValue());
                
            case STRING_LITERAL:
                consume(TokenType.STRING_LITERAL);
                return new LiteralExpression(token.getValue());
                
            case INTEGER_LITERAL:
                consume(TokenType.INTEGER_LITERAL);
                return new LiteralExpression(Integer.parseInt(token.getValue()));
                
            case DOUBLE_LITERAL:
                consume(TokenType.DOUBLE_LITERAL);
                return new LiteralExpression(Double.parseDouble(token.getValue()));
                
            case NULL:
                consume(TokenType.NULL);
                return new LiteralExpression(null);
                
            case TRUE:
                consume(TokenType.TRUE);
                return new LiteralExpression(true);
                
            case FALSE:
                consume(TokenType.FALSE);
                return new LiteralExpression(false);
                
            case LEFT_PAREN:
                consume(TokenType.LEFT_PAREN);
                Expression expr = parseExpression();
                consume(TokenType.RIGHT_PAREN);
                return expr;
                
            default:
                throw new RuntimeException("Unexpected token in expression: " + token);
        }
    }
    
    private Token getCurrentToken() {
        if (position >= tokens.size()) {
            return new Token(TokenType.EOF, "", position);
        }
        return tokens.get(position);
    }
    
    private Token consume(TokenType expectedType) {
        Token token = getCurrentToken();
        if (token.getType() != expectedType) {
            throw new RuntimeException("Expected " + expectedType + " but found " + token.getType());
        }
        position++;
        return token;
    }
}
