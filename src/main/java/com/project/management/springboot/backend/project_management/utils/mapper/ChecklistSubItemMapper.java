package com.project.management.springboot.backend.project_management.utils.mapper;

import com.project.management.springboot.backend.project_management.DTO.ChecklistSubItemDTO;
import com.project.management.springboot.backend.project_management.entities.models.ChecklistSubItem;

public class ChecklistSubItemMapper {

    public static ChecklistSubItemDTO toDTO(ChecklistSubItem subItem) {
        if (subItem == null) {
            return null;
        }

        ChecklistSubItemDTO dto = new ChecklistSubItemDTO();
        dto.setId(subItem.getId());
        dto.setContent(subItem.getContent());
        dto.setDone(subItem.getDone());

        if (subItem.getChecklistItem() != null) {
            dto.setChecklistItemId(subItem.getChecklistItem().getId());
        }

        return dto;
    }

    public static ChecklistSubItem toEntity(ChecklistSubItemDTO dto) {
        if (dto == null) {
            return null;
        }

        ChecklistSubItem subItem = new ChecklistSubItem();
        subItem.setId(dto.getId());
        subItem.setContent(dto.getContent());
        subItem.setDone(dto.getDone() != null ? dto.getDone() : false);

        return subItem;
    }
}