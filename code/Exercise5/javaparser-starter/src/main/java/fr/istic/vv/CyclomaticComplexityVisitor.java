package fr.istic.vv;

import java.util.HashMap;
import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class CyclomaticComplexityVisitor extends VoidVisitorAdapter<Void> {
    private int complexity;
    private String currentPackage;
    private Map<String, MethodInfo> methodComplexityMap = new HashMap<>();

    public CyclomaticComplexityVisitor() {
        this.complexity = 1;
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        this.complexity = 1;  // Reset
        super.visit(n, arg);

        String methodName = n.getNameAsString();
        String parameterTypes = n.getParameters().stream()
                .map(Parameter::getTypeAsString)
                .reduce((a, b) -> a + ", " + b)
                .orElse("No parameters");
        String className = n.findAncestor(ClassOrInterfaceDeclaration.class).map(ClassOrInterfaceDeclaration::getNameAsString).orElse("No class");

        // Save method info with its complexity
        methodComplexityMap.put(methodName, new MethodInfo(currentPackage, className, methodName, parameterTypes, complexity));
    }

    // Increment complexity for each control flow structure
    @Override
    public void visit(IfStmt n, Void arg) {
        super.visit(n, arg);
        complexity++;
    }

    @Override
    public void visit(ForStmt n, Void arg) {
        super.visit(n, arg);
        complexity++;
    }

    @Override
    public void visit(WhileStmt n, Void arg) {
        super.visit(n, arg);
        complexity++;
    }

    @Override
    public void visit(ForEachStmt n, Void arg) {
        super.visit(n, arg);
        complexity++;
    }

    @Override
    public void visit(SwitchStmt n, Void arg) {
        super.visit(n, arg);
        complexity++;
    }

    @Override
    public void visit(DoStmt n, Void arg) {
        super.visit(n, arg);
        complexity++;
    }

    @Override
    public void visit(CatchClause n, Void arg) {
        super.visit(n, arg);
        complexity++;
    }

    public void setCurrentPackage(String packageName) {
        this.currentPackage = packageName;
    }

    public Map<String, MethodInfo> getMethodComplexityMap() {
        return methodComplexityMap;
    }

    // Inner class to hold method information
    public static class MethodInfo {
        private String packageName;
        private String className;
        private String methodName;
        private String parameterTypes;
        private int complexity;

        public MethodInfo(String packageName, String className, String methodName, String parameterTypes, int complexity) {
            this.packageName = packageName;
            this.className = className;
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
            this.complexity = complexity;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getParameterTypes() {
            return parameterTypes;
        }

        public int getComplexity() {
            return complexity;
        }
    }
}
