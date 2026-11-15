package com.civicbuild.dto.request;

import java.util.HashMap;
import java.util.Map;

public class TaskCompleteRequest {
    private Map<String, Object> variables = new HashMap<>();

    public TaskCompleteRequest() {}

    public TaskCompleteRequest(Map<String, Object> variables) {
        this.variables = variables;
    }

    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }
}
