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
		//System.out.println("DEBUG operator: " + car.getName());
		if (form instanceof Define) {

    Node target = cdr.getCar();
    Node rest = cdr.getCdr();
			
    // (define (f params) body)
    if (target.isPair()) {

        Node functionName = target.getCar();
        Node params = target.getCdr();
        Node body = rest;

        Node lambdaExpr =
            new Cons(
                new Ident("lambda"),
                new Cons(params, body)
            );

        Node closure = lambdaExpr.eval(env);
        env.define(functionName, closure);
        return functionName;
    }

    // (define x expr)
	if (!rest.isPair()) {
    System.err.println("Error: malformed define");
    return Nil.getInstance();
	}
    //System.out.println("here");
    Node value = rest.getCar().eval(env);
    env.define(target, value);

    return target;
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

    Node rest1 = cdr.getCdr();
    Node rest2 = rest1.getCdr();

    if (!rest1.isPair() || !rest2.isPair()) {
        System.err.println("Error: malformed if expression");
        return Nil.getInstance();
    }

    Node test = cdr.getCar().eval(env);
    Node conseq = rest1.getCar();
    Node alt = rest2.getCar();

    return (!test.isNil()) ? conseq.eval(env) : alt.eval(env);
}
    else  if (form instanceof Quote) {
		//System.out.println("CAR = " + car.getName());
		//System.out.println("FORM = " + form.getClass());
    Node quoted = cdr;
    if (quoted.isNil()) {
        System.err.println("Error: quote missing argument");
        return Nil.getInstance();
    }
    return quoted.getCar();
	}
    else if (form instanceof Begin) {

    Node exprs = cdr;
    Node result = Nil.getInstance();

    while (exprs.isPair()) {
        result = exprs.getCar().eval(env);
        exprs = exprs.getCdr();
    }

    if (!exprs.isNil()) {
        System.err.println("Error: malformed begin expression");
    }

    return result;
}else if (form instanceof Let) {
		return handleLet(env);
	} else if (form instanceof Cond) {
    	return handleCond(env);
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

    if (list.isNil()) {
        return Nil.getInstance();
    }

    if (!list.isPair()) {
        System.err.println("ERROR: malformed list in evalList: " + list);
        Thread.dumpStack();
        return Nil.getInstance();
    }

    Node car = list.getCar().eval(env);
    Node cdr = evalList(list.getCdr(), env);

    return new Cons(car, cdr);
}
	private Node handleLet(Environment env) {

    // (let ((x 1) (y 2)) body...)

    Node bindings = cdr.getCar();   // ((x 1) (y 2))
    Node body = cdr.getCdr();       // body expressions

    Environment newEnv = new Environment(env);

    // Step 1: evaluate bindings
    while (!bindings.isNil()) {

        Node bind = bindings.getCar();   // (x 1)

        Node name = bind.getCar();
        Node valueExpr = bind.getCdr().getCar();

        Node value = valueExpr.eval(env);

        newEnv.define(name, value);

        bindings = bindings.getCdr();
    }

    // Step 2: evaluate body in new environment
    Node result = Nil.getInstance();
    Node exprs = body;

    while (!exprs.isNil()) {
        result = exprs.getCar().eval(newEnv);
        exprs = exprs.getCdr();
    }

    return result;
}
private Node handleCond(Environment env) {

    Node clauses = cdr;

    while (!clauses.isNil()) {

        Node clause = clauses.getCar();   // (test expr...)

        Node test = clause.getCar();

        // check for else
        boolean isElse =
            test.isSymbol() &&
            test.getName().equalsIgnoreCase("else");

        Node testResult;

        if (isElse) {
            testResult = BooleanLit.getInstance(true);
        } else {
            testResult = test.eval(env);
        }

        if (!testResult.isNil()) {

            // evaluate all expressions in clause, return last
            Node exprs = clause.getCdr();
            Node result = Nil.getInstance();

            while (!exprs.isNil()) {
                result = exprs.getCar().eval(env);
                exprs = exprs.getCdr();
            }

            return result;
        }

        clauses = clauses.getCdr();
    }

    return Nil.getInstance();
}
}
