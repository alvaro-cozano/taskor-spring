package com.project.management.springboot.backend.project_management.utils.mapper;

import com.project.management.springboot.backend.project_management.DTO.ChecklistItemDTO;
import com.project.management.springboot.backend.project_management.DTO.ChecklistSubItemDTO;
import com.project.management.springboot.backend.project_management.entities.models.ChecklistItem;

import java.util.List;
import java.util.stream.Collectors;

public class ChecklistItemMapper {

    public static ChecklistItemDTO toDTO(ChecklistItem checklistItem) {
        if (checklistItem == null) {
            return null;
        }

        ChecklistItemDTO dto = new ChecklistItemDTO();
        dto.setId(checklistItem.getId());
        dto.setTitle(checklistItem.getTitle());
        dto.setCompleted(checklistItem.getCompleted());

        if (checklistItem.getCard() != null) {
            dto.setCardId(checklistItem.getCard().getId());
        }

        if (checklistItem.getSubItems() != null && !checklistItem.getSubItems().isEmpty()) {
            List<ChecklistSubItemDTO> subItemDTOs = checklistItem.getSubItems().stream()
                    .map(ChecklistSubItemMapper::toDTO)
                    .collect(Collectors.toList());
            dto.setSubItems(subItemDTOs);
        }

        return dto;
    }

    public static ChecklistItem toEntity(ChecklistItemDTO dto) {
        if (dto == null) {
            return null;
        }

        ChecklistItem checklistItem = new ChecklistItem();
        checklistItem.setId(dto.getId());
        checklistItem.setTitle(dto.getTitle());
        checklistItem.setCompleted(dto.getCompleted() != null ? dto.getCompleted() : false);

        return checklistItem;
    }
}