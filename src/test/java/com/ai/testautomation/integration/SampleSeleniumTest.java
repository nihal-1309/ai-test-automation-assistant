package com.ai.testautomation.integration;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SampleSeleniumTest {

    @Test
    public void openExampleDotCom() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver(opts);
        try {
            driver.get("https://example.com");
            String title = driver.getTitle();
            Assert.assertTrue(title.contains("Example"));
        } finally {
            driver.quit();
        }
    }
}
