package com.database.query;

/**
 * Represents a single token in SQL parsing.
 */
public class Token {
    private final TokenType type;
    private final String value;
    private final int position;
    
    public Token(TokenType type, String value, int position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }
    
    public TokenType getType() {
        return type;
    }
    
    public String getValue() {
        return value;
    }
    
    public int getPosition() {
        return position;
    }
    
    @Override
    public String toString() {
        return String.format("Token{type=%s, value='%s', position=%d}", type, value, position);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Token token = (Token) obj;
        return position == token.position &&
               type == token.type &&
               (value != null ? value.equals(token.value) : token.value == null);
    }
    
    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + position;
        return result;
    }
}
