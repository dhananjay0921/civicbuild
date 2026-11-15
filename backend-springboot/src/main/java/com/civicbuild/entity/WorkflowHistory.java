package com.civicbuild.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_histories")
public class WorkflowHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(nullable = false, length = 100)
    private String stage;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(nullable = false, length = 100)
    private String actor;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "process_instance_key", length = 100)
    private String processInstanceKey;

    public WorkflowHistory() {}

    public WorkflowHistory(Application application, String stage, String action, String actor, String comments, String processInstanceKey) {
        this.application = application;
        this.stage = stage;
        this.action = action;
        this.actor = actor;
        this.comments = comments;
        this.processInstanceKey = processInstanceKey;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getProcessInstanceKey() { return processInstanceKey; }
    public void setProcessInstanceKey(String processInstanceKey) { this.processInstanceKey = processInstanceKey; }
}
