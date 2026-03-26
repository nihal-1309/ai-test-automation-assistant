package com.ai.testautomation.service;

import com.ai.testautomation.model.TestCase;
import com.ai.testautomation.model.TestStep;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.testng.TestNG;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TestExecutorService {

    @Autowired
    private ScriptGeneratorService scriptGeneratorService;

    private String lastReportPath = "";

    public String runTestCases(List<TestCase> testCases) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File reportsDir = new File("reports");
            reportsDir.mkdirs();
            String reportFile = "reports/report_" + timestamp + ".html";

            ExtentSparkReporter html = new ExtentSparkReporter(reportFile);
            ExtentReports reports = new ExtentReports();
            reports.attachReporter(html);

            // Generate, compile and run each TestCase as TestNG class
            List<String> classNames = new ArrayList<>();
            for (TestCase tc : testCases) {
                ExtentTest t = reports.createTest(tc.getId() + " - " + tc.getName());
                String javaPath = scriptGeneratorService.generateJavaTest(tc);
                t.info("Generated source: " + javaPath);
                // compile
                boolean ok = compileJava(javaPath, t);
                if (!ok) { t.fail("Compilation failed for " + javaPath); continue; }
                String className = Path.of(javaPath).getFileName().toString().replaceFirst("\\.java$", "");
                classNames.add(className);
                t.pass("Compiled: " + className);
            }

            // Run TestNG on compiled classes
            if (!classNames.isEmpty()) {
                runTestNG(classNames, reports);
            }

            reports.flush();
            lastReportPath = new File(reportFile).getAbsolutePath();
            return lastReportPath;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean compileJava(String sourcePath, ExtentTest t) {
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) { t.fail("No system Java compiler available"); return false; }
            try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
                Iterable compilationUnits = fileManager.getJavaFileObjectsFromStrings(List.of(sourcePath));
                JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, List.of("-d","generated-classes"), null, compilationUnits);
                return task.call();
            }
        } catch (Exception e) {
            t.fail("Compilation error: " + e.getMessage());
            return false;
        }
    }

    private void runTestNG(List<String> classNames, ExtentReports reports) throws Exception {
        // load classes from generated-classes
        File classesDir = new File("generated-classes");
        URLClassLoader loader = new URLClassLoader(new URL[]{ classesDir.toURI().toURL() }, this.getClass().getClassLoader());
        TestNG testng = new TestNG();
        List<Class> clsList = new ArrayList<>();
        for (String cn : classNames) {
            try {
                Class cls = Class.forName(cn, true, loader);
                clsList.add(cls);
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }
        if (clsList.isEmpty()) return;
        testng.setTestClasses(clsList.toArray(new Class[0]));
        testng.run();
    }

    public String getLatestReportPath() { return lastReportPath; }
}
