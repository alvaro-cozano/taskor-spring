package com.project.management.springboot.backend.project_management.services.label;

import com.project.management.springboot.backend.project_management.DTO.LabelDTO;

import java.util.List;

public interface LabelService {

    LabelDTO createLabel(Long board_id, LabelDTO labelDTO);

    void deleteLabel(Long labelId);

    LabelDTO updateLabel(Long labelId, LabelDTO labelDTO);

    List<LabelDTO> getLabelsByBoardId(Long board_id);
}