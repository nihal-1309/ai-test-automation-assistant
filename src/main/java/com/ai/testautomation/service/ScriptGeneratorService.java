package com.ai.testautomation.service;

import com.ai.testautomation.model.TestCase;
import com.ai.testautomation.model.TestStep;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ScriptGeneratorService {

    public String generateJavaTest(TestCase testCase) {
        try {
            Path dir = Paths.get("generated-scripts");
            Files.createDirectories(dir);
            String className = sanitizeName(testCase.getName());
            Path file = dir.resolve(className + ".java");
            try (FileWriter w = new FileWriter(file.toFile())) {
                w.write(buildClassSource(className, testCase));
            }
            return file.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String sanitizeName(String name) {
        if (name == null || name.isBlank()) return "GeneratedTest" + System.currentTimeMillis();
        return name.replaceAll("[^A-Za-z0-9]", "_").replaceAll("_+", "_");
    }

    private String buildClassSource(String className, TestCase tc) {
        StringBuilder sb = new StringBuilder();
        sb.append("import org.openqa.selenium.WebDriver;\n");
        sb.append("import org.openqa.selenium.chrome.ChromeDriver;\n");
        sb.append("import io.github.bonigarcia.wdm.WebDriverManager;\n");
        sb.append("import org.testng.annotations.*;\n");
        sb.append("import java.time.Duration;\n");
        sb.append("public class ").append(className).append(" {\n");
        sb.append("  private WebDriver driver;\n");
        sb.append("  @BeforeClass\n  public void setup() {\n    WebDriverManager.chromedriver().setup();\n    driver = new ChromeDriver();\n    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));\n  }\n");
        sb.append("  @Test\n  public void test() throws Exception {\n");
            for (TestStep s : tc.getSteps()) {
            String action = s.getAction();
            String sel = s.getSelector();
                String val = s.getValue();
                if ("navigate".equalsIgnoreCase(action)) {
                    sb.append("    driver.get(\"").append(escapeForJava(val)).append("\");\n");
                } else if ("assertTitleContains".equalsIgnoreCase(action)) {
                    sb.append("    if (!driver.getTitle().contains(\"").append(escapeForJava(val)).append("\")) throw new AssertionError(\"Title does not contain: ").append(escapeForJava(val)).append("\");\n");
            } else {
                // generic comment
                sb.append("    // step: ").append(action).append(" selector:").append(sel).append(" value:").append(val).append("\n");
            }
        }
        sb.append("  }\n");
        sb.append("  @AfterClass\n  public void teardown() { if (driver!=null) driver.quit(); }\n");
        sb.append("}\n");
        return sb.toString();
    }

        private String escapeForJava(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\").replace("\"", "\\\"");
        }
}
