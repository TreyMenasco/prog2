// Cons -- Parse tree node class for representing a Cons node

package Tree;

import Special.Begin;
import Special.Cond;
import Special.Define;
import Special.If;
import Special.Lambda;
import Special.Let;
import Special.Quote;
import Special.Regular;
import Special.Set;
import Special.Special;

public class Cons extends Node {
	private Node car;
	private Node cdr;
	private Special form;

	public Cons(Node a, Node d) {
		car = a;
		cdr = d;
		parseList();
	}

	// parseList() `parses' special forms, constructs an appropriate
	// object of a subclass of Special, and stores a pointer to that
	// object in variable form. It would be possible to fully parse
	// special forms at this point. Since this causes complications
	// when using (incorrect) programs as data, it is easiest to let
	// parseList only look at the car for selecting the appropriate
	// object from the Special hierarchy and to leave the rest of
	// parsing up to the interpreter.
	void parseList() {

		if (!car.isSymbol())
			form = new Regular();
		else {
			String sym = car.getName();

			if (sym.equalsIgnoreCase("begin"))
				form = new Begin();
			else if (sym.equalsIgnoreCase("cond"))
				form = new Cond();
			else if (sym.equalsIgnoreCase("define"))
				form = new Define();
			else if (sym.equalsIgnoreCase("if"))
				form = new If();
			else if (sym.equalsIgnoreCase("lambda"))
				form = new Lambda();
			else if (sym.equalsIgnoreCase("let"))
				form = new Let();
			else if (sym.equalsIgnoreCase("quote"))
				form = new Quote();
			else if (sym.equalsIgnoreCase("set!"))
				form = new Set();
			else
				form = new Regular();
		}
	}

	public void print(int n) {
		form.print(this, n, false);
	}

	public void print(int n, boolean p) {
		form.print(this, n, p);
	}

	public boolean isPair() {
		return true;
	}

	public void setCar(Node a) {
		car = a;
		parseList();
	}

	public void setCdr(Node d) {
		cdr = d;
	}

	public Node getCar() {
		return car;
	}

	public Node getCdr() {
		return cdr;
	}
	
	public Node eval(Environment env) {
		if (form instanceof Define) {
        // (define name expr)
        Node nameNode = cdr.getCar();
        Node valueNode = cdr.getCdr().getCar();
        Node value = valueNode.eval(env);
        env.define(nameNode, value);
        return nameNode;
    } 
    else if (form instanceof Set) {
        // (set! name expr)
        Node nameNode = cdr.getCar();
        Node valueNode = cdr.getCdr().getCar();
        Node value = valueNode.eval(env);
        env.assign(nameNode, value);
        return value;
    } 
    else if (form instanceof Lambda) {
        // (lambda (params) body)
        return new Closure(this, env);
    } 
    else if (form instanceof If) {
        Node test = cdr.getCar().eval(env);
        Node conseq = cdr.getCdr().getCar();
        Node alt = cdr.getCdr().getCdr().getCar();
        return (!test.isNil()) ? conseq.eval(env) : alt.eval(env);
    } 
    else if (form instanceof Quote) {
        return cdr.getCar(); // just return the quoted expression
    } 
    else if (form instanceof Begin) {
        Node result = Nil.getInstance();
        Node exprs = cdr;
        while (!exprs.isNil()) {
            result = exprs.getCar().eval(env);
            exprs = exprs.getCdr();
        }
        return result;
    }
    else {
        // Regular function application
        Node operatorNode = car.eval(env);
        Node argument = evalList(cdr, env);

        if (!operatorNode.isProcedure()) {
            System.err.println("Error: trying to apply a non-function");
            return Nil.getInstance();
        }
        return operatorNode.apply(argument);
    }
	}
	public Node evalList(Node list, Environment env) {
        if (list.isNil()) return Nil.getInstance();
        return new Cons(list.getCar().eval(env), evalList(list.getCdr(), env));
    }
}
