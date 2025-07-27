package com.database.query;

/**
 * Represents different types of SQL tokens.
 */
public enum TokenType {
    // Keywords
    SELECT, FROM, WHERE, INSERT, INTO, VALUES, UPDATE, SET, DELETE,
    CREATE, TABLE, DROP, ORDER, BY, GROUP, HAVING, JOIN, INNER, LEFT, RIGHT, FULL,
    AND, OR, NOT, NULL, TRUE, FALSE, AS, DISTINCT, COUNT, SUM, AVG, MIN, MAX,
    
    // Identifiers and literals
    IDENTIFIER,
    STRING_LITERAL,
    INTEGER_LITERAL,
    DOUBLE_LITERAL,
    
    // Operators
    EQUALS,          // =
    NOT_EQUALS,      // !=, <>
    LESS_THAN,       // <
    LESS_EQUAL,      // <=
    GREATER_THAN,    // >
    GREATER_EQUAL,   // >=
    PLUS,            // +
    MINUS,           // -
    MULTIPLY,        // *
    DIVIDE,          // /
    
    // Punctuation
    SEMICOLON,       // ;
    COMMA,           // ,
    LEFT_PAREN,      // (
    RIGHT_PAREN,     // )
    
    // Special
    EOF,             // End of file
    UNKNOWN          // Unknown token
}
