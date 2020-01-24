import java.io.*;
import java.util.*;

// Add token class and complete html encoding

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

    public static String getNextToken() throws IOException {
        // Skip whitespace
        while (SPACES.contains(c)) {
            c = (char) System.in.read();
        }
        
        // Check for Comparators
        switch(c) {
            case '=':
                c = (char) System.in.read();
                if (c == '=') { 
                    return "<COMPARATOR, '=='>";
        
                } else {
                    return "<TERMINAL, '='>";
                }
            
            case '<':
                c = (char) System.in.read();
                if (c == '=') {
                    return "<COMPARATOR, '<='>";

                } else if (c == '>') {
                    c = (char) System.in.read();
                    return "<COMPARATOR, '<>'>";

                } else {
                    return "<COMPARATOR, '<'>";

                }

            case '>':
                c = (char) System.in.read();
                if (c == '=') { 
                    return "<COMPARATOR, '>='>";

                } else {
                    return "<COMPARATOR, '>'>";
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
                    return "<" + id.toUpperCase() + ">";
                
                } else {
                    return "<ID, " + index + ">";

                }

            // Add token to known symbols if not already included
            } else {
                symbols.add(id);
                int index = symbols.size();
                return "<ID, " + index + ">";
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
                
                return "<ERROR>";
            
            } else if (c != '.') {
                return "<INT, " + num + ">";

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
                        return "<ERROR>";
                    }

                // Is invalid double
                } else if (isLetter(c)) {
                    while (isLetter(c) || isDigit(c) || TERMINALS.contains(c)) {
                        num += (char) c;
                        c = (char) System.in.read();
                    }

                    return "<ERROR>";
                } 

            // Is valid double
            return "<DOUBLE, " + num + ">";
            }
    
        } else if (TERMINALS.contains(c)) {
            if (c == '.') {
                return "<END>";

            } else {
                String token =  "<TERMINAL, '" + (char) c + "'>";
                c = (char) System.in.read();
                return token;
            }

        } else {
            String token = "<ERROR>";
            c = (char) System.in.read();
            return token;
        }
    }

    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer();
        String token = "";

        while (token.compareTo("<END>") != 0) {
            token = getNextToken();
            System.out.print(token);
            System.out.print(" ");
        }
    }
}
