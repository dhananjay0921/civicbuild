package com.civicbuild.service;

import com.civicbuild.dto.response.TaskItemResponse;
import com.civicbuild.entity.User;
import java.util.List;
import java.util.Map;

public interface TaskService {
    List<TaskItemResponse> getMyTasks(User user);
    TaskItemResponse getTaskById(String taskId, User user);
    void completeTask(String taskId, Map<String, Object> variables, User user);
}
