// BuiltIn.java -- the data structure for function closures

// Class BuiltIn is used for representing the value of built-in functions
// such as +.  Populate the initial environment with
// (name, new BuiltIn(name)) pairs.

// The object-oriented style for implementing built-in functions would be
// to include the Java methods for implementing a Scheme built-in in the
// BuiltIn object.  This could be done by writing one subclass of class
// BuiltIn for each built-in function and implementing the method apply
// appropriately.  This requires a large number of classes, though.
// Another alternative is to program BuiltIn.apply() in a functional
// style by writing a large if-then-else chain that tests the name of
// of the function symbol.

package Tree;
import Parse.Scanner;
import Parse.Parser;
import java.io.FileInputStream;
import java.io.IOException;

public class BuiltIn extends Node {
    // TODO: For allowing the built-in functions to access the environment,
    // keep a copy of the Environment here and synchronize it with
    // class Scheme4101.

    private static Environment globalEnv = null;
    
    public static void setGlobalEnv(Environment env) {
        globalEnv = env;
    }

    private Node symbol;

    public BuiltIn(Node s) {
        symbol = s;
    }

    public Node getSymbol() {
        return symbol;
    }

    public boolean isProcedure() {
        return true;
    }

    public void print(int n) {
        // there got to be a more efficient way to print n spaces
        for (int i = 0; i < n; i++)
            System.out.print(' ');
        System.out.print("#{Built-in Procedure ");
        if (symbol != null)
            symbol.print(-Math.abs(n) - 1);
        System.out.print('}');
        if (n >= 0)
            System.out.println();
    }

