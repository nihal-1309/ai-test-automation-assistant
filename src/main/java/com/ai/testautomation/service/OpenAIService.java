package com.ai.testautomation.service;

import com.ai.testautomation.model.TestCase;
import com.ai.testautomation.model.TestStep;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class OpenAIService {

    @Value("${openai.api.url}")
    private String openaiUrl;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    private final Gson gson = new Gson();

    public List<TestCase> generateTestCases(String input) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            return sampleTestCases(input);
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            String prompt = buildPrompt(input);
                // Build Chat Completions v1 payload (messages array)
                String payload = gson.toJson(new ChatRequest(model, new Message("user", prompt)));

                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(openaiUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                String respBody = resp.body();
            // Response from /chat/completions contains a choices[].message.content field.
            try {
                ChatResponse chatResp = gson.fromJson(respBody, ChatResponse.class);
                if (chatResp != null && chatResp.choices!=null && chatResp.choices.length>0) {
                    String content = chatResp.choices[0].message.content;
                    // Try to parse JSON directly from content
                    Type listType = new TypeToken<List<TestCase>>(){}.getType();
                    try { List<TestCase> cases = gson.fromJson(content, listType); if (cases!=null && !cases.isEmpty()) return cases; } catch (Exception ignored) {}
                    // Fallback: attempt to extract JSON substring
                    int start = content.indexOf('[');
                    int end = content.lastIndexOf(']');
                    if (start>=0 && end>start) {
                        String json = content.substring(start, end+1);
                        try { List<TestCase> cases = gson.fromJson(json, listType); if (cases!=null && !cases.isEmpty()) return cases; } catch (Exception ignored) {}
                    }
                }
            } catch (Exception ignored) {}
            return sampleTestCases(input);
        } catch (IOException | InterruptedException e) {
            return sampleTestCases(input);
        }
    }

    private String buildPrompt(String input) {
        if (input == null || input.isBlank()) {
            return "Generate a single simple web UI test case describing steps and expected results for a sample login page as JSON array of TestCase objects.";
        }
        return "Generate structured test cases (JSON array) for this input: " + input + ". Each TestCase should include id,name,description,steps where each step has action,selector,value";
    }

    private List<TestCase> sampleTestCases(String input) {
        List<TestCase> out = new ArrayList<>();
        TestCase tc = new TestCase();
        tc.setId("TC-1");
        tc.setName("Open page and check title");
        tc.setDescription("Navigate to provided URL and verify page title contains expected text");
        List<TestStep> steps = new ArrayList<>();
        steps.add(new TestStep("navigate", "", input == null || input.isBlank() ? "https://example.com" : input));
        steps.add(new TestStep("assertTitleContains", "", "Example Domain"));
        tc.setSteps(steps);
        out.add(tc);
        return out;
    }

    static class OpenAIRequest {
        String model;
        String prompt;
        OpenAIRequest(String model, String prompt) { this.model = model; this.prompt = prompt; }
    }
    static class ChatRequest { String model; Message[] messages; ChatRequest(String model, Message m){ this.model=model; this.messages=new Message[]{m}; }}
    static class Message { String role; String content; Message(String role,String content){this.role=role;this.content=content;} }
    static class ChatResponse { Choice[] choices; }
    static class Choice { Message message; }
}
