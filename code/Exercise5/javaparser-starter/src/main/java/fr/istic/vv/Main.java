package fr.istic.vv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.statistics.HistogramDataset;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.utils.SourceRoot;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Should provide the path to the source code");
            System.exit(1);
        }

        File file = new File(args[0]);
        if (!file.exists() || !file.isDirectory() || !file.canRead()) {
            System.err.println("Provide a path to an existing readable directory");
            System.exit(2);
        }

        SourceRoot root = new SourceRoot(file.toPath());
        CyclomaticComplexityVisitor visitor = new CyclomaticComplexityVisitor();

        root.parse("", (localPath, absolutePath, result) -> {
            result.ifSuccessful(unit -> {
                // Capture the package name
                String packageName = unit.getPackageDeclaration().map(pd -> pd.getNameAsString()).orElse("");
                visitor.setCurrentPackage(packageName);

                // Visit methods
                for (MethodDeclaration method : unit.findAll(MethodDeclaration.class)) {
                    visitor.visit(method, null);
                }
            });
            return SourceRoot.Callback.Result.DONT_SAVE;
        });

        // Write report
        writeReport(visitor.getMethodComplexityMap(), "complexity_report.csv");

        // Generate histogram
        generateHistogram(visitor.getMethodComplexityMap(), "complexity_histogram.png");
    }

    // Method to write report
    private static void writeReport(Map<String, CyclomaticComplexityVisitor.MethodInfo> methodComplexityMap, String fileName) throws IOException {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("Package Name,Class Name,Method Name,Parameter Types,Cyclomatic Complexity\n");
            for (CyclomaticComplexityVisitor.MethodInfo info : methodComplexityMap.values()) {
                writer.write(String.format("%s,%s,%s,\"%s\",%d\n", 
                    info.getPackageName(), 
                    info.getClassName(), 
                    info.getMethodName(), 
                    info.getParameterTypes(), 
                    info.getComplexity()));
            }
        }
        System.out.println("Report generated: " + fileName);
    }

    // Method to generate a histogram 
    private static void generateHistogram(Map<String, CyclomaticComplexityVisitor.MethodInfo> methodComplexityMap, String fileName) throws IOException {
        HistogramDataset dataset = new HistogramDataset();
        double[] complexities = methodComplexityMap.values().stream().mapToDouble(CyclomaticComplexityVisitor.MethodInfo::getComplexity).toArray();
        dataset.addSeries("CC Values", complexities, complexities.length);

        JFreeChart histogram = ChartFactory.createHistogram(
                "Cyclomatic Complexity Distribution",
                "CC Value",
                "Frequency",
                dataset
        );

        // Save histogram
        ChartUtils.saveChartAsPNG(new File(fileName), histogram, 800, 600);
        System.out.println("Histogram generated: " + fileName);
    }
}
