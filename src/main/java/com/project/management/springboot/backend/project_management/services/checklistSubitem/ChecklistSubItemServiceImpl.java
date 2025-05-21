package com.project.management.springboot.backend.project_management.services.checklistSubitem;

import com.project.management.springboot.backend.project_management.DTO.ChecklistSubItemDTO;
import com.project.management.springboot.backend.project_management.entities.models.ChecklistItem;
import com.project.management.springboot.backend.project_management.entities.models.ChecklistSubItem;
import com.project.management.springboot.backend.project_management.repositories.ChecklistItemRepository;
import com.project.management.springboot.backend.project_management.repositories.ChecklistSubItemRepository;
import com.project.management.springboot.backend.project_management.utils.mapper.ChecklistSubItemMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChecklistSubItemServiceImpl implements ChecklistSubItemService {

    @Autowired
    private ChecklistSubItemRepository subItemRepository;

    @Autowired
    private ChecklistItemRepository checklistItemRepository;

    @Override
    public ChecklistSubItemDTO create(Long checklistItemId, ChecklistSubItemDTO dto) {
        ChecklistItem checklistItem = checklistItemRepository.findById(checklistItemId)
                .orElseThrow(() -> new RuntimeException("ChecklistItem not found"));

        Boolean done = dto.getDone() != null ? dto.getDone() : false;

        ChecklistSubItem subItem = new ChecklistSubItem(dto.getContent(), done, checklistItem);
        subItem = subItemRepository.save(subItem);
        return ChecklistSubItemMapper.toDTO(subItem);
    }

    @Override
    public ChecklistSubItemDTO update(Long subItemId, ChecklistSubItemDTO dto) {
        ChecklistSubItem subItem = subItemRepository.findById(subItemId)
                .orElseThrow(() -> new RuntimeException("SubItem not found"));

        subItem.setContent(dto.getContent());
        subItem.setDone(dto.getDone());

        subItem = subItemRepository.save(subItem);
        return ChecklistSubItemMapper.toDTO(subItem);
    }

    @Override
    public void delete(Long subItemId) {
        ChecklistSubItem subItem = subItemRepository.findById(subItemId)
                .orElseThrow(() -> new RuntimeException("SubItem not found"));
        subItemRepository.delete(subItem);
    }

    @Override
    public List<ChecklistSubItemDTO> getAllByChecklistItemId(Long checklistItemId) {
        List<ChecklistSubItem> subItems = subItemRepository.findByChecklistItem_Id(checklistItemId);
        return subItems.stream().map(ChecklistSubItemMapper::toDTO).collect(Collectors.toList());
    }
}