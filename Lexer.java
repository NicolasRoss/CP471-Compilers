import java.io.*;
import java.util.*;

import java.io.*;
import java.util.*;

class Token {
    public Type type;
    public String name;
    // Constructors
    public Token(String n, Type t) { name = n; type = t; }
    public Token(char n, Type t) { name = String.valueOf(n); type = t; }
    // Get Methods
    public String getName() { return name; }
    public Type getTokenType() { return type; }
}

class Type {
    public String type;
    // Constructors
    public Type(String t) { type = t; }
    // Get Method
    public String getType() { return type; }
    // Token Types
    public static final Type RESERVED = new Type("reserved"),
                             ID = new Type("id"),
                             INT = new Type("int"),
                             DOUBLE = new Type("double"),
                             COMPARATOR = new Type("comparator"),
                             TERMINAL = new Type("terminal"),
                             ERROR = new Type("error"),
                             END = new Type("end");
}

public class Lexer {
    private static final List<String> RESERVED = Arrays.asList("int", "while", "do", "od", "print", "double", "def", "fed", "return", "if", "fi", "then", "and", "not", "or");
    private static final List<Character> TERMINALS = Arrays.asList(',', ';', '(', ')', '<', '>', '[', ']', '=', '+', '-', '*', '/', '%', '.');
    private static final List<Character> SPACES = Arrays.asList(' ', '\t', '\r', '\n');
    private static final List<String> symbols = new ArrayList<String>();
    private static char c = ' ';
    
    // Constructor
    public Lexer() {
        for (String str : RESERVED) { symbols.add(str); }
    }

    public static boolean isLetter(int c) {
        return (c >= 97 && c <= 122);
    }

    public static boolean isDigit(int c) {
        return (c >= 48 && c <= 57);
    }

    public static boolean read(char next) throws IOException {
        c = (char) System.in.read();
        if (c != next) { 
            return false;
        
        } else {
            c = (char) System.in.read(); 
            return true;
        }
    }

    public static String getNextToken() throws IOException {
        // Skip whitespace

        while (SPACES.contains(c)) {
            c = (char) System.in.read();
        }
        
        // Check for Comparators
        switch(c) {
            case '=':
                if (read('=')) { 
                    return "<" + Type.COMPARATOR.getType() + ", '=='>";
        
                } else {
                    return "<" + Type.TERMINAL.getType() + ", '='>";
                }
            
            case '<':
                if (read('=')) {
                    return "<" + Type.COMPARATOR.getType() + ", '<='>";

                } else if (c == '>') {
                    c = (char) System.in.read();
                    return "<" + Type.COMPARATOR.getType() + ", '<>'>";

                } else {
                    return "<" + Type.COMPARATOR.getType() + ", '<'>";

                }

            case '>':
                if (read('=')) { 
                    return "<" + Type.COMPARATOR.getType() + ", '>='>";

                } else {
                    return "<" + Type.COMPARATOR.getType() + ", '>'>";
                }

        }

        // Check if the token is a valid string
        if (isLetter(c)) {
            String id = "";
            while (isLetter(c) || isDigit(c)) {
                id += (char) c;
                c = (char) System.in.read();
            }
            
            if (symbols.contains(id)) {
                int index = symbols.indexOf(id);

                // Check if the token is reserved or not
                if (index < RESERVED.size())  {
                    return "<" + id + ">";
                
                } else {
                    return "<" + Type.ID.getType() + ", " + index + ">";

                }

            // Add token to known symbols if not already included
            } else {
                symbols.add(id);
                int index = symbols.size();
                return "<" + Type.ID.getType() + ", " + index + ">";
            }

        // Check if the token is a valid number
        } else if (isDigit(c)) {
            String num = "";

            while (isDigit(c)) {
                num += (char) c;
                c = (char) System.in.read();
            }

            // Checks if its a valid integer or not
            if (isLetter(c) && c != 'e' && c != 'E') {
                while (isLetter(c) || isDigit(c)) {
                    num += (char) c;
                    c = (char) System.in.read();
                }
                
                return "<" + Type.ERROR.getType() + ">";
            
            } else if (c != '.') {
                return "<" + Type.INT.getType() + ", " + num + ">";

            // Checks if its a double
            } else {
                num += (char) c;
                c = (char) System.in.read();

                while (isDigit(c)) {
                    num += (char) c;
                    c = (char) System.in.read();
                }

                // Check for scientific notation
                if (c == 'e' || c == 'E') {
                    num += (char) c;
                    c = (char) System.in.read();
                    
                    if (c == '-' || c == '+') {
                        num += (char) c;
                        c = (char) System.in.read();

                    }

                    if (isDigit(c)) {
                        while (isDigit(c)) {
                            num += (char) c;
                            c = (char) System.in.read();
                        }
                    // Is invalid double

                    } else {
                        System.out.println((char) c);
                        return "<" + Type.ERROR.getType() + ">";
                    }
                // Is invalid double
                } else if (isLetter(c)) {
                    return "<" + Type.ERROR.getType() + ">";
                } 
            // Is valid double
            return "<" + Type.DOUBLE.getType() + ", " + num + ">";
            }
    
        } else if (TERMINALS.contains(c)) {
            if (c == '.') {
                return "<" + Type.END.getType() + ">";

            } else {
                String token =  "<" + Type.TERMINAL.getType() + ", '" + (char) c + "'>";
                c = (char) System.in.read();
                return token;
            }

        } else {
            String token = "<" + Type.ERROR.getType() + ">";
            c = (char) System.in.read();
            return token;
        }
    }

    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer();
        int i = 0;
        while (c != '.') {
            System.out.print(getNextToken());
            System.out.print("");
        }
    }
}
