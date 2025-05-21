package com.project.management.springboot.backend.project_management.controllers;

import com.project.management.springboot.backend.project_management.DTO.ChecklistSubItemDTO;
import com.project.management.springboot.backend.project_management.services.checklistSubitem.ChecklistSubItemService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/checklist-subitems")
@CrossOrigin(origins = "http://localhost:5173", originPatterns = "*")
public class ChecklistSubItemController {

    @Autowired
    private ChecklistSubItemService checklistSubItemService;

    @GetMapping("{checklistItemId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<List<ChecklistSubItemDTO>> getSubItemsByChecklistItem(@PathVariable Long checklistItemId) {
        List<ChecklistSubItemDTO> subItems = checklistSubItemService.getAllByChecklistItemId(checklistItemId);
        return ResponseEntity.ok(subItems);
    }

    @PostMapping("{checklistItemId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<?> createSubItem(@PathVariable Long checklistItemId,
            @Valid @RequestBody ChecklistSubItemDTO checklistSubItemDTO,
            BindingResult result) {
        if (result.hasFieldErrors()) {
            return validation(result);
        }

        ChecklistSubItemDTO createdSubItem = checklistSubItemService.create(checklistItemId, checklistSubItemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubItem);
    }

    @PutMapping("/{subItemId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<?> updateSubItem(@PathVariable Long subItemId,
            @Valid @RequestBody ChecklistSubItemDTO checklistSubItemDTO,
            BindingResult result) {
        if (result.hasFieldErrors()) {
            return validation(result);
        }

        ChecklistSubItemDTO updatedSubItem = checklistSubItemService.update(subItemId, checklistSubItemDTO);
        return ResponseEntity.ok(updatedSubItem);
    }

    @DeleteMapping("/{subItemId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<Void> deleteSubItem(@PathVariable Long subItemId) {
        checklistSubItemService.delete(subItemId);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> validation(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
