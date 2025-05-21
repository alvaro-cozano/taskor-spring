package com.project.management.springboot.backend.project_management.repositories;

import com.project.management.springboot.backend.project_management.entities.models.ChecklistSubItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChecklistSubItemRepository extends JpaRepository<ChecklistSubItem, Long> {
    List<ChecklistSubItem> findByChecklistItem_Id(Long checklistItemId);
}
