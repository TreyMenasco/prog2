package Tree;

import Parse.Scanner;
import Parse.Parser;
import java.io.FileInputStream;
import java.io.IOException;

public class BuiltIn extends Node {

    private static Environment globalEnv = null;

    public static void setGlobalEnv(Environment env) {
        globalEnv = env;
    }

    private Node symbol;

    public BuiltIn(Node s) {
        symbol = s;
    }

    public boolean isProcedure() {
        return true;
    }

    public Node apply(Node args) {

        String name = symbol.getName();
        Node arg1 = (args != null && !args.isNil()) ? args.getCar() : null;

        // LOAD
        if (name.equals("load")) {
            String filename = arg1.getName();
            try {
                Scanner scanner = new Scanner(new FileInputStream(filename));
                Parser parser = new Parser(scanner);

                Node root = parser.parseExp();
                while (root != null) {
                    root.eval(globalEnv);
                    root = parser.parseExp();
                }
            } catch (IOException e) {
                System.err.println("Could not find file " + filename);
            }
            return Nil.getInstance();
        }

        // ARITHMETIC
        else if (name.equals("b+") || name.equals("b-") || name.equals("b*") || name.equals("b/") ||
                 name.equals("b=") || name.equals("b<") || name.equals("b>")) {

            Node arg2 = args.getCdr().getCar();

            int x = ((IntLit) arg1).getValue();
            int y = ((IntLit) arg2).getValue();

            switch (name) {
                case "b+": return new IntLit(x + y);
                case "b-": return new IntLit(x - y);
                case "b*": return new IntLit(x * y);
                case "b/": return new IntLit(x / y);
                case "b=": return x == y ? BooleanLit.getInstance(true) : Nil.getInstance();
                case "b<": return x < y ? BooleanLit.getInstance(true) : Nil.getInstance();
                case "b>": return x > y ? BooleanLit.getInstance(true) : Nil.getInstance();
            }
        }

        // LIST OPS
        else if (name.equals("cons")) {
            return new Cons(arg1, args.getCdr().getCar());
        }
        else if (name.equals("car")) {
            return arg1.getCar();
        }
        else if (name.equals("cdr")) {
            return arg1.getCdr();
        }

        // PREDICATES
        else if (name.equals("null?")) return arg1.isNil() ? BooleanLit.getInstance(true) : Nil.getInstance();
        else if (name.equals("number?")) return arg1.isNumber() ? BooleanLit.getInstance(true) : Nil.getInstance();
        else if (name.equals("symbol?")) return arg1.isSymbol() ? BooleanLit.getInstance(true) : Nil.getInstance();
        else if (name.equals("pair?")) return arg1.isPair() ? BooleanLit.getInstance(true) : Nil.getInstance();
        else if (name.equals("procedure?")) return arg1.isProcedure() ? BooleanLit.getInstance(true) : Nil.getInstance();

        // EVAL/APPLY
        else if (name.equals("eval")) return arg1.eval(globalEnv);
        else if (name.equals("apply")) return arg1.apply(args.getCdr().getCar());

        // IO
        else if (name.equals("write")) {
            arg1.print(0);
            return Nil.getInstance();
        }
        else if (name.equals("newline")) {
            System.out.println();
            return Nil.getInstance();
        }

        return Nil.getInstance();
    }

    public static void addBuiltIns(Environment env) {
        String[] names = {
            "load", "b+", "b-", "b*", "b/", "b=", "b<", "b>",
            "cons", "car", "cdr",
            "null?", "number?", "symbol?", "pair?", "procedure?",
            "eval", "apply",
            "write", "newline"
        };

        for (String name : names) {
            env.define(new Ident(name), new BuiltIn(new Ident(name)));
        }
    }
}