package com.ai.testautomation.service;

import com.ai.testautomation.model.TestCase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class OpenAIParser {
    private static final Gson gson = new Gson();

    public static List<TestCase> parseTestCases(String assistantContent) {
        if (assistantContent == null) return null;
        Type listType = new TypeToken<List<TestCase>>(){}.getType();
        try {
            List<TestCase> cases = gson.fromJson(assistantContent, listType);
            if (cases != null && !cases.isEmpty()) return cases;
        } catch (Exception ignored) {}

        int start = assistantContent.indexOf('[');
        int end = assistantContent.lastIndexOf(']');
        if (start>=0 && end>start) {
            String json = assistantContent.substring(start, end+1);
            try { return gson.fromJson(json, listType); } catch (Exception ignored) {}
        }
        return null;
    }
}
