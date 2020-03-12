import java.io.*;
import java.util.*;

class Entry {
    private String id, var, type;  
    private Object val;

    public Entry(String i, String v, String t) {
        var = v;
        id = i;
        type = t;
        val = null;
    }

    public Object getValue() { return val; }
    public String getID() { return id; }
    public String getType() { return type; }
    public String getName() { return var; }

    public boolean equals(Object o) {
        if (o != null && o instanceof Entry) {
            Entry rhs = (Entry) o;
            return ((this.var.equals(rhs.var)) && (this.type.equals(rhs.type)));
        }

        return false;
    }

    public void setValue(Object v) { val = v; }
}

class SymbolTable {
    private ArrayList<Entry> symbols;
    private String tableName;
    private int count;

    public SymbolTable(String name) {
        tableName = name;
        symbols = new ArrayList<Entry>();
        count = 16;
    } 

    public String getName() { return tableName; }
    public int getSize() { return count; }

    public Entry get(String name) {
        for (Entry e : symbols) {
            if (e.getName().equals(name)) {
                return e;
            }
        }

        return null;
    }

    public Entry get(Integer i) { return symbols.get(i); }

    public ArrayList<Entry> getSymbols() { return symbols; }

    public void updateVal(Integer index, Object value) { symbols.get(index).setValue(value);}
    public void updateVal(String varName, Object value) { get(varName).setValue(value); }

    public boolean contains(Entry entry) {
        for (int i = 0; i < symbols.size(); i++) {
            if (symbols.get(i).equals(entry)) {
                return true;
            }
        }

        return false;
    }

