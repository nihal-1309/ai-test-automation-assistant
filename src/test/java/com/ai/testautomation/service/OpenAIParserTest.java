package com.ai.testautomation.service;

import com.ai.testautomation.model.TestCase;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OpenAIParserTest {

    @Test
    public void testParseSimpleArray() {
        String json = "[ { \"id\":\"TC1\", \"name\":\"t\", \"description\":\"d\", \"steps\": [ { \"action\":\"navigate\", \"selector\":\"\", \"value\":\"https://example.com\" } ] } ]";
        List<TestCase> cases = OpenAIParser.parseTestCases(json);
        Assert.assertNotNull(cases);
        Assert.assertEquals(cases.size(),1);
        TestCase tc = cases.get(0);
        Assert.assertEquals(tc.getId(),"TC1");
        Assert.assertEquals(tc.getSteps().get(0).getAction(),"navigate");
    }

    @Test
    public void testExtractFromText() {
        String content = "Here are testcases:\n```json\n[ { \"id\":\"TC2\", \"name\":\"t2\", \"steps\": [ { \"action\":\"navigate\", \"selector\":\"\", \"value\":\"https://x\" } ] } ]\n```";
        List<TestCase> cases = OpenAIParser.parseTestCases(content);
        Assert.assertNotNull(cases);
        Assert.assertEquals(cases.get(0).getId(),"TC2");
    }
}
