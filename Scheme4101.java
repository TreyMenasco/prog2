// Scheme4101 -- The main program of the Scheme interpreter.

import Parse.Scanner;
import java.io.FileInputStream;
import java.io.InputStream;
import Parse.Parser;
import Tokens.Token;
import Tokens.TokenType;
import Tree.*;

public class Scheme4101 {

	private static Environment env = null;

	 private static final String prompt = "Scheme4101> ";
	// private static final String prompt = "> ";

	 private static final String ini_file = "C:\\Users\\mecha\\Desktop\\prog2\\prog2\\ini.scm";

	public static void main(String argv[]) {

		// Create scanner that reads from standard input
		Scanner scanner = new Scanner(System.in);

		if (argv.length > 1 ||
				(argv.length == 1 && !argv[0].equals("-d"))) {
			System.err.println("Usage: java Scheme4101 [-d]");
			System.exit(1);
		}

		// If command line option -d is provided, debug the scanner
		if (argv.length == 1 && argv[0].equals("-d")) {

			Token tok = scanner.getNextToken();
			while (tok != null) {
				TokenType tt = tok.getType();

				System.out.print(tt.name());
				if (tt == TokenType.INT)
					System.out.println(", intVal = " + tok.getIntVal());
				else if (tt == TokenType.STRING)
					System.out.println(", strVal = " + tok.getStrVal());
				else if (tt == TokenType.IDENT)
					System.out.println(", name = " + tok.getName());
				else
					System.out.println();

				tok = scanner.getNextToken();
			}
			System.exit(0);
		}

		// Create parser
		Parser parser = new Parser(scanner);
		Node root;

		
		// create the top-level environment

		env = new Environment();
		BuiltIn.setGlobalEnv(env);
		//
		// populate the environment with BuiltIns and the code from ini.scm
		//
		BuiltIn.addBuiltIns(env);
		env = new Environment(env);
		BuiltIn.setGlobalEnv(env);
		 //input everything from ini.scm into the environment
		 try {
			InputStream iniStream = new FileInputStream(ini_file);
			Scanner iniScanner = new Scanner(iniStream);
			Parser iniParser = new Parser(iniScanner);
			

			Node expression = iniParser.parseExp();
			while (expression != null) {
				expression.eval(env);
				expression = iniParser.parseExp();
			} 
		}catch (Exception e) {
    		System.err.println("Error loading " + ini_file + ": " + e);
		}
		 

		// Read-eval-print loop

		
		root = parser.parseExp();
		while (root != null) {
			Node result = root.eval(env);
			if (result != null) {
				result.print(0);
			}
			root = parser.parseExp();
		}
		System.exit(0);
	}
}
