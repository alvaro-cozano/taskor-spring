package com.project.management.springboot.backend.project_management.utils.mapper;

import com.project.management.springboot.backend.project_management.DTO.CardDTO;
import com.project.management.springboot.backend.project_management.DTO.ChecklistItemDTO;
import com.project.management.springboot.backend.project_management.DTO.LabelDTO;
import com.project.management.springboot.backend.project_management.DTO.UserReferenceDTO;
import com.project.management.springboot.backend.project_management.entities.models.Board;
import com.project.management.springboot.backend.project_management.entities.models.Card;
import com.project.management.springboot.backend.project_management.entities.models.Label;
import com.project.management.springboot.backend.project_management.entities.models.Status;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class CardMapper {

    public static CardDTO toDTO(Card card) {
        if (card == null) {
            return null;
        }

        List<UserReferenceDTO> userDTOs = card.getUsers() != null
                ? card.getUsers().stream()
                        .map(user -> {
                            String base64Image = null;
                            if (user.getProfileImage() != null) {
                                base64Image = Base64.getEncoder().encodeToString(user.getProfileImage());
                            }
                            return new UserReferenceDTO(user.getEmail(), base64Image);
                        })
                        .collect(Collectors.toList())
                : null;

        List<ChecklistItemDTO> checklistItemDTOs = card.getChecklistItems() != null
                ? card.getChecklistItems().stream()
                        .map(ChecklistItemMapper::toDTO)
                        .collect(Collectors.toList())
                : null;

        LabelDTO labelDTO = card.getLabel() != null ? LabelMapper.toDTO(card.getLabel()) : null;

        return new CardDTO(
                card.getId(),
                card.getCardTitle(),
                card.getDescription(),
                card.getStart_date(),
                card.getEnd_date(),
                card.getPriority(),
                card.getPosition(),
                card.getAttachedLinks(),
                card.getBoard() != null ? card.getBoard().getId() : null,
                card.getStatus() != null ? card.getStatus().getId() : null,
                userDTOs,
                checklistItemDTOs,
                labelDTO,
                card.getFinished());
    }

    public static Card toEntity(CardDTO dto) {
        if (dto == null) {
            return null;
        }

        Card card = new Card(
                dto.getCardTitle(),
                dto.getDescription(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getPriority(),
                dto.getPosition(),
                null,
                null,
                dto.getFinished());
        card.setAttachedLinks(dto.getAttachedLinks());

        if (dto.getBoard_id() != null) {
            Board board = new Board();
            board.setId(dto.getBoard_id());
            card.setBoard(board);
        }

        if (dto.getStatus_id() != null) {
            Status status = new Status();
            status.setId(dto.getStatus_id());
            card.setStatus(status);
        }

        if (dto.getLabel() != null && dto.getLabel().getId() != null) {
            Label label = new Label();
            label.setId(dto.getLabel().getId());
            card.setLabel(label);
        }

        return card;
    }
}
