package com.ai.testautomation.controller;

import com.ai.testautomation.model.TestCase;
import com.ai.testautomation.service.OpenAIService;
import com.ai.testautomation.service.ScriptGeneratorService;
import com.ai.testautomation.service.TestExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private ScriptGeneratorService scriptGeneratorService;

    @Autowired
    private TestExecutorService testExecutorService;

    @PostMapping("/generate-testcases")
    public ResponseEntity<List<TestCase>> generateTestcases(@RequestBody(required = false) String input) {
        List<TestCase> cases = openAIService.generateTestCases(input);
        return ResponseEntity.ok(cases);
    }

    @PostMapping("/generate-script")
    public ResponseEntity<String> generateScript(@RequestBody TestCase testCase) {
        String path = scriptGeneratorService.generateJavaTest(testCase);
        return ResponseEntity.ok(path);
    }

    @PostMapping("/run-tests")
    public ResponseEntity<String> runTests(@RequestBody List<TestCase> testCases) {
        String report = testExecutorService.runTestCases(testCases);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/report")
    public ResponseEntity<String> getLatestReport() {
        String path = testExecutorService.getLatestReportPath();
        return ResponseEntity.ok(path);
    }
}
