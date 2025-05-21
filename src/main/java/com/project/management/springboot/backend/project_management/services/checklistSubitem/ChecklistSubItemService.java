package com.project.management.springboot.backend.project_management.services.checklistSubitem;

import com.project.management.springboot.backend.project_management.DTO.ChecklistSubItemDTO;

import java.util.List;

public interface ChecklistSubItemService {
    ChecklistSubItemDTO create(Long checklistItemId, ChecklistSubItemDTO dto);

    ChecklistSubItemDTO update(Long subItemId, ChecklistSubItemDTO dto);

    void delete(Long subItemId);

    List<ChecklistSubItemDTO> getAllByChecklistItemId(Long checklistItemId);
}
