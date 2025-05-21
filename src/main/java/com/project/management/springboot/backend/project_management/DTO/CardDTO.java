package com.project.management.springboot.backend.project_management.DTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CardDTO {

    private Long id;

    @NotEmpty
    private String cardTitle;

    private String description;

    @Column(name = "start_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    private Date startDate;

    @Column(name = "end_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    private Date endDate;

    @NotNull
    private Long priority;

    private Integer position;

    private String attachedLinks;

    private Boolean finished;

    private Long board_id;
    private Long status_id;

    private List<UserReferenceDTO> users;
    private List<ChecklistItemDTO> checklistItems;

    private LabelDTO label;

    public CardDTO() {
        this.users = new ArrayList<>();
        this.checklistItems = new ArrayList<>();
        this.finished = false;
    }

    public CardDTO(Long id, String cardTitle, String description, Date startDate, Date endDate,
            Long priority, Integer position, String attachedLinks, Long board_id, Long status_id,
            List<UserReferenceDTO> users, List<ChecklistItemDTO> checklistItems, LabelDTO label, Boolean finished) {
        this.id = id;
        this.cardTitle = cardTitle;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.priority = priority;
        this.position = position;
        this.attachedLinks = attachedLinks;
        this.board_id = board_id;
        this.status_id = status_id;
        this.users = users != null ? users : new ArrayList<>();
        this.checklistItems = checklistItems != null ? checklistItems : new ArrayList<>();
        this.label = label;
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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

    public Long getBoard_id() {
        return board_id;
    }

    public void setBoard_id(Long board_id) {
        this.board_id = board_id;
    }

    public Long getStatus_id() {
        return status_id;
    }

    public void setStatus_id(Long status_id) {
        this.status_id = status_id;
    }

    public List<UserReferenceDTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserReferenceDTO> users) {
        this.users = users;
    }

    public List<ChecklistItemDTO> getChecklistItems() {
        return checklistItems;
    }

    public void setChecklistItems(List<ChecklistItemDTO> checklistItems) {
        this.checklistItems = checklistItems;
    }

    public LabelDTO getLabel() {
        return label;
    }

    public void setLabel(LabelDTO label) {
        this.label = label;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }
}
