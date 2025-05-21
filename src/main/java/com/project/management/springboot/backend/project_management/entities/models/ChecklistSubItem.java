package com.project.management.springboot.backend.project_management.entities.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "checklist_sub_item")
public class ChecklistSubItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String content;

    @NotNull
    private Boolean done = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_item_id")
    @JsonIgnoreProperties({ "subItems", "handler", "hibernateLazyInitializer" })
    private ChecklistItem checklistItem;

    public ChecklistSubItem() {
    }

    public ChecklistSubItem(String content, Boolean done, ChecklistItem checklistItem) {
        this.content = content;
        this.done = done;
        this.checklistItem = checklistItem;
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

    public ChecklistItem getChecklistItem() {
        return checklistItem;
    }

    public void setChecklistItem(ChecklistItem checklistItem) {
        this.checklistItem = checklistItem;
    }
}
