import java.io.*;
import java.util.*;

class Token {
    public String type;
    public String val;

    public Token(String t, String v) {
        type = t;
        val = v;
    }

    public String getType() { return type; }
    public String getValue() { return val; }
    public String getTokenString() {
        if (this.getType().equals("RESERVED")) return "<" + this.getValue() + ">";
        else if (this.getType().equals("END")) return "<END>";
        else if (this.getType().equals("SPACE")) return "<SPACE>";
        else if (this.getType().equals("NEWLINE")) return "<NEWLINE>";
        else if (this.getType().equals("TAB")) return "<TAB>";
        
        return "<" + this.getType() + ", '" + this.getValue() +"'>";
    }
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
    
    // Creates an html doc of the input code.
    public static void encode(ArrayList<Token> tokens) {
        String header = "<!DOCTYPE html>\n<html>\n\t<style>body { background-color: black; } </style>\n";
        String comment = "\t<!--\n\t";
        String body = "\n\t<body>\n\t\t<p>\n"; 
        String type = "";
        int count = 1;

        for (Token t : tokens) {
            type = t.getType();

            if (!(type.equals("NEWLINE") || type.equals("TAB") || type.equals("SPACE"))) {
                comment = comment + t.getTokenString() + " ";

                if (count % 8 == 0) {
                    comment += "\n\t";
                }

                count++;
            }
        }

        comment += "\n\t-->";

        for (Token t : tokens) {
            body = body + setColour(t);
        }
        
        body = body + "\t\t</p>\n\t</body>\n</html>";
        System.out.print(header + comment + body);
    }
    
    // Sets the colour of each token based on the type
    public static String setColour(Token token) {
        switch(token.getType()) {
            case "RESERVED":
                return "\t\t\t<font color=\"#FF00E8\" size=\"14\">" + token.getValue().toLowerCase() + "</font>\n";
            
            case "ID":
                int index = Integer.parseInt(token.getValue()) - 1;
                return "\t\t\t<font color=\"#FFF300\" size=\"14\">" + symbols.get(index) + "</font>\n";

            case "TERMINAL":
                return "\t\t\t<font color=\"#0FFF00\" size=\"14\">" + token.getValue() + "</font>\n";

            case "INTEGER":
                return "\t\t\t<font color=\"#005DFF\" size=\"14\">" + token.getValue() + "</font>\n";

            case "DOUBLE":
                return "\t\t\t<font color=\"#00FFC1\" size=\"14\">" + token.getValue() + "</font>\n";
            
            case "COMPARATOR":
                return "\t\t\t<font color=\"#FF9300\" size=\"14\">" + token.getValue() + "</font>\n";
            
            case "ERROR":
                return "\t\t\t<font color=\"#FF0000\" size=\"14\">" + token.getValue() + "</font>\n";

            case "SPACE":
                return "\t\t\t<font size=\"14\">&nbsp</font>\n";

            case "TAB":
                return "\t\t\t<font size=\"14\">&nbsp&nbsp&nbsp&nbsp</font>\n";
            
            case "NEWLINE":
                return "\t\t</p>\n\t\t<p>\n";

            case "END":
                return "\t\t\t<font color=\"#FFFFFF\" size=\"14\">" + token.getValue() + "</font>\n";

        }

        return "";
    }

    public static boolean isLetter(int c) {
        return (c >= 97 && c <= 122);
    }

    public static boolean isDigit(int c) {
        return (c >= 48 && c <= 57);
    }

    // Creates and returns the next token from the input code
    public static Token getNextToken() throws IOException {
        // Skip whitespace
        while (SPACES.contains(c)) {
            if (c == '\n') {
                c = (char) System.in.read();
                // return new Token("NEWLINE", "\n");
            
            } else if (c == '\r') {
                c = (char) System.in.read();
                return new Token("NEWLINE", "\r");

            } else if (c == '\t') {
                c = (char) System.in.read();
                return new Token("TAB", "\t");

            } else {
                c = (char) System.in.read();
                return new Token("SPACE", " ");
            } 
        }
        
        // Check for Comparators
        switch(c) {
            case '=':
                c = (char) System.in.read();
                if (c == '=') { 
                    return new Token("COMPARATOR", "==");
        
                } else {
                    return new Token("TERMINAL", "=");
                }
            
            case '<':
                c = (char) System.in.read();
                if (c == '=') {
                    return new Token("COMPARATOR", "<=");

                } else if (c == '>') {
                    c = (char) System.in.read();
                    return new Token("COMPARATOR", "<>");

                } else {
                    return new Token("COMPARATOR", "<");

                }

            case '>':
                c = (char) System.in.read();
                if (c == '=') { 
                    return new Token("COMPARATOR", ">=");

                } else {
                    return new Token("COMPARATOR", ">");

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
                int index = symbols.indexOf(id) + 1;

                // Check if the token is reserved or not
                if (index < RESERVED.size())  {
                    return new Token("RESERVED", id.toUpperCase());

                } else {
                    return new Token("ID", Integer.toString(index));

                }

            // Add token to known symbols if not already included
            } else {
                symbols.add(id);
                int index = symbols.size();
                return new Token("ID", Integer.toString(index));

            }

        // Check if the token is a valid number
        } else if (isDigit(c)) {
            String num = "";

            while (isDigit(c)) {
                num += (char) c;
                c = (char) System.in.read();
            }

            // Checks if its a valid integer or not
            if (isLetter(c) && c != 'e') {
                while (isLetter(c) || isDigit(c)) {
                    num += (char) c;
                    c = (char) System.in.read();
                }
                
                return new Token("ERROR", num);
            
            } else if (c != '.') {
                return new Token("INTEGER", num);

            // Checks if its a double
            } else {
                num += (char) c;
                c = (char) System.in.read();

                if (!isDigit(c)) {
                    while (isLetter(c) || isDigit(c) || TERMINALS.contains(c)) {
                        num += (char) c;
                        c = (char) System.in.read();
                    }
                    
                    return new Token("ERROR", num);
                }

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
                        return new Token("ERROR", num);
                    }

                // Is invalid double
                } else if (isLetter(c)) {
                    while (isLetter(c) || isDigit(c) || TERMINALS.contains(c)) {
                        num += (char) c;
                        c = (char) System.in.read();
                    }

                    return new Token("ERROR", num);
                } 

            // Is valid double
            return new Token("DOUBLE", num);
            }
    
        } else if (TERMINALS.contains(c)) {
            if (c == '.') {
                return new Token("END", ".");

            } else {
                Token token =  new Token("TERMINAL", String.valueOf(c));
                c = (char) System.in.read();
                return token;
            }

        } else {
            Token token = new Token("ERROR", String.valueOf(c));
            c = (char) System.in.read();
            return token;
        }
    }

    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer();
        ArrayList<Token> tokens = new ArrayList<Token>();
        Token t = new Token("START", "START");
        c = (char) System.in.read();

        while (t.getTokenString().compareTo("<END>") != 0) {
            t = getNextToken();
            tokens.add(t);
        }

        encode(tokens);
    }
}
