package Tree;

public class Environment extends Node {

    private Node scope;          // innermost frame (association list)
    private Environment env;     // enclosing environment

    public Environment() {
        scope = Nil.getInstance();
        env = null;
    }

    public Environment(Environment e) {
        scope = Nil.getInstance();
        env = e;
    }

    public boolean isEnvironment() {
        return true;
    }

    // =========================================================
    // FIND helper: returns the binding pair (id . value-cell)
    // =========================================================
    private static Node find(Node id, Node alist) {
        if (!alist.isPair()) return null;

        Node bind = alist.getCar();

        if (id.getName().equals(bind.getCar().getName())) {
            return bind;
        }

        return find(id, alist.getCdr());
    }

    // =========================================================
    // LOOKUP (variable access)
    // =========================================================
    public Node lookup(Node id) {
        Node bind = find(id, scope);

        if (bind != null) {
            // bind = (id value)
            return bind.getCdr().getCar();   // ✅ FIXED
        }

        if (env != null) {
            return env.lookup(id);
        }

        throw new RuntimeException("Undefined variable: " + id.getName());
    }

    // =========================================================
    // DEFINE (add or update in current scope)
    // =========================================================
    public void define(Node id, Node val) {
        Node bind = find(id, scope);

        if (bind != null) {
            bind.setCdr(new Cons(val, Nil.getInstance()));
        } else {
            scope = new Cons(
                new Cons(id, new Cons(val, Nil.getInstance())),
                scope
            );
        }
    }

    // =========================================================
    // ASSIGN (set!)
    // =========================================================
    public void assign(Node id, Node val) {
        Node bind = find(id, scope);

        if (bind != null) {
            bind.setCdr(new Cons(val, Nil.getInstance()));
        } else if (env != null) {
            env.assign(id, val);
        } else {
            throw new RuntimeException("Undefined variable: " + id.getName());
        }
    }

    // alias for Scheme set!
    public void set(Node id, Node val) {
        assign(id, val);
    }
}