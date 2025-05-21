package com.project.management.springboot.backend.project_management.services.checklistItem;

import com.project.management.springboot.backend.project_management.DTO.ChecklistItemDTO;
import com.project.management.springboot.backend.project_management.DTO.ChecklistSubItemDTO;
import com.project.management.springboot.backend.project_management.entities.models.Card;
import com.project.management.springboot.backend.project_management.entities.models.ChecklistItem;
import com.project.management.springboot.backend.project_management.entities.models.ChecklistSubItem;
import com.project.management.springboot.backend.project_management.repositories.CardRepository;
import com.project.management.springboot.backend.project_management.repositories.ChecklistItemRepository;
import com.project.management.springboot.backend.project_management.utils.mapper.ChecklistItemMapper;
import com.project.management.springboot.backend.project_management.utils.mapper.ChecklistSubItemMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ChecklistItemServiceImpl implements ChecklistItemService {

    private final ChecklistItemRepository checklistItemRepository;
    private final CardRepository cardRepository;

    public ChecklistItemServiceImpl(ChecklistItemRepository checklistItemRepository,
            CardRepository cardRepository) {
        this.checklistItemRepository = checklistItemRepository;
        this.cardRepository = cardRepository;
    }

    @Override
    @Transactional
    public ChecklistItemDTO create(Long cardId, ChecklistItemDTO dto) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NoSuchElementException("Card with ID " + cardId + " not found"));

        ChecklistItem checklistItem = ChecklistItemMapper.toEntity(dto);
        checklistItem.setCard(card);

        if (dto.getSubItems() != null && !dto.getSubItems().isEmpty()) {
            for (ChecklistSubItemDTO subItemDTO : dto.getSubItems()) {
                ChecklistSubItem subItem = ChecklistSubItemMapper.toEntity(subItemDTO);
                checklistItem.addSubItem(subItem);
            }
        }

        checklistItem = checklistItemRepository.save(checklistItem);
        return ChecklistItemMapper.toDTO(checklistItem);
    }

    @Override
    @Transactional
    public ChecklistItemDTO update(Long checklistItemId, ChecklistItemDTO dto) {
        ChecklistItem checklistItem = checklistItemRepository.findById(checklistItemId)
                .orElseThrow(() -> new NoSuchElementException("ChecklistItem not found with ID: " + checklistItemId));

        if (dto.getTitle() != null) {
            checklistItem.setTitle(dto.getTitle());
        }
        if (dto.getCompleted() != null) {
            checklistItem.setCompleted(dto.getCompleted());
        }

        if (dto.getSubItems() != null) {
            for (ChecklistSubItemDTO subItemDTO : dto.getSubItems()) {
                if (subItemDTO.getId() != null) {
                    checklistItem.getSubItems().stream()
                            .filter(si -> si.getId().equals(subItemDTO.getId()))
                            .findFirst()
                            .ifPresent(existingSubItem -> {
                                if (subItemDTO.getContent() != null) {
                                    existingSubItem.setContent(subItemDTO.getContent());
                                }
                                if (subItemDTO.getDone() != null) {
                                    existingSubItem.setDone(subItemDTO.getDone());
                                }
                            });
                } else {
                    ChecklistSubItem newSubItem = ChecklistSubItemMapper.toEntity(subItemDTO);
                    checklistItem.addSubItem(newSubItem);
                }
            }
        }

        checklistItem = checklistItemRepository.save(checklistItem);
        return ChecklistItemMapper.toDTO(checklistItem);
    }

    @Override
    @Transactional
    public void delete(Long checklistItemId) {
        ChecklistItem checklistItem = checklistItemRepository.findById(checklistItemId)
                .orElseThrow(() -> new NoSuchElementException("ChecklistItem not found with ID: " + checklistItemId));

        checklistItemRepository.delete(checklistItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChecklistItemDTO> getAllByCardId(Long cardId) {
        return checklistItemRepository.findByCard_Id(cardId).stream()
                .map(ChecklistItemMapper::toDTO)
                .collect(Collectors.toList());
    }
}