package com.project.management.springboot.backend.project_management.entities.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "card")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @NotBlank
    private String cardTitle;

    private String description;

    @Column(name = "start_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    private Date start_date;

    @Column(name = "end_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    private Date end_date;

    @Min(1)
    @Max(3)
    @NotNull
    private Long priority;

    @Column(nullable = true)
    private Integer position;

    @Column(name = "attached_links")
    private String attachedLinks;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean finished = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    @JsonIgnoreProperties({ "cards", "handler", "hibernateLazyInitializer" })
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    @JsonIgnoreProperties({ "cards", "handler", "hibernateLazyInitializer" })
    private Status status;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({ "card", "handler", "hibernateLazyInitializer" })
    private List<ChecklistItem> checklistItems = new ArrayList<>();

    @JsonIgnoreProperties({ "users", "handler", "hibernateLazyInitializer" })
    @ManyToMany
    @JoinTable(name = "user_card", joinColumns = @JoinColumn(name = "card_id"), inverseJoinColumns = @JoinColumn(name = "user_id"), uniqueConstraints = @UniqueConstraint(columnNames = {
            "card_id", "user_id" }))
    private List<User> users = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_id", nullable = true)
    @JsonIgnoreProperties({ "cards", "handler", "hibernateLazyInitializer" })
    private Label label;

    public Card() {
        users = new ArrayList<>();
    }

    public Card(@NotBlank String cardTitle, String description, @NotNull Date start_date,
            @NotNull Date end_date, @Min(1) @Max(3) @NotNull Long priority,
            Integer position, Board board, Status status, Boolean finished) {
        this.cardTitle = cardTitle;
        this.description = description;
        this.start_date = start_date;
        this.end_date = end_date;
        this.priority = priority;
        this.position = position;
        this.board = board;
        this.status = status;
        this.finished = finished != null ? finished : false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardTitle() {
        return cardTitle;
    }

    public void setCardTitle(String cardTitle) {
        this.cardTitle = cardTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStart_date() {
        return start_date;
    }

    public void setStart_date(Date start_date) {
        this.start_date = start_date;
    }

    public Date getEnd_date() {
        return end_date;
    }

    public void setEnd_date(Date end_date) {
        this.end_date = end_date;
    }

    public Long getPriority() {
        return priority;
    }

    public void setPriority(Long priority) {
        this.priority = priority;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getAttachedLinks() {
        return attachedLinks;
    }

    public void setAttachedLinks(String attachedLinks) {
        this.attachedLinks = attachedLinks;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<ChecklistItem> getChecklistItems() {
        return checklistItems;
    }

    public void setChecklistItems(List<ChecklistItem> checklistItems) {
        this.checklistItems = checklistItems;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }
}
