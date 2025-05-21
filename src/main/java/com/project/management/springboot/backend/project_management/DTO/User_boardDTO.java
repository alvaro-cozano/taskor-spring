package com.project.management.springboot.backend.project_management.DTO;

public class User_boardDTO {

    private Long user_id;
    private Long board_id;
    private Integer posX;
    private Integer posY;
    private Boolean isAdmin;

    public User_boardDTO() {
    }

    public User_boardDTO(Long user_id, Long board_id, Integer posX, Integer posY, Boolean isAdmin) {
        this.user_id = user_id;
        this.board_id = board_id;
        this.posX = posX;
        this.posY = posY;
        this.isAdmin = isAdmin;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public Long getBoard_id() {
        return board_id;
    }

    public void setBoard_id(Long board_id) {
        this.board_id = board_id;
    }

    public Integer getPosX() {
        return posX;
    }

    public void setPosX(Integer posX) {
        this.posX = posX;
    }

    public Integer getPosY() {
        return posY;
    }

    public void setPosY(Integer posY) {
        this.posY = posY;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
