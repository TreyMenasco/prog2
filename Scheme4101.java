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
    private static final String ini_file = "C:\\Users\\mecha\\Desktop\\prog2\\prog2\\ini.scm";

    public static void main(String argv[]) {

        Scanner scanner = new Scanner(System.in);

        // Debug mode
        if (argv.length > 1 ||
            (argv.length == 1 && !argv[0].equals("-d"))) {
            System.err.println("Usage: java Scheme4101 [-d]");
            System.exit(1);
        }

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

        Parser parser = new Parser(scanner);

        // ✅ Create environment ONCE
        env = new Environment();

        // ✅ Add built-ins
        BuiltIn.addBuiltIns(env);
        BuiltIn.setGlobalEnv(env);

        // ✅ Load ini.scm properly
        try {
            InputStream iniStream = new FileInputStream(ini_file);
            Scanner iniScanner = new Scanner(iniStream);
            Parser iniParser = new Parser(iniScanner);

            Node expr = iniParser.parseExp();
            while (expr != null) {
                expr.eval(env);
                expr = iniParser.parseExp();
            }

        } catch (Exception e) {
            System.err.println("Error loading " + ini_file + ": " + e);
        }

        // ✅ REPL loop
        while (true) {
            try {
                System.out.print(prompt);

                Node root = parser.parseExp();

                if (root == null) {
                    System.out.println();
                    break;
                }

                Node result = root.eval(env);

                if (result != null) {
                    result.print(0);
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        System.exit(0);
    }
}