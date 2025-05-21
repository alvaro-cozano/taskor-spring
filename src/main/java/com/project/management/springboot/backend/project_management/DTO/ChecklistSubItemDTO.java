package com.project.management.springboot.backend.project_management.DTO;

public class ChecklistSubItemDTO {

    private Long id;
    private String content;
    private Boolean done;
    private Long checklistItemId;

    public ChecklistSubItemDTO() {
    }

    public ChecklistSubItemDTO(Long id, String content, Boolean done, Long checklistItemId) {
        this.id = id;
        this.content = content;
        this.done = done;
        this.checklistItemId = checklistItemId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getDone() {
        return done;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }

    public Long getChecklistItemId() {
        return checklistItemId;
    }

    public void setChecklistItemId(Long checklistItemId) {
        this.checklistItemId = checklistItemId;
    }
}
