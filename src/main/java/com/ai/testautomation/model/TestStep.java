package com.ai.testautomation.model;

public class TestStep {
    private String action; // click, enterText, navigate, assertTitle, wait
    private String selector; // css/xpath
    private String value; // text or expected

    public TestStep() {}

    public TestStep(String action, String selector, String value) {
        this.action = action;
        this.selector = selector;
        this.value = value;
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getSelector() { return selector; }
    public void setSelector(String selector) { this.selector = selector; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
