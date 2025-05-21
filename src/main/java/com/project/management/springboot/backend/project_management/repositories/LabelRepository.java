package com.project.management.springboot.backend.project_management.repositories;

import com.project.management.springboot.backend.project_management.entities.models.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabelRepository extends JpaRepository<Label, Long> {
    public List<Label> findByBoard_Id(Long boardId);
}