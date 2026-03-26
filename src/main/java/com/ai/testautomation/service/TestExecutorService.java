package com.ai.testautomation.service;

import com.ai.testautomation.model.TestCase;
import com.ai.testautomation.model.TestStep;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TestExecutorService {

    private String lastReportPath = "";

    public String runTestCases(List<TestCase> testCases) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File reportsDir = new File("reports");
            reportsDir.mkdirs();
            String reportFile = "reports/report_" + timestamp + ".html";

            ExtentHtmlReporter html = new ExtentHtmlReporter(reportFile);
            ExtentReports reports = new ExtentReports();
            reports.attachReporter(html);

            for (TestCase tc : testCases) {
                ExtentTest t = reports.createTest(tc.getId() + " - " + tc.getName());
                WebDriverManager.chromedriver().setup();
                ChromeOptions opts = new ChromeOptions();
                opts.addArguments("--headless=new");
                WebDriver driver = new ChromeDriver(opts);
                try {
                    for (TestStep s : tc.getSteps()) {
                        executeStep(driver, s, t);
                    }
                    t.pass("Test case executed");
                } catch (AssertionError ae) {
                    t.fail("Assertion failed: " + ae.getMessage());
                } catch (Exception e) {
                    t.fail("Error: " + e.getMessage());
                } finally {
                    driver.quit();
                }
            }

            reports.flush();
            lastReportPath = new File(reportFile).getAbsolutePath();
            return lastReportPath;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void executeStep(WebDriver driver, TestStep s, com.aventstack.extentreports.ExtentTest t) {
        String action = s.getAction();
        String sel = s.getSelector();
        String val = s.getValue();
        switch (action.toLowerCase()) {
            case "navigate":
                driver.get(val);
                t.info("Navigated to " + val);
                break;
            case "click":
                driver.findElement(By.cssSelector(sel)).click();
                t.info("Clicked " + sel);
                break;
            case "entertext":
                driver.findElement(By.cssSelector(sel)).sendKeys(val);
                t.info("Entered text into " + sel);
                break;
            case "asserttitlecontains":
                String title = driver.getTitle();
                if (!title.contains(val)) throw new AssertionError("Title did not contain: " + val + ", actual=" + title);
                t.info("Title contains: " + val);
                break;
            default:
                t.info("Unsupported step action: " + action);
        }
    }

    public String getLatestReportPath() { return lastReportPath; }
}
