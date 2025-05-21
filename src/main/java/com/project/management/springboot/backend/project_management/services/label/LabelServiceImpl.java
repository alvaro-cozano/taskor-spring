package com.project.management.springboot.backend.project_management.services.label;

import com.project.management.springboot.backend.project_management.DTO.LabelDTO;
import com.project.management.springboot.backend.project_management.entities.models.Board;
import com.project.management.springboot.backend.project_management.entities.models.Label;
import com.project.management.springboot.backend.project_management.repositories.BoardRepository;
import com.project.management.springboot.backend.project_management.repositories.LabelRepository;
import com.project.management.springboot.backend.project_management.utils.mapper.LabelMapper;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LabelServiceImpl implements LabelService {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Override
    public LabelDTO createLabel(Long boardId, LabelDTO labelDTO) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("Board with ID " + boardId + " not found"));

        Label label = LabelMapper.toEntity(labelDTO);
        label.setBoard(board);

        return LabelMapper.toDTO(labelRepository.save(label));
    }

    @Override
    public void deleteLabel(Long labelId) {
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new EntityNotFoundException("Label with ID " + labelId + " not found"));

        labelRepository.delete(label);
    }

    @Override
    public LabelDTO updateLabel(Long labelId, LabelDTO labelDTO) {
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new EntityNotFoundException("Label with ID " + labelId + " not found"));

        label.setTitle(labelDTO.getTitle());
        label.setColor(labelDTO.getColor());

        return LabelMapper.toDTO(labelRepository.save(label));
    }

    @Override
    public List<LabelDTO> getLabelsByBoardId(Long boardId) {
        return labelRepository.findByBoard_Id(boardId).stream()
                .map(LabelMapper::toDTO)
                .collect(Collectors.toList());
    }
}
