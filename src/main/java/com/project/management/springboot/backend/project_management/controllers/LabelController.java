package com.project.management.springboot.backend.project_management.controllers;

import com.project.management.springboot.backend.project_management.DTO.LabelDTO;
import com.project.management.springboot.backend.project_management.services.label.LabelService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173", originPatterns = "*")
@RequestMapping("/labels")
public class LabelController {

    @Autowired
    private LabelService labelService;

    @PostMapping("/{boardId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<LabelDTO> createLabel(@PathVariable Long boardId, @RequestBody LabelDTO labelDTO) {
        return ResponseEntity.ok(labelService.createLabel(boardId, labelDTO));
    }

    @DeleteMapping("/{labelId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<Void> deleteLabel(@PathVariable Long labelId) {
        labelService.deleteLabel(labelId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{labelId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<LabelDTO> updateLabel(@PathVariable Long labelId, @RequestBody LabelDTO labelDTO) {
        return ResponseEntity.ok(labelService.updateLabel(labelId, labelDTO));
    }

    @GetMapping("/{boardId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<List<LabelDTO>> getLabelsByBoardId(@PathVariable Long boardId) {
        return ResponseEntity.ok(labelService.getLabelsByBoardId(boardId));
    }
}
