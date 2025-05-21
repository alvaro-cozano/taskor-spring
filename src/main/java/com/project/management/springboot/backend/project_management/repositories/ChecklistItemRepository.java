package com.project.management.springboot.backend.project_management.repositories;

import com.project.management.springboot.backend.project_management.entities.models.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {
    List<ChecklistItem> findByCard_Id(Long cardId);
}
