package tfg;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4viz.VizGUI;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Module;
import edu.mit.csail.sdg.parser.CompUtil;
import edu.mit.csail.sdg.translator.A4Options;
import edu.mit.csail.sdg.translator.A4Solution;
import edu.mit.csail.sdg.translator.TranslateAlloyToKodkod;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Parser {

    private static final String FILE_PATH = "src/main/resources/Tree.java";
    private static final String OUTPUT_PATH = "outputs/test.als";

    public static void main(String[] args) throws Exception {
        CompilationUnit cu = StaticJavaParser.parse((new FileInputStream(FILE_PATH)));
        //System.out.println(cu);

        List<String> modelText = new ArrayList<>();
        VoidVisitor<List<String>> methodNameVisitor = new ClassSigPrinter();
        methodNameVisitor.visit(cu, modelText);

        try (PrintWriter out = new PrintWriter(OUTPUT_PATH)) {
            modelText.forEach(line -> out.println(line));
            out.println("run autoTest{} for 2");  //run command for the model
        } catch (IOException e) {
            System.out.println("Error generando fichero Alloy.");
            e.printStackTrace();
        }


        //Alloy API starts here
        //Visualizer
        VizGUI viz = null;
        //Reporter
        A4Reporter rep = new A4Reporter() {
            // Print warnings to System.out
            @Override public void warning(ErrorWarning msg) {
                System.out.print("Alloy Reporter Warning: "+(msg.toString().trim())+"\n");
                System.out.flush();
            }
        };
        //Read the module
        Module world = CompUtil.parseEverything_fromFile(rep, null, OUTPUT_PATH);
        //Options
        A4Options options = new A4Options();
        options.solver = A4Options.SatSolver.SAT4J;
        //Execute commands. There should only be one
        for (Command command: world.getAllCommands()) {
            A4Solution sol = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);
            // Print the outcome
            System.out.println(sol);
            // If satisfiable...
            if (sol.satisfiable()) {
                // You can query "sol" to find out the values of each set or type.
                // This can be useful for debugging.
                //
                // You can also write the outcome to an XML file
                sol.writeXML("alloy_example_output.xml");
                //
                // You can then visualize the XML file by calling this:
                if (viz==null) {
                    viz = new VizGUI(false, "alloy_example_output.xml", null);
                } else {
                    viz.loadXML("alloy_example_output.xml", true);
                }
            }
        }


    }

    private static class ClassSigPrinter extends VoidVisitorAdapter<List<String>> {
        @Override
        public void visit(ClassOrInterfaceDeclaration cd, List<String> model) {
            super.visit(cd,model);

            model.add("sig " + cd.getName() + " {");
            cd.getChildNodes().forEach(ch -> {
                if(ch instanceof FieldDeclaration) {

                    ((FieldDeclaration) ch).getVariables().forEach(v ->
                            model.add("\t" + v.getName() + ": " + correctPrintType(v.getTypeAsString()) + ",")
                    );
                }
            });
            model.add("}");
        }

        public String correctPrintType(String type) {
            if (Objects.equals(type, "int"))
                return "Int";
            return type;
        }
    }





    /*

        Unused classes

     */
    /*
    private static class ClassSigPrinterDeprecated extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(ClassOrInterfaceDeclaration cd, Void arg) {
            super.visit(cd,arg);

            try (PrintWriter out = new PrintWriter(new FileOutputStream(OUTPUT_PATH, true))) {
                out.println("sig " + cd.getName() + " {");

                cd.getChildNodes().forEach(ch -> {
                    if(ch instanceof FieldDeclaration) {

                        ((FieldDeclaration) ch).getVariables().forEach(v ->
                                out.println("\t" + v.getName() + ": " + correctPrintType(v.getTypeAsString()) + ",")
                        );
                    }});

                out.println("}");
            } catch (IOException e) {
                System.out.println("Error escribiendo fichero Alloy.");
                e.printStackTrace();
            }

        }

        public String correctPrintType(String type) {
            if (Objects.equals(type, "int"))
                return "Int";
            return type;
        }
    }*/
}
