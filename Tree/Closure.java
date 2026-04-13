package Tree;

public class Closure extends Node {
    private Node fun;
    private Environment env;

    public Closure(Node f, Environment e) {
        fun = f;
        env = e;
    }

    public boolean isProcedure() {
        return true;
    }

    public Node apply(Node args) {

        Node params = fun.getCdr().getCar();
        Node body = fun.getCdr().getCdr();

        Environment newEnv = new Environment(env);

        Node p = params;
        Node a = args;

        while (p.isPair() && a.isPair()) {
            newEnv.define(p.getCar(), a.getCar());
            p = p.getCdr();
            a = a.getCdr();
        }

        if (!p.isNil() || !a.isNil()) {
            throw new RuntimeException("Wrong number of arguments");
        }

        Node result = Nil.getInstance();
        Node exprs = body;

        while (exprs.isPair()) {
            result = exprs.getCar().eval(newEnv);
            exprs = exprs.getCdr();
        }

        return result;
    }
}