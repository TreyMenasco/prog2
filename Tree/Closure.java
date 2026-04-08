// Closure.java -- the data structure for function closures

// Class Closure is used to represent the value of lambda expressions.
// It consists of the lambda expression itself, together with the
// environment in which the lambda expression was evaluated.

// The method apply() takes the environment out of the closure,
// adds a new frame for the function call, defines bindings for the
// parameters with the argument values in the new frame, and evaluates
// the function body.

package Tree;

public class Closure extends Node {
    private Node fun; // a lambda expression
    private Environment env; // the environment in which
                             // the function was defined

    public Closure(Node f, Environment e) {
        fun = f;
        env = e;
    }

    public Node getFun() {
        return fun;
    }

    public Environment getEnv() {
        return env;
    }

    public boolean isProcedure() {
        return true;
    }

    public void print(int n) {
        // there got to be a more efficient way to print n spaces
        for (int i = 0; i < n; i++)
            System.out.print(' ');
        System.out.println("#{Procedure");
        if (fun != null)
            fun.print(Math.abs(n) + 2);
        for (int i = 0; i < Math.abs(n); i++)
            System.out.print(' ');
        System.out.println(" }");
    }
    // TODO: The method apply() should be defined in class Node
    // to report an error. It should be overwritten only in classes
    // BuiltIn and Closure.
    public Node apply(Node args) {
     
        Node params = fun.getCdr().getCar();
        Node body = fun.getCdr().getCdr();
        Environment newEnv = new Environment(env);

         // 3. Bind each parameter to the corresponding argument
        Node p = params;
        Node a = args;
        while (p.isPair() && a.isPair()) {
            newEnv.define(p.getCar(), a.getCar());
            p = p.getCdr();
            a = a.getCdr();
        }

        // Check for mismatched number of args vs parameters
        if (!p.isNil() || !a.isNil()) {
            System.out.println("Error: wrong number of arguments");
            return Nil.getInstance();
        }

        // 4. Evaluate the function body in the new environment
        // If the body is a sequence of expressions, evaluate each and return the last
        Node result = Nil.getInstance();
        Node exprs = body;
        while (exprs.isPair()) {
            result = exprs.getCar().eval(newEnv);
            exprs = exprs.getCdr();
        }

        return result;
    }
}
