package com.project.management.springboot.backend.project_management.DTO;

public class LabelDTO {

    private Long id;
    private String title;
    private String color;
    private Long boardId;

    public LabelDTO() {
    }

    public LabelDTO(Long id, String title, String color, Long boardId) {
        this.id = id;
        this.title = title;
        this.color = color;
        this.boardId = boardId;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }
}