    // TODO: The method apply() should be defined in class Node
    // to report an error. It should be overwritten only in classes
    // BuiltIn and Closure.
    public Node apply(Node args) {
        
        String name = symbol.getName();
        Node arg1 = args.getCar();
        //Cannot compute globally or other methods call it and return errors
        //Node arg2 = args.getCdr().getCar();
        if (name.equals("load")) {
            if (!arg1.isString()) {
                System.err.println("Error: load expected a string arg");
                return Nil.getInstance();
            }
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
            return Nil.getInstance();  // or Unspecific.getInstance();
        } else if(name.equals("b=")) {
            Node arg2 = args.getCdr().getCar();
            if (!arg1.isNumber() || !arg2.isNumber()) {
                System.out.println("be: must be given two numbers");
                return Nil.getInstance();
            }
            int x = ((IntLit) arg1).getValue();
            int y = ((IntLit) arg2).getValue();

            return x == y ? BooleanLit.getInstance(true) : Nil.getInstance();
        } else if(name.equals("b<")) {
            Node arg2 = args.getCdr().getCar();
            if (!arg1.isNumber() || !arg2.isNumber()) {
                System.out.println("b<: must be given two numbers");
                return Nil.getInstance();
            }
            int x = ((IntLit) arg1).getValue();
            int y = ((IntLit) arg2).getValue();
            return x < y ? BooleanLit.getInstance(true) : Nil.getInstance();
        } else if (name.equals("b>")) {
            Node arg2 = args.getCdr().getCar();
             if (!arg1.isNumber() || !arg2.isNumber()) {
                 System.out.println("b>: must be given two numbers");
                 return Nil.getInstance();
             }
             int x = ((IntLit) arg1).getValue();
             int y = ((IntLit) arg2).getValue();
             return x > y ? BooleanLit.getInstance(true) : Nil.getInstance();
        } else if (name.equals("b+")) {
            Node arg2 = args.getCdr().getCar();
            if (!arg1.isNumber() || !arg2.isNumber()) {
                System.out.println("b+: must be given two numbers");
                return Nil.getInstance();
            }
            int x = ((IntLit) arg1).getValue();
            int y = ((IntLit) arg2).getValue();
            return new IntLit(x + y);
        } else if (name.equals("b-")) {
            Node arg2 = args.getCdr().getCar();
            if (!arg1.isNumber() || !arg2.isNumber()) {
                System.out.println("b-: must be given two numbers");
                return Nil.getInstance();
            }
            int x = ((IntLit) arg1).getValue();
            int y = ((IntLit) arg2).getValue();
            return new IntLit(x - y);
        } else if (name.equals("b*")) {
            Node arg2 = args.getCdr().getCar();
            if (!arg1.isNumber() || !arg2.isNumber()) {
                System.out.println("b*: must be given two numbers");
                return Nil.getInstance();
            }
            int x = ((IntLit) arg1).getValue();
            int y = ((IntLit) arg2).getValue();
            return new IntLit(x * y);
        } else if (name.equals("b/")) {
            Node arg2 = args.getCdr().getCar();
            if (!arg1.isNumber() || !arg2.isNumber()) {
                System.out.println("b/: must be given two numbers");
                return Nil.getInstance();
            }
            int x = ((IntLit) arg1).getValue();
            int y = ((IntLit) arg2).getValue();
            if (y == 0) {
                System.out.println("Error: division by zero");
                return Nil.getInstance();
            }
            return new IntLit(x / y);
        } else if(name.equals("eq?")) {
            Node arg2 = args.getCdr().getCar();
            return arg1 == arg2 ? BooleanLit.getInstance(true) : Nil.getInstance();
        } else if (name.equals("cons")) {
            Node arg2 = args.getCdr().getCar();
            //System.out.println("cons: arguments are " + arg1 + " and " + arg2);
            return new Cons(arg1, arg2);
        } else if (name.equals("car")) {
            //System.out.println("car: argument is " + arg1);
            if (!arg1.isPair()) {
            System.out.println("car: argument is not a pair");
            return Nil.getInstance();
        }
            return arg1.getCar();
        } else if (name.equals("cdr")) {
            if (!arg1.isPair()) {
            System.out.println("cdr: argument is not a pair");
            return Nil.getInstance();
            }
            return arg1.getCdr();
        } else if (name.equals("set-car!")) {
            Node arg2 = args.getCdr().getCar();
            if (!arg1.isPair()) {
                System.out.println("set-car!: first argument must be a pair");
                return Nil.getInstance();
            }
            arg1.setCar(arg2);
            return Nil.getInstance();
        } else if (name.equals("set-cdr!")) {
            Node arg2 = args.getCdr().getCar();
            if (!arg1.isPair()) {
                System.out.println("set-cdr!: first argument must be a pair");
                return Nil.getInstance();
            }
            arg1.setCdr(arg2);
            return Nil.getInstance();
        } else if (name.equals("pair?")) {
            //check if these 2 are right
            return arg1.isPair() ? BooleanLit.getInstance(true) : Nil.getInstance();
        } else if (name.equals("symbol?")) {
            return arg1.isSymbol() ? BooleanLit.getInstance(true) : Nil.getInstance();
        } else if (name.equals("null?")) {
            return arg1.isNil() ? BooleanLit.getInstance(true) : Nil.getInstance();
        } else if (name.equals("number?")) {
            return arg1.isNumber() ? BooleanLit.getInstance(true) : Nil.getInstance();
        } else if (name.equals("apply")) {
            Node function = args.getCar();
            Node functionArgs = args.getCdr().getCar();

            if (!function.isProcedure()) {
                System.err.println("Error: trying to apply a non-function");
                return Nil.getInstance();
            }
            return function.apply(functionArgs);
        } else {
            System.err.println("Error: unknown built-in function " + name);
            return Nil.getInstance();
        }
    }

    // The easiest way to implement BuiltIn.apply is as an
    // if-then-else chain testing for the different names of
    // the built-in functions.  E.g., here's how load could
    // be implemented:
    public static void addBuiltIns(Environment env) {
    String[] names = {
        "load",
        "b=", "b<", "b>", "b+", "b-", "b*", "b/",
        "cons", "car", "cdr",
        "set-car!", "set-cdr!",
        "pair?", "symbol?", "null?", "number?",
        "eq?",
        "apply"
    };

    for (String name : names) {
        Node sym = new Ident(name); // IMPORTANT: use your symbol class
        env.define(sym, new BuiltIn(sym));
    }
}
     
}
