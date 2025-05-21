package com.project.management.springboot.backend.project_management.utils.mapper;

import com.project.management.springboot.backend.project_management.DTO.LabelDTO;
import com.project.management.springboot.backend.project_management.entities.models.Board;
import com.project.management.springboot.backend.project_management.entities.models.Label;

public class LabelMapper {

    public static LabelDTO toDTO(Label label) {
        if (label == null) {
            return null;
        }

        return new LabelDTO(
                label.getId(),
                label.getTitle(),
                label.getColor(),
                label.getBoard() != null ? label.getBoard().getId() : null);
    }

    public static Label toEntity(LabelDTO dto) {
        if (dto == null) {
            return null;
        }

        Label label = new Label();
        label.setId(dto.getId());
        label.setTitle(dto.getTitle());
        label.setColor(dto.getColor());

        if (dto.getBoardId() != null) {
            Board board = new Board();
            board.setId(dto.getBoardId());
            label.setBoard(board);
        }

        return label;
    }
}
