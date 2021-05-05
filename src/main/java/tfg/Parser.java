package tfg;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.*;
import java.util.Objects;


public class Parser {

    private static final String FILE_PATH = "src/main/resources/Tree.java";

    public static void main(String[] args) throws Exception {
        CompilationUnit cu = StaticJavaParser.parse((new FileInputStream(FILE_PATH)));
        //System.out.println(cu);

        //Re-create output file
        //TODO: limpiar creacion de fichero
        try (PrintWriter f = new PrintWriter("outputs/test.als")) {

        } catch (IOException e) {
            System.out.println("Error creando fichero Alloy.");
            e.printStackTrace();
        }
        VoidVisitor<Void> methodNameVisitor = new ClassSigPrinter();
        methodNameVisitor.visit(cu, null);

        //Print run command
        try (PrintWriter out = new PrintWriter(new FileOutputStream("outputs/test.als", true))) {
            out.println("run autoTest{} for 2");
        } catch (IOException e) {
            System.out.println("Error escribiendo fichero Alloy.");
            e.printStackTrace();
        }
        //TODO: recoger string de visit para crear y escribir el fichero de una vez
    }

    private static class ClassSigPrinter extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(ClassOrInterfaceDeclaration cd, Void arg) {
            super.visit(cd,arg);

            try (PrintWriter out = new PrintWriter(new FileOutputStream("outputs/test.als", true))) {
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
    }



    /*

        Unused classes

     */
    private static class VarSigPrinter extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(FieldDeclaration fd, Void arg) {
            super.visit(fd,arg);
            System.out.println("field: " + fd);

            fd.getVariables().forEach(v -> System.out.println(v));
            System.out.println(fd.getParentNode().getClass());

            System.out.println("-----------");
/*
            for (NodeList<VariableDeclarator> child : cl.getChildNodes()) {
                //if (child.is)
                System.out.println("M: " + child);
            }*/
            //System.out.println("Metodo: " + c.getName());
        }
    }
}
