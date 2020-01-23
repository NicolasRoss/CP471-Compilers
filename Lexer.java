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
    private static final List<Character> TERMINALS = Arrays.asList(',', ':', '(', ')', '<', '>', '[', ']', '=', '+', '-', '*', '/', '%', '.');
    private static final List<Character> SPACES = Arrays.asList(' ', '\t', '\n');
    private static final List<String> symbols = new ArrayList<String>();

    public Lexer() {
        for (String str : RESERVED) { symbols.add(str); }
    }

    public static String getNextToken(int c) throws IOException {
        // Skip whitespace
        while (SPACES.contains(c)) {
            c = System.in.read();
        }

        

        // Check if the token is a valid string
        if (Character.isLetter(c)) {
            String id = "";
            
            while (Character.isLetter(c) || Character.isDigit(c)) {
                id += (char) c;
                c = System.in.read();
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
        } else if (Character.isDigit(c)) {
            String num = "";

            while (Character.isLetter(c) || Character.isDigit(c)) {
                num += (char) c;
                c = System.in.read();
            }
            // Checks if its a valid integer or not
            if (c != '.') {
                return "<" + Type.INT.getType() + ", " + num + ">";
            
            } else if (Character.isLetter(c) && c != 'e' && c != 'E') {
                return "<" + Type.ERROR.getType() + ">";
            // Checks if its a double
            } else {
                num += (char) c;
                c = System.in.read();

                while (Character.isDigit(c)) {
                    num += (char) c;
                    c = System.in.read();
                }
                // Check for scientific notation
                if (c == 'e' || c == 'E') {
                    num += (char) c;
                    c = System.in.read();
                    
                    if (c == '-' || c == '+') {
                        num += (char) c;
                        c = System.in.read();

                    }

                    if (Character.isDigit(c)) {
                        while (Character.isDigit(c)) {
                            num += (char) c;
                            c = System.in.read();
                        }
                    // Is invalid double
                    } else {
                        return "<" + Type.ERROR.getType() + ">";
                    }
                // Is invalid double
                } else if (Character.isLetter(c)) {
                    return "<" + Type.ERROR.getType() + ">";
                } 
            // Is valid double
            return "<" + Type.DOUBLE.getType() + ", " + num + ">";
            }
        }

        return "fails";
    }

    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer();
        int c = System.in.read();
        int i = 0;
        while (c != -1) {
            // System.out.println((char) c);
            System.out.print(getNextToken(c));
            System.out.print("");
            c = System.in.read();
        }
    }
}