    public boolean contains(String name) {
        for (int i = 0; i < symbols.size(); i++) {
            if (symbols.get(i).getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public void add(Entry entry) {
        if (!symbols.contains(entry)) {
            symbols.add(entry);
            count++;
        }
    }

    public void printSymbolTable() {
        System.out.println("--------" + this.getName() + "------");
        System.out.println("ID   Var   Val   Type");
        Entry symbolEntry;

        for (int i = 0; i < symbols.size(); i++) {
            symbolEntry = symbols.get(i);
            System.out.println(symbolEntry.getID() + "    " + symbolEntry.getName() + "    " + symbolEntry.getValue() + "    "+ symbolEntry.getType());
        }
    }
}

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

class Node {
    private Node left;
    private Node right;
    private String value = null;
    public SymbolTable symbols;
    private ArrayList<Node> children;

    public Node(String v) {
        value = v;
        left = null;
        right = null;
        children = new ArrayList<Node>();
        symbols = new SymbolTable(v);
    }

    public Node getLeft() { return left; }
    public Node getRight() { return right; }
    public String getValue() { return value; }
    public ArrayList<Node> getChildren() { return children; }

    public void setLeft(Node l) { left = l; }
    public void setRight(Node r) { right = r; }
    public void setValue(String v) { value = v; }
    public void setChildren(ArrayList<Node> c) { children = c; };
    public void addChild(Node n) {
        children.add(n);
    }

    public static void printTree(Node root) {
        if (root != null) {
            if (root.getLeft() != null) { printTree(root.getLeft()); }
            System.out.println(root.getValue()); 
            if (root.getRight() != null) { printTree(root.getRight()); }
            if (root.children.size() > 0) {
                // System.out.println("size:" + root.children.size());
                for (int i = 0; i < root.children.size(); i++) {
                    printTree(root.children.get(i));
                }
            } 
        }
    }
}

class Eval {
    private Node funcNode;
    private Node prevFuncNode;

    public Eval(Node root) {
        if (root != null) {
            funcNode = root;
            prevFuncNode = null;
            funcNode.symbols = Parser.getSymbolTable(root.getValue());
            evaluate(root);
        }
    }

    public Node evaluate(Node root) {
        Node node = null;
        for (Node n : root.getChildren()) { 
            node = statements(n);
            if (node != null) {
                return node;
            }
        }

        return node;
    }

    public Node getStart() { return funcNode; }
    public String getFuncName() { return funcNode.getValue(); }
    public String getPrevFunc() { return prevFuncNode.getValue(); }

    public Node statements(Node n) {
        Node node = null; Object value = null;
       
        if (n.getValue().equals("=")) {
            value = expressions(n.getRight());
            // System.out.println(n.getLeft().getValue());
            // funcNode.symbols.printSymbolTable();
            funcNode.symbols.updateVal(n.getLeft().getValue(), value);

        } else if (n.getValue().equals("if")) {
            if (conditionals(n.getLeft())) {
                for (int i = 0; i < n.getChildren().size(); i++) {
                    node = statements(n.getChildren().get(i));

                    if (node != null) {
                        return node;
                    }
                }

            } else {
                for (int i = 0; i < n.getRight().getChildren().size(); i++) {
                    node = statements(n.getRight().getChildren().get(i));

                    if (node != null) {
                        return node;
                    }
                }
            }

        } else if (n.getValue().equals("while")) {
            while(conditionals(n.getLeft())) {
                for (int i = 0; i < n.getChildren().size(); i++) {
                    node = statements(n.getChildren().get(i));
                    
                    if (node != null) {
                        return node;
                    }
                }
            }

            
        } else if (n.getValue().equals("print")) {
            // System.out.println(n.getValue());
            value = expressions(n.getLeft());
            System.out.println(value);

        } else if (n.getValue().equals("return")) {
            node = new Node(expressions(n.getLeft()).toString());
            funcNode.symbols = Parser.getSymbolTable(prevFuncNode.getValue());
            funcNode = prevFuncNode;
            // prevFuncNode = null;
            

        } else if (Parser.getFuncNode(n.getValue()) != null) {
            SymbolTable table = Parser.getSymbolTable(n.getValue());
            ArrayList<Node> children = n.getChildren();
        
            for (int i = 0; i < children.size(); i++) {
                table.updateVal(i, expressions(children.get(i)));
            }

            node = Parser.getFuncNode(n.getValue());
            node.symbols = table;

            evaluate(node);
        }

        return node;
    }

    public Object expressions(Node n) {
        Object value = null; Object intORdub = resolve(n);

        if (n.getValue().equals("+")) {
            Object r  = expressions(n.getRight());
            Object l  = expressions(n.getLeft());
    
            if (l instanceof String) { l = (Object) Parser.getTable(getFuncName()).get(l.toString()).getValue(); }
            if (r instanceof String) { r = (Object) Parser.getTable(getFuncName()).get(r.toString()).getValue(); }

            // System.out.println(l + " " + r);
            value = numericOperation("+", l, r);

        } else if (n.getValue().equals("-")) {
            Object r  = expressions(n.getRight());
            Object l  = expressions(n.getLeft());

            if (l instanceof String) { l = (Object) Parser.getTable(getFuncName()).get(l.toString()).getValue(); }
            if (r instanceof String) { r = (Object) Parser.getTable(getFuncName()).get(r.toString()).getValue(); }
            
            // System.out.println(l + " " + r);
            value = numericOperation("-", l, r);
        
        } else if (n.getValue().equals("*")) {
            Object r  = expressions(n.getRight());
            Object l  = expressions(n.getLeft());

            if (l instanceof String) { l = (Object) Parser.getTable(getFuncName()).get(l.toString()).getValue(); }
            if (r instanceof String) { r = (Object) Parser.getTable(getFuncName()).get(r.toString()).getValue(); }
            
            // System.out.println(l + " " + r);
            value = numericOperation("*", l, r);
        
        } else if (n.getValue().equals("/")) {
            Object r  = expressions(n.getRight());
            Object l  = expressions(n.getLeft());

            if (l instanceof String) { l = (Object) Parser.getTable(getFuncName()).get(l.toString()).getValue(); }
            if (r instanceof String) { r = (Object) Parser.getTable(getFuncName()).get(r.toString()).getValue(); }
            
            // System.out.println(l + " " + r);
            value = numericOperation("/", l, r);
        
        } else if (n.getValue().equals("%")) {
            Object r  = expressions(n.getRight());
            Object l  = expressions(n.getLeft());

            if (l instanceof String) { l = (Object) Parser.getTable(getFuncName()).get(l.toString()).getValue(); }
            if (r instanceof String) { r = (Object) Parser.getTable(getFuncName()).get(r.toString()).getValue(); }
            
            // System.out.println(l + " " + r);
            value = numericOperation("%", l, r);

        } else if (Parser.getFuncNode(n.getValue()) != null) {
            SymbolTable table = Parser.getSymbolTable(n.getValue());
            ArrayList<Node> children = n.getChildren();

            for (int i = 0; i < children.size(); i++) {
                table.updateVal(i, expressions(children.get(i)));
            }
           
            Node node = Parser.getFuncNode(n.getValue());
            node.symbols = table;
            n.symbols = table;
            prevFuncNode = funcNode;
            funcNode = n;

            return expressions(evaluate(node));

        } else if (funcNode.symbols.contains(n.getValue())) {
            return funcNode.symbols.get(n.getValue()).getValue();

        } else if (intORdub instanceof Integer || intORdub instanceof Double) {
            value = intORdub;
    
        } else {
            // System.out.println(n.getValue());
            Parser.error();
        }

        return value;
    }

    public boolean conditionals(Node n) {
        boolean condition = false;

        if (n.getValue().equals("<")) {
            Object l = expressions(n.getLeft());
            Object r = expressions(n.getRight());
            if (l instanceof String) { l = (Object) Parser.getTable(getFuncName()).get(l.toString()).getValue(); }
            if (r instanceof String) { r = (Object) Parser.getTable(getFuncName()).get(r.toString()).getValue(); }
            if (comparison("<", l, r)) { 
                condition = true;

            } else { 
               condition = false;
            }

        } else if (n.getValue().equals(">")) {
            Object l = expressions(n.getLeft());
            Object r = expressions(n.getRight());
            if (l instanceof String) { l = (Object) Parser.getTable(getFuncName()).get(l.toString()).getValue(); }
            if (r instanceof String) { r = (Object) Parser.getTable(getFuncName()).get(r.toString()).getValue(); }
            if (comparison(">", l, r)) { 
                condition = true;

            } else { 
               condition = false;
            }
        
        } else if (n.getValue().equals("<=")) {
            Object l = expressions(n.getLeft());
            Object r = expressions(n.getRight());
            if (l instanceof String) { l = (Object) Parser.getTable(getFuncName()).get(l.toString()).getValue(); }
            if (r instanceof String) { r = (Object) Parser.getTable(getFuncName()).get(r.toString()).getValue(); }
            if (comparison("<=", l, r)) { 
                condition = true;

            } else { 
               condition = false;
            }
        
        } else if (n.getValue().equals(">=")) {
            Object l = expressions(n.getLeft());
            Object r = expressions(n.getRight());
            if (l instanceof String) { l = (Object) Parser.getTable(getFuncName()).get(l.toString()).getValue(); }
            if (r instanceof String) { r = (Object) Parser.getTable(getFuncName()).get(r.toString()).getValue(); }
            if (comparison(">=", l, r)) { 
                condition = true;

            } else { 
               condition = false;
            }
        
        } else if (n.getValue().equals("==")) {
            Object l = expressions(n.getLeft());
            Object r = expressions(n.getRight());
            
            if (l instanceof String) { l = (Object) Parser.getTable(getFuncName()).get(l.toString()).getValue(); }
            if (r instanceof String) { r = (Object) Parser.getTable(getFuncName()).get(r.toString()).getValue(); }
            if (comparison("==", l, r)) { 
                condition = true;

            } else { 
               condition = false;
            }
        
        } else if (n.getValue().equals("<>")) {
            Object l = expressions(n.getLeft());
            Object r = expressions(n.getRight());
            if (l instanceof String) { l = (Object) Parser.getTable(getFuncName()).get(l.toString()).getValue(); }
            if (r instanceof String) { r = (Object) Parser.getTable(getFuncName()).get(r.toString()).getValue(); }

            // System.out.println(l + " " + r);
            if (comparison("<>", l, r)) { 
                condition = true;

            } else { 
               condition = false;
            }
        
        }
        return condition;
    }

    public Object resolve(Node n) {
        Object val = null;
        boolean d = false;

        try {
            try {
                val = Integer.parseInt(n.getValue());

            } catch (NumberFormatException e) {
                d = true;
            }

            if (d) {
                val = Double.parseDouble(n.getValue());
            }
            
        } catch (NumberFormatException e) {
            val = n.getValue();
        }
        
        return val;
    }

    public Object numericOperation(String op, Object x, Object y) {
        Object result = null;
        if (x instanceof Integer && y instanceof Integer) { result = numericOperation(op, (int) x, (int) y); }
        else if (x instanceof Double && y instanceof Double) { result = numericOperation(op, (double) x, (double) y); }
        else { Parser.error(); }

        return result;
    }

    public int numericOperation(String op, int x, int y) {
        int result = 0;

        switch(op) {
            case "+":
                result = x + y;
                return result;
            
            case "-":
                result = x - y;
                return result;

            case "*":
                result = x * y;
                return result;
            
            case "/":
                result = x / y;
                return result;
            
            case "%":
                result = x % y;
                return result;
        }

        return result;
    }

    public double numericOperation(String op, double x, double y) {
        double result = 0;

        switch(op) {
            case "+":
                result = x + y;
                return result;
            
            case "-":
                result = x - y;
                return result;

            case "*":
                result = x * y;
                return result;
            
            case "/":
                result = x / y;
                return result;
            
            case "%":
                result = x % y;
                return result;
        }

        return result;
    }

    public boolean comparison(String op, Object x, Object y) {
        boolean result = false;
        // System.out.println(y instanceof String);
        if (x instanceof Integer && y instanceof Integer) { result = comparison(op, (int) x, (int) y); }
        else if (x instanceof Double && y instanceof Double) { result = comparison(op, (double) x, (double) y); }
        else { Parser.error(); }

        return result;
    }

    public boolean comparison(String op, int x, int y) {
        boolean result = false;
        switch(op) {
            case "<":
                result = x < y;
                return result;
            
            case ">":
                result = x > y;
                return result;
            
            case "<=":
                result = x <= y;
                return result;
            
            case ">=":
                result = x >= y;
                return result;
            
            case "==":
                result = x == y;
                return result;
            
            case "<>":
                result = x != y;
                return result;
        }

        return result;
    }

    public boolean comparison(String op, double x, double y) {
        boolean result = false;
        switch(op) {
            case "<":
                result = x < y;
                return result;
            
            case ">":
                result = x > y;
                return result;
            
            case "<=":
                result = x <= y;
                return result;
            
            case ">=":
                result = x >= y;
                return result;
            
            case "==":
                result = x == y;
                return result;
            
            case "<>":
                result = x != y;
                return result;
        }

        return result;
    }

}

public class Parser {
    private static Lexer lexer = new Lexer();
    private static ArrayList<SymbolTable> funcTable = new ArrayList<SymbolTable>();
    private static ArrayList<Node> funcNodes = new ArrayList<Node>();
    private Token nextToken = null;
    private Token token = null;
    private static Hashtable<String, List<String>> GRAMMER = new Hashtable<String, List<String>>();
    private String name, function;
    private Node currNode;

    private void createGRAMMER() {
        GRAMMER.put("<program>", Arrays.asList("def", "int", "double", "if", "while", "print", "return", "ID"));
        GRAMMER.put("<fdecls>", Arrays.asList("def"));
        GRAMMER.put("<fdecls_r>", Arrays.asList("def"));
        GRAMMER.put("<fdec>", Arrays.asList("def"));
        GRAMMER.put("<params>", Arrays.asList("int", "double"));
        GRAMMER.put("<params_r>", Arrays.asList(","));
        GRAMMER.put("<fname>", Arrays.asList("ID"));
        GRAMMER.put("<declarations>", Arrays.asList("int", "double"));
        GRAMMER.put("<declarations_r>", Arrays.asList("int", "double"));
        GRAMMER.put("<decl>", Arrays.asList("int", "double"));
        GRAMMER.put("<type>", Arrays.asList("int", "double"));
        GRAMMER.put("<var>", Arrays.asList("ID"));
        GRAMMER.put("<varlist>", Arrays.asList("ID"));
        GRAMMER.put("<varlist_r>", Arrays.asList(","));
        GRAMMER.put("<statement_seq>", Arrays.asList("if", "while", "print", "return", "ID"));
        GRAMMER.put("<statement_seq_r>", Arrays.asList(";"));
        GRAMMER.put("<statement>", Arrays.asList("if", "while", "print", "return", "ID"));
        GRAMMER.put("<else>", Arrays.asList("else"));
        GRAMMER.put("<expr>", Arrays.asList("ID", "NUMBER", "("));
        GRAMMER.put("<expr_r>", Arrays.asList("+", "-"));
        GRAMMER.put("<term>", Arrays.asList("ID", "NUMBER", "("));
        GRAMMER.put("<term_r>", Arrays.asList("*", "/", "%"));
        GRAMMER.put("<factor>", Arrays.asList("ID", "NUMBER", "("));
        GRAMMER.put("<factor_r>", Arrays.asList("("));
        GRAMMER.put("<bexpr>", Arrays.asList("(", "not"));
        GRAMMER.put("<bexpr_r>", Arrays.asList("or", "(", "not"));
        GRAMMER.put("<bterm>", Arrays.asList("(", "not"));
        GRAMMER.put("<bterm_r>", Arrays.asList("and", "(", "not"));
        GRAMMER.put("<bfactor>", Arrays.asList("(", "not"));
        GRAMMER.put("<bfactor_r>", Arrays.asList("(", "ID", "NUMBER"));
        GRAMMER.put("<comp>", Arrays.asList("<", ">", "==", "<=", ">=", "<>"));
        GRAMMER.put("<exprseq>", Arrays.asList(",", "ID", "NUMBER"));
        GRAMMER.put("<exprseq_r>", Arrays.asList(","));
    }

    public Parser() throws IOException {
        createGRAMMER();
        nextToken(); 
        nextToken();
    }

    public static void main(String[] args) throws IOException {
        Parser parser = new Parser();
        Node root = parser.program();
        System.out.println("correct");

        // for (Node n : funcNodes) { Node.printTree(n); System.out.println(); }
        // Node.printTree(root);
        
        new Eval(root);
        // for (Node n : funcNodes) { new Eval(n); }

        for (int i = 0; i < funcTable.size(); i++){
            getTable(funcTable.get(i).getName()).printSymbolTable();
        }
    }

    public static ArrayList<SymbolTable> getFuncTable() { return funcTable; }
    public static Node getFuncNode (String name) { 
        for (Node n: funcNodes) {
            if (n.getValue().equals(name)) {
                return n;
            }
        }
        return null;
    }

    public static SymbolTable getSymbolTable(String funcName) {
        for (SymbolTable s : funcTable) { 
            if (s.getName().equals(funcName)) {
                return s;
            }
        }

        return null;
    }

    public static void updateFuncTable(String funcName, String target, Object val) {
        for (SymbolTable s : funcTable) { 
            if (s.getName().equals(funcName)) {
                s.updateVal(target, val);
            }
        }
    }

    public static SymbolTable getTable(String name) {
        for (int i = 0; i < funcTable.size(); i++) {
            if (funcTable.get(i).getName().equals(name)) {
                return funcTable.get(i);
            }
        }

        return null;
    }

    public Node program() {
        currNode = new Node("global");
        fdecls(); declarations(); statement_seq(); isMatch('.'); 
        return currNode;
    }

    public void fdecls() {
        if (checkGRAMMER("<fdecls>") != null) {
            fdec(); isMatch(';'); fdecls_r();
        }
    }

    public void fdec() {
        if (checkGRAMMER("<fdec>") != null) {
            Node tmp = currNode;
            Node node;
            isMatch("def"); type(); node = fname(); 
            currNode = node; 
            isMatch('('); params(); isMatch(')'); declarations(); statement_seq(); 
            funcNodes.add(currNode);
            isMatch("fed");
            currNode = tmp;
            function = null;
        }
    }

    public void fdecls_r() {
        if (checkGRAMMER("<fdecls_r>") != null) {
            fdec(); isMatch(';'); fdecls_r();
        }
    }

    public void type() {
        switch(checkGRAMMER("<type>")) {
            case "int":
                isMatch("int");
                return;
            
            case "double":
                isMatch("double");
                return;
            
            default:
                error();
        }
    }   

    public Node fname() {
        Node node = null;

        if (checkGRAMMER("<fname>") != null) {
            name = nextToken.getValue();
            function = name;

            if (getTable("global") == null) {
                funcTable.add(new SymbolTable("global"));
                getTable("global").add(new Entry(Integer.toString(getTable("global").getSize()), name, "FUNC"));

            } else {
                getTable("global").add(new Entry(Integer.toString(getTable("global").getSize()), name, "FUNC"));
            }

            funcTable.add(new SymbolTable(function));
            node = new Node(name);
            isMatch("ID");

        } else {
            error();
        }

        return node;
    }

    public void params() {
        if (checkGRAMMER("<params>") != null) {
            type(); var(); params_r();
        }
    }

    public void params_r() {
        if (checkGRAMMER("<params_r>") != null) {
            isMatch(','); params();
        }
    }

    public void declarations() {
        if (checkGRAMMER("<declarations>") != null) {
            decl(); isMatch(';'); declarations_r();
        }
    }

    public void declarations_r() {
        if (checkGRAMMER("<declarations_r>") != null) {
            decl(); isMatch(';'); declarations_r();
        }
    }

    public void decl() {
        if (checkGRAMMER("<decl>") != null) {
            type(); varlist();
        }
    }

    public void varlist() {
        if (checkGRAMMER("<varlist>") != null) {
            var(); varlist_r();

        } else {
            error();
        }
    }

    public void varlist_r() {
        if (checkGRAMMER("<varlist_r>") != null) {
            isMatch(','); varlist();
        }
    }

    public void statement_seq() {
        if (checkGRAMMER("<statement_seq>") != null) {
            statement(); statement_seq_r();
        }
    }

    public void statement_seq_r() {
        if (checkGRAMMER("<statement_seq_r>") != null) {
            isMatch(';'); statement_seq();     
        }
    }

    public Node statement() {
        Node node = null;
        Node l, r, tmp; 
        Node optElse = new Node("else");

        if (checkGRAMMER("<statement>") != null) {
            // System.out.println(nextToken.getValue());
            switch(checkGRAMMER("<statement>")) {
                case "ID":
                    l = var(); node = isMatch('='); r = expr();
                    node.setLeft(l); node.setRight(r);
                    currNode.addChild(node);
                    return node;

                case "if":
                    node = isMatch("if");
                    tmp = currNode;
                    currNode = node;
                    node.setLeft(bexpr()); isMatch("then"); statement_seq(); 
                    currNode = optElse;
                    optional(); isMatch("fi");
                    node.setRight(optElse);
                    // System.out.println(node.getRight().getValue());
                    // for (Node n : node.getRight().getChildren()) {
                    //     System.out.print(n.getValue() + " ");
                    // }
                    // System.out.println();
                    currNode = tmp;
                    currNode.addChild(node);
                    return node;

                case "while":
                    node = isMatch("while");
                    tmp = currNode;
                    currNode = node;
                    node.setLeft(bexpr()); isMatch("do"); statement_seq(); isMatch("od");
                    currNode = tmp;
                    currNode.addChild(node);
                    return node;
                
                case "print":
                    node = isMatch("print"); node.setLeft(expr());
                    currNode.addChild(node);
                    return node;

                case "return":
                    node = isMatch("return"); node.setLeft(expr());
                    currNode.addChild(node);
                    return node;

                default:
                    return node;
            }
        }
        
        return node;
    }

    public void optional() {
        if (checkGRAMMER("<else>") != null) {
            isMatch("else"); statement_seq();
        }
    }

    public Node var() {
        Node node = null;
        if (checkGRAMMER("<var>") != null) {
            name = nextToken.getValue();
        
            if (function != null) {
                getTable(function).add(new Entry(Integer.toString(getTable(function).getSize()), name, "VAR"));

            } else {
                if (getTable("global") == null) {
                    funcTable.add(new SymbolTable("global"));
                    getTable("global").add(new Entry(Integer.toString(getTable("global").getSize()), name, "VAR"));

                } else {
                    getTable("global").add(new Entry(Integer.toString(getTable("global").getSize()), name, "VAR"));
                }


            }

            isMatch("ID"); var_r();
            node = new Node(name);

        } else {
            error();
        }

        return node;
    }

    public void var_r() {
        if (checkGRAMMER("<var_r>") != null) {
            isMatch('['); expr(); isMatch(']');
        }
    }

    public Node expr() {
        Node node = null;
        if (checkGRAMMER("<expr>") != null) {
            Node a = term(); Node b = expr_r();
            
            if (b == null) {
                node = a;

            } else {
                b.setLeft(a);
                node = b;
            }

        } else {
            error();
        }

        return node;
    }

    public Node expr_r() {
        String grammer = checkGRAMMER("<expr_r>");
        Node node = null;

        if (grammer != null) {
            if (grammer.equals("+")) {
                node = isMatch('+'); Node a = term(); Node b = expr_r();

                if (b == null) {
                    node.setRight(a);

                } else {
                    b.setLeft(a);
                    node.setRight(b);
                } 

            } else if (grammer.equals("-")) {
                node = isMatch('-'); Node a = term(); Node b = expr_r();

                if (b == null) {
                    node.setRight(a);

                } else {
                    b.setLeft(a);
                    node.setRight(b);
                } 

            } else {
                error();
            }
        }

        return node;
    }

    public Node bexpr() {
        Node node = null;
        if (checkGRAMMER("<bexpr>") != null) {
            Node a = bterm(); Node b = bexpr_r();

            if (b == null) {
                node = a;

            } else {
                b.setLeft(a);
                node = b;
            }
        
        } else {
            error();
        }

        return node;
    }

    public Node bexpr_r() {
        Node node = null;

        if (checkGRAMMER("<bexpr_r>") != null) {
            node = isMatch("or"); Node a = bterm(); Node b = bexpr_r();

            if (b == null) {
                node.setRight(a);

            } else {
                b.setLeft(a);
                node.setRight(b);
            }
        }

        return node;
    }

    public Node bterm() {
        Node node = null;
        if (checkGRAMMER("<bterm>") != null) {
            Node a = bfactor(); Node b = bterm_r();
            
            if (b == null) {
                node = a;

            } else {
                b.setLeft(a);
                node = b;
            }

        } else {
            error();
        }

        return node;
    }

    public Node bterm_r() { 
        Node node = null;
        if (checkGRAMMER("<bterm_r>") != null) {
            node = isMatch("and"); Node a = bfactor(); Node b = bterm_r();

            if (b == null) {
                node.setRight(a);

            } else {
                b.setLeft(a);
                node.setRight(a);
            }
        }

        return node;
    }

    public Node bfactor() {
        Node node = null;
        switch(checkGRAMMER("<bfactor>")) {
            case "(":
                isMatch('('); node = bfactor_r();  isMatch(')');
                return node;

            case "not":
                isMatch("not"); node = bfactor();
                return node;
            
            default:
                error();
        }

        return node;
    }

    public Node bfactor_r() {
        String grammer = checkGRAMMER("<bfactor_r>");
        Node node = null;
        if (GRAMMER.get("<bfactor_r>").contains(grammer) && token.getType() == "COMPARATOR") {
            Node l = expr(); node = comp(); Node r = expr();
            node.setLeft(l); node.setRight(r);
            
        } else if (GRAMMER.get("<bfactor_r>").contains(grammer)) {
            bexpr();
        
        } else {
            error();
        }

        return node;
    }

    public Node comp() {
        Node node = null;
        if (checkGRAMMER("<comp>") != null) {
            node = isMatch("COMPARATOR");

        } else {
            error();
        }

        return node;
    }

    public Node term() {
        Node node = null;
        if (checkGRAMMER("<term>") != null) {
            Node a = factor(); Node b = term_r();

            if (b == null) {
                node = a;

            } else {
                b.setLeft(a);
                node = b;
            }

        } else {
            error();
        }

        return node;
    }

    public Node term_r() {
        String grammer = checkGRAMMER("<term_r>");
        Node node = null;

        if (grammer != null) {
            if (grammer.equals("*")) {
                node = isMatch('*'); Node a = factor(); Node b = term_r();

                if (b == null) {
                    node.setRight(a);

                } else {
                    b.setLeft(a);
                    node.setRight(b);
                }

            } else if (grammer.equals("/")) {
                node = isMatch('/'); Node a = factor(); Node b = term_r();

                if (b == null) {
                    node.setRight(a);

                } else {
                    b.setLeft(a);
                    node.setRight(b);
                }

            } else if (grammer.equals("%")) {
                node = isMatch('%'); Node a = factor(); Node b = term_r();

                if (b == null) {
                    node.setRight(a);

                } else {
                    b.setLeft(a);
                    node.setRight(b);
                }
            
            } else {
                error();
            }
        }

        return node;
    }

    public Node factor() {
        String grammer = checkGRAMMER("<factor>");
        Node node = null;
        Node tmp;
        
        if (grammer != null) {
            if (grammer.equals("ID")) {
                node = isMatch("ID"); 
                tmp = currNode;
                currNode = node;
                factor_r(); 
                currNode = tmp;
            
            } else if (grammer.equals("NUMBER")) {
                name = nextToken.getValue();

                // if (function != null) {
                //     getTable(function).add(new Entry(name, Integer.toString(getTable(function).getSize()) , "CONST"));
    
                // } else {
                //     if (getTable("global") == null) {
                //         funcTable.add(new SymbolTable("global"));
                //         getTable("global").add(new Entry(name, Integer.toString(getTable("global").getSize()) , "CONST"));
    
                //     } else {
                //         getTable("global").add(new Entry(name, Integer.toString(getTable("global").getSize()) , "CONST"));
                //     }
                // }

                isMatch("DOUBLE");
                node = new Node(name);
            
            } else if (grammer.equals("(")) {
                isMatch('('); node = expr(); isMatch(')');

            } else if (grammer.equals("ID")) {
                var();
            
            } else {
                error();
            }
        }

        return node;
    }

    public Node factor_r() {
        String grammer = checkGRAMMER("<factor_r>");
        Node node = null;

        if (grammer != null) {
            if (grammer.equals("(")) {
                isMatch('('); exprseq(); isMatch(')');
            }
        }

        return node;
    }

    public void exprseq() {
        if (checkGRAMMER("<exprseq>") != null) {
            currNode.addChild(expr()); exprseq_r();
        }
    }

    public void exprseq_r() {
        if (checkGRAMMER("<exprseq_r>") != null) {
            isMatch(','); exprseq();
        }
    }

    public void nextToken() {
        nextToken = token;

        try {
            if (token == null || (token != null && token.getType() != "END")) {
                token = lexer.getNextToken();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Node isMatch(char c) {
        boolean isMatch = nextToken.getValue().equals(String.valueOf(c));
        Node n = null;

        if (isMatch) {
            n = new Node(String.valueOf(c));
            nextToken();

        } else {
            error();
        }

        return n;
    }

    public Node isMatch(String s) {
        boolean isMatch = false;
        Node n = null;

        if (nextToken.getType() == s) {
            isMatch = true;
        
        } else if (s == "INTEGER" || s == "DOUBLE") {
            isMatch = true;

        } else {
            isMatch = nextToken.getValue().toLowerCase().equals(s);
        }
        
        if (isMatch) {
            n = new Node(nextToken.getValue());
            nextToken();

        } else {
            error();
        }

        return n;
    }

    public String checkGRAMMER(String nonterminal) {
        List<String> grammer = GRAMMER.get(nonterminal);

        if (grammer != null) {
            if ((nextToken.getType() == "INTEGER" || nextToken.getType() == "DOUBLE") && grammer.contains("NUMBER")) {
                return "NUMBER";

            } else if (nextToken.getType() == "ID" && grammer.contains("ID")) {
                return "ID";

            } else if (grammer.contains(nextToken.getValue())) {
                return nextToken.getValue();

            } else {
                return null;
            }

        } else {
            return null;
        }
    }

    public static void error() {
        System.out.println("Error.");
        System.exit(0);
    }
}

class Lexer {
    private static final List<String> RESERVED = Arrays.asList("int", "while", "do", "od", "print", "double", "def", "fed", "return", "if", "fi", "then", "else", "and", "not", "or");
    private static final List<Character> TERMINALS = Arrays.asList(',', ';', '(', ')', '<', '>', '[', ']', '=', '+', '-', '*', '/', '%', '.');
    private static final List<Character> SPACES = Arrays.asList(' ', '\t', '\r', '\n');
    static final List<String> symbols = new ArrayList<String>();
    private static char c = ' ';
    int line = 1;
    
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

    // public static int getLineNum() {
    //     return line;
    // }

    // Creates and returns the next token from the input code
    public Token getNextToken() throws IOException {
        // Skip whitespace
        while (SPACES.contains(c)) {
            if (c == '\r') {
                line += 1;
            }
            c = (char) System.in.read();
                // return new Token("NEWLINE", "\n");
            
            // } else if (c == '\r') {
            //     c = (char) System.in.read();
            //     return new Token("NEWLINE", "\r");

            // } else if (c == '\t') {
            //     c = (char) System.in.read();
            //     return new Token("TAB", "\t");

            // } else {
            //     c = (char) System.in.read();
            //     return new Token("SPACE", " ");
        }
        
        // Check for Comparators
        switch(c) {
            case '=':
                c = (char) System.in.read();
                if (c == '=') { 
                    c = (char) System.in.read();
                    return new Token("COMPARATOR", "==");
        
                } else {
                    return new Token("TERMINAL", "=");
                }
            
            case '<':
                c = (char) System.in.read();
                if (c == '=') {
                    c = (char) System.in.read();
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
                    return new Token("RESERVED", id);

                } else {
                    return new Token("ID", id);

                }

            // Add token to known symbols if not already included
            } else {
                symbols.add(id);
                return new Token("ID", id);

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
}
