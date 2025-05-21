package com.project.management.springboot.backend.project_management.utils.mapper;

import com.project.management.springboot.backend.project_management.DTO.StatusDTO;
import com.project.management.springboot.backend.project_management.entities.models.Board;
import com.project.management.springboot.backend.project_management.entities.models.Status;

import java.util.Date;

public class StatusMapper {

    public static StatusDTO toDTO(Status status) {
        if (status == null)
            return null;

        return new StatusDTO(
                status.getId(),
                status.getName(),
                status.getBoard() != null ? status.getBoard().getId() : null);
    }

    public static Status toEntity(StatusDTO dto) {
        if (dto == null)
            return null;

        Status status = new Status();
        status.setId(dto.getId());
        status.setName(dto.getName());

        if (dto.getBoardId() != null) {
            Board board = new Board();
            board.setId(dto.getBoardId());
            status.setBoard(board);
        }

        status.setCreatedAt(new Date());
        status.setUpdatedAt(new Date());

        return status;
    }
}
