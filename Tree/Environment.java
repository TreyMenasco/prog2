package Tree;

public class Environment extends Node {

    private Node scope;
    private Environment env;

    public Environment() {
        scope = Nil.getInstance();
        env = null;
    }

    public Environment(Environment e) {
        scope = Nil.getInstance();
        env = e;
    }

    private static Node find(Node id, Node alist) {
        if (!alist.isPair()) return null;

        Node bind = alist.getCar();
        if (id.getName().equals(bind.getCar().getName()))
            return bind;

        return find(id, alist.getCdr());
    }

    public Node lookup(Node id) {
        Node bind = find(id, scope);

        if (bind != null) return bind.getCdr();
        if (env != null) return env.lookup(id);

        throw new RuntimeException("Undefined variable: " + id.getName());
    }

    public void define(Node id, Node val) {
        Node bind = find(id, scope);

        if (bind != null) bind.setCdr(val);
        else scope = new Cons(new Cons(id, val), scope);
    }

    public void assign(Node id, Node val) {
        Node bind = find(id, scope);

        if (bind != null) bind.setCdr(val);
        else if (env != null) env.assign(id, val);
        else throw new RuntimeException("Undefined variable: " + id.getName());
    }

    public void set(Node id, Node val) {
        assign(id, val);
    }
}