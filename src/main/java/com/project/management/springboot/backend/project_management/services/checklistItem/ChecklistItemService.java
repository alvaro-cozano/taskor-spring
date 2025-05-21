package com.project.management.springboot.backend.project_management.services.checklistItem;

import com.project.management.springboot.backend.project_management.DTO.ChecklistItemDTO;

import java.util.List;

public interface ChecklistItemService {

    ChecklistItemDTO create(Long cardId, ChecklistItemDTO dto);

    ChecklistItemDTO update(Long checklistItemId, ChecklistItemDTO dto);

    void delete(Long checklistItemId);

    List<ChecklistItemDTO> getAllByCardId(Long cardId);
}
