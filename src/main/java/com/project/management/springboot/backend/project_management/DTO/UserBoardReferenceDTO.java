package com.project.management.springboot.backend.project_management.DTO;

public class UserBoardReferenceDTO {
    private Integer posX;
    private Integer posY;
    private Boolean isAdmin;

    public UserBoardReferenceDTO() {
    }

    public UserBoardReferenceDTO(Integer posX, Integer posY, Boolean isAdmin) {
        this.posX = posX;
        this.posY = posY;
        this.isAdmin = isAdmin;
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
