package com.database.query;

import java.util.*;

/**
 * Lexical analyzer for SQL statements.
 * Converts SQL text into a stream of tokens.
 */
public class SQLLexer {
    private final String sql;
    private int position;
    private final List<Token> tokens;
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();
    
    static {
        KEYWORDS.put("SELECT", TokenType.SELECT);
        KEYWORDS.put("FROM", TokenType.FROM);
        KEYWORDS.put("WHERE", TokenType.WHERE);
        KEYWORDS.put("INSERT", TokenType.INSERT);
        KEYWORDS.put("INTO", TokenType.INTO);
        KEYWORDS.put("VALUES", TokenType.VALUES);
        KEYWORDS.put("UPDATE", TokenType.UPDATE);
        KEYWORDS.put("SET", TokenType.SET);
        KEYWORDS.put("DELETE", TokenType.DELETE);
        KEYWORDS.put("CREATE", TokenType.CREATE);
        KEYWORDS.put("TABLE", TokenType.TABLE);
        KEYWORDS.put("DROP", TokenType.DROP);
        KEYWORDS.put("ORDER", TokenType.ORDER);
        KEYWORDS.put("BY", TokenType.BY);
        KEYWORDS.put("GROUP", TokenType.GROUP);
        KEYWORDS.put("HAVING", TokenType.HAVING);
        KEYWORDS.put("JOIN", TokenType.JOIN);
        KEYWORDS.put("INNER", TokenType.INNER);
        KEYWORDS.put("LEFT", TokenType.LEFT);
        KEYWORDS.put("RIGHT", TokenType.RIGHT);
        KEYWORDS.put("FULL", TokenType.FULL);
        KEYWORDS.put("AND", TokenType.AND);
        KEYWORDS.put("OR", TokenType.OR);
        KEYWORDS.put("NOT", TokenType.NOT);
        KEYWORDS.put("NULL", TokenType.NULL);
        KEYWORDS.put("TRUE", TokenType.TRUE);
        KEYWORDS.put("FALSE", TokenType.FALSE);
        KEYWORDS.put("AS", TokenType.AS);
        KEYWORDS.put("DISTINCT", TokenType.DISTINCT);
        KEYWORDS.put("COUNT", TokenType.COUNT);
        KEYWORDS.put("SUM", TokenType.SUM);
        KEYWORDS.put("AVG", TokenType.AVG);
        KEYWORDS.put("MIN", TokenType.MIN);
        KEYWORDS.put("MAX", TokenType.MAX);
    }
    
    public SQLLexer(String sql) {
        this.sql = sql != null ? sql.trim() : "";
        this.position = 0;
        this.tokens = new ArrayList<>();
    }
    
    /**
     * Tokenizes the SQL string and returns a list of tokens.
     */
    public List<Token> tokenize() {
        tokens.clear();
        position = 0;
        
        while (position < sql.length()) {
            skipWhitespace();
            
            if (position >= sql.length()) {
                break;
            }
            
            Token token = nextToken();
            if (token != null) {
                tokens.add(token);
            }
        }
        
        tokens.add(new Token(TokenType.EOF, "", position));
        return new ArrayList<>(tokens);
    }
    
    private Token nextToken() {
        char ch = sql.charAt(position);
        
        // String literals
        if (ch == '\'' || ch == '"') {
            return readStringLiteral();
        }
        
        // Numbers
        if (Character.isDigit(ch)) {
            return readNumber();
        }
        
        // Identifiers and keywords
        if (Character.isLetter(ch) || ch == '_') {
            return readIdentifier();
        }
        
        // Operators and punctuation
        return readOperatorOrPunctuation();
    }
    
    private Token readStringLiteral() {
        int start = position;
        char quote = sql.charAt(position);
        position++; // Skip opening quote
        
        StringBuilder value = new StringBuilder();
        
        while (position < sql.length() && sql.charAt(position) != quote) {
            char ch = sql.charAt(position);
            if (ch == '\\' && position + 1 < sql.length()) {
                // Handle escape sequences
                position++;
                char escaped = sql.charAt(position);
                switch (escaped) {
                    case 'n': value.append('\n'); break;
                    case 't': value.append('\t'); break;
                    case 'r': value.append('\r'); break;
                    case '\\': value.append('\\'); break;
                    case '\'': value.append('\''); break;
                    case '"': value.append('"'); break;
                    default: value.append(escaped); break;
                }
            } else {
                value.append(ch);
            }
            position++;
        }
        
        if (position < sql.length()) {
            position++; // Skip closing quote
        }
        
        return new Token(TokenType.STRING_LITERAL, value.toString(), start);
    }
    
    private Token readNumber() {
        int start = position;
        StringBuilder value = new StringBuilder();
        boolean hasDecimal = false;
        
        while (position < sql.length()) {
            char ch = sql.charAt(position);
            if (Character.isDigit(ch)) {
                value.append(ch);
                position++;
            } else if (ch == '.' && !hasDecimal) {
                hasDecimal = true;
                value.append(ch);
                position++;
            } else {
                break;
            }
        }
        
        TokenType type = hasDecimal ? TokenType.DOUBLE_LITERAL : TokenType.INTEGER_LITERAL;
        return new Token(type, value.toString(), start);
    }
    
    private Token readIdentifier() {
        int start = position;
        StringBuilder value = new StringBuilder();
        
        while (position < sql.length()) {
            char ch = sql.charAt(position);
            if (Character.isLetterOrDigit(ch) || ch == '_') {
                value.append(ch);
                position++;
            } else {
                break;
            }
        }
        
        String identifier = value.toString();
        TokenType type = KEYWORDS.getOrDefault(identifier.toUpperCase(), TokenType.IDENTIFIER);
        
        return new Token(type, identifier, start);
    }
    
    private Token readOperatorOrPunctuation() {
        int start = position;
        char ch = sql.charAt(position);
        
        // Two-character operators
        if (position + 1 < sql.length()) {
            String twoChar = sql.substring(position, position + 2);
            switch (twoChar) {
                case "!=":
                case "<>":
                    position += 2;
                    return new Token(TokenType.NOT_EQUALS, twoChar, start);
                case "<=":
                    position += 2;
                    return new Token(TokenType.LESS_EQUAL, twoChar, start);
                case ">=":
                    position += 2;
                    return new Token(TokenType.GREATER_EQUAL, twoChar, start);
            }
        }
        
        // Single-character operators
        position++;
        switch (ch) {
            case '=': return new Token(TokenType.EQUALS, "=", start);
            case '<': return new Token(TokenType.LESS_THAN, "<", start);
            case '>': return new Token(TokenType.GREATER_THAN, ">", start);
            case '+': return new Token(TokenType.PLUS, "+", start);
            case '-': return new Token(TokenType.MINUS, "-", start);
            case '*': return new Token(TokenType.MULTIPLY, "*", start);
            case '/': return new Token(TokenType.DIVIDE, "/", start);
            case ';': return new Token(TokenType.SEMICOLON, ";", start);
            case ',': return new Token(TokenType.COMMA, ",", start);
            case '(': return new Token(TokenType.LEFT_PAREN, "(", start);
            case ')': return new Token(TokenType.RIGHT_PAREN, ")", start);
            default: return new Token(TokenType.UNKNOWN, String.valueOf(ch), start);
        }
    }
    
    private void skipWhitespace() {
        while (position < sql.length() && Character.isWhitespace(sql.charAt(position))) {
            position++;
        }
    }
    
    public String getSQL() {
        return sql;
    }
}
