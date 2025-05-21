package com.project.management.springboot.backend.project_management.entities.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "checklist_item")
public class ChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @NotNull
    private Boolean completed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    @JsonIgnoreProperties({ "checklist_items", "handler", "hibernateLazyInitializer" })
    private Card card;

    @OneToMany(mappedBy = "checklistItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({ "checklistItem", "handler", "hibernateLazyInitializer" })
    private List<ChecklistSubItem> subItems = new ArrayList<>();

    public ChecklistItem() {
    }

    public ChecklistItem(String title, Boolean completed, Card card) {
        this.title = title;
        this.completed = completed;
        this.card = card;
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

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public List<ChecklistSubItem> getSubItems() {
        return subItems;
    }

    public void setSubItems(List<ChecklistSubItem> subItems) {
        this.subItems = subItems;
    }

    public void addSubItem(ChecklistSubItem subItem) {
        subItems.add(subItem);
        subItem.setChecklistItem(this);
    }

    public void removeSubItem(ChecklistSubItem subItem) {
        subItems.remove(subItem);
        subItem.setChecklistItem(null);
    }
}
