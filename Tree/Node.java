package Tree;

public class Node {

    public void print(int n) {
    }

    public void print(int n, boolean p) {
        print(n);
    }

    public boolean isBoolean() { return false; }
    public boolean isNumber() { return false; }
    public boolean isString() { return false; }
    public boolean isSymbol() { return false; }
    public boolean isNull() { return false; }
    public boolean isNil() { return this == Nil.getInstance(); }
    public boolean isPair() { return false; }
    public boolean isProcedure() { return false; }
    public boolean isEnvironment() { return false; }

    public static void print(Node t, int n, boolean p) {
        t.print(n, p);
    }

    public static Node getCar(Node t) {
        return t.getCar();
    }

    public static Node getCdr(Node t) {
        return t.getCdr();
    }

    public static boolean isNull(Node t) {
        return t.isNull();
    }

    public static boolean isPair(Node t) {
        return t.isPair();
    }

    public Node getCar() {
        System.err.println("Error: argument of car is not a pair");
        return Nil.getInstance();
    }

    public Node getCdr() {
        System.err.println("Error: argument of cdr is not a pair");
        return Nil.getInstance();
    }

    public void setCar(Node a) {
        System.err.println("Error: argument of set-car! is not a pair");
    }

    public void setCdr(Node d) {
        System.err.println("Error: argument of set-cdr! is not a pair");
    }

    public String getName() {
        return "";
    }

    // ===================== EVAL =====================
    public Node eval(Environment env) {

        // Self-evaluating
        if (this.isNumber() || this.isString() || this.isBoolean()) {
            return this;
        }

        // Variable lookup
        if (this.isSymbol()) {
            return env.lookup(this);
        }

        // Must be a list
        if (!this.isPair()) {
            System.err.println("Error: invalid expression");
            return Nil.getInstance();
        }

        Node first = this.getCar();
        Node rest = this.getCdr();

        // -------- Special Forms --------
        if (first.isSymbol()) {
            String name = first.getName();

            // quote
            if (name.equals("quote")) {
                return rest.getCar();
            }

            // if
            else if (name.equals("if")) {
                Node cond = rest.getCar().eval(env);

                if (!(cond instanceof BooleanLit && cond == BooleanLit.getInstance(false))) {
                    return rest.getCdr().getCar().eval(env);
                } else {
                    return rest.getCdr().getCdr().getCar().eval(env);
                }
            }

            // define
            else if (name.equals("define")) {
                Node var = rest.getCar();
                Node val = rest.getCdr().getCar().eval(env);

                env.define(var, val);
                return var;
            }

            // lambda
            else if (name.equals("lambda")) {
                return new Closure(this, env);
            }

            // begin
            else if (name.equals("begin")) {
                Node result = Nil.getInstance();
                Node current = rest;

                while (!current.isNil()) {
                    result = current.getCar().eval(env);
                    current = current.getCdr();
                }
                return result;
            }

            // set!
            else if (name.equals("set!")) {
                Node var = rest.getCar();
                Node val = rest.getCdr().getCar().eval(env);

                env.set(var, val);
                return val;
            }

            // cond
            else if (name.equals("cond")) {
                Node clauses = rest;

                while (!clauses.isNil()) {
                    Node clause = clauses.getCar();

                    Node test = clause.getCar();
                    Node expr = clause.getCdr().getCar();

                    if (test.isSymbol() && test.getName().equals("else")) {
                        return expr.eval(env);
                    }

                    Node testVal = test.eval(env);

                    if (!(testVal instanceof BooleanLit && testVal == BooleanLit.getInstance(false))) {
                        return expr.eval(env);
                    }

                    clauses = clauses.getCdr();
                }

                return Nil.getInstance();
            }
        }

        // -------- Function Call --------
        Node func = first.eval(env);
        Node args = evalList(rest, env);

        return func.apply(args);
    }

    // ===================== EVAL LIST =====================
    private Node evalList(Node list, Environment env) {
        if (list.isNil()) {
            return Nil.getInstance();
        }

        return new Cons(
            list.getCar().eval(env),
            evalList(list.getCdr(), env)
        );
    }

    public Node apply(Node args) {
        System.err.println("Error: trying to apply a non-function");
        return Nil.getInstance();
    }
}