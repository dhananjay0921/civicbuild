package com.civicbuild.controller;

import com.civicbuild.dto.request.TaskCompleteRequest;
import com.civicbuild.dto.response.ApiResponse;
import com.civicbuild.dto.response.TaskItemResponse;
import com.civicbuild.entity.User;
import com.civicbuild.service.AuthService;
import com.civicbuild.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AuthService authService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskItemResponse>>> getMyTasks() {
        User currentUser = authService.getCurrentUser();
        List<TaskItemResponse> tasks = taskService.getMyTasks(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", tasks));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskItemResponse>> getTaskById(@PathVariable String taskId) {
        User currentUser = authService.getCurrentUser();
        TaskItemResponse task = taskService.getTaskById(taskId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Task details retrieved", task));
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<ApiResponse<String>> completeTask(
            @PathVariable String taskId,
            @RequestBody(required = false) TaskCompleteRequest request) {
        User currentUser = authService.getCurrentUser();
        var variables = request != null && request.getVariables() != null ? request.getVariables() : java.util.Collections.<String, Object>emptyMap();
        taskService.completeTask(taskId, variables, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Task completed successfully", "DONE"));
    }
}
