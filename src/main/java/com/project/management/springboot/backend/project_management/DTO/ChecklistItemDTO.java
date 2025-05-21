package com.project.management.springboot.backend.project_management.DTO;

import java.util.ArrayList;
import java.util.List;

public class ChecklistItemDTO {

    private Long id;
    private String title;
    private Boolean completed;
    private Long cardId;
    private List<ChecklistSubItemDTO> subItems = new ArrayList<>();

    public ChecklistItemDTO() {
    }

    public ChecklistItemDTO(Long id, String title, Boolean completed, Long cardId, List<ChecklistSubItemDTO> subItems) {
        this.id = id;
        this.title = title;
        this.completed = completed;
        this.cardId = cardId;
        this.subItems = subItems;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public List<ChecklistSubItemDTO> getSubItems() {
        return subItems;
    }

    public void setSubItems(List<ChecklistSubItemDTO> subItems) {
        this.subItems = subItems;
    }
}
