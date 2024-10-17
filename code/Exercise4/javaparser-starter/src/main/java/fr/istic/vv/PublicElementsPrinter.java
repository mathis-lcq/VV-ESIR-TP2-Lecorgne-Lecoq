package fr.istic.vv;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;


// This class visits a compilation unit and
// prints all public enum, classes or interfaces along with their public methods
public class PublicElementsPrinter extends VoidVisitorWithDefaults<Void> {

    private final BufferedWriter writer;

    // Constructor to open the file for writing
    public PublicElementsPrinter(String outputPath) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(outputPath, false));
    }

    // Close the writer after use
    public void close() throws IOException {
        writer.close(
        );
    }

    @Override
    public void visit(CompilationUnit unit, Void arg) {
        for(TypeDeclaration<?> type : unit.getTypes()) {
            type.accept(this, null);
        }
    }

    public void visitTypeDeclaration(TypeDeclaration<?> declaration, Void arg) {
        if(!declaration.isPublic()) return;
        System.out.println(declaration.getFullyQualifiedName().orElse("[Anonymous]"));
        for(MethodDeclaration method : declaration.getMethods()) {
            method.accept(this, arg);
        }
        // Printing nested types in the top level
        for(BodyDeclaration<?> member : declaration.getMembers()) {
            if (member instanceof TypeDeclaration)
                member.accept(this, arg);
        }
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {
        if (!declaration.isPublic()) return;

        Set<String> privateFields = new HashSet<>();
        Set<String> publicGetters = new HashSet<>();
        String className = declaration.getNameAsString();
        String packageName = declaration.findCompilationUnit()
                .flatMap(CompilationUnit::getPackageDeclaration)
                .map(pd -> pd.getNameAsString())
                .orElse("[No Package]");

        // Collect private fields
        for (BodyDeclaration<?> member : declaration.getMembers()) {
            if (member instanceof FieldDeclaration) {
                FieldDeclaration field = (FieldDeclaration) member;
                field.getVariables().forEach(variable -> {
                    if (field.isPrivate()) {
                        privateFields.add(variable.getNameAsString());
                    }
                });
            } else if (member instanceof MethodDeclaration) {
                MethodDeclaration method = (MethodDeclaration) member;
                // Collect public getters
                if (method.isPublic() && method.getNameAsString().startsWith("get")) {
                    String fieldName = method.getNameAsString().substring(3).toLowerCase(); // Extract field name
                    publicGetters.add(fieldName);
                }
            }
        }

        // Write "no getters" to the report file
        for (String field : privateFields) {
            if (!publicGetters.contains(field)) {
                try {
                    writer.write("Class: " + className + ", Package: " + packageName + ", Private field: " + field + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Call this to flush and close the writer after visiting all files
    public void finish() throws IOException {
        writer.close();
    }

    @Override
    public void visit(EnumDeclaration declaration, Void arg) {
        visitTypeDeclaration(declaration, arg);
    }

    @Override
    public void visit(MethodDeclaration declaration, Void arg) {
        if(!declaration.isPublic()) return;
        System.out.println("  " + declaration.getDeclarationAsString(true, true));
    }

}
