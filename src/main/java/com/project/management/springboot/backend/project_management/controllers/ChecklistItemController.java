package com.project.management.springboot.backend.project_management.controllers;

import com.project.management.springboot.backend.project_management.DTO.ChecklistItemDTO;
import com.project.management.springboot.backend.project_management.services.checklistItem.ChecklistItemService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/checklist-items")
@CrossOrigin(origins = "${app.front-url}", originPatterns = "*")
public class ChecklistItemController {

    @Autowired
    private ChecklistItemService checklistItemService;

    @GetMapping("/card/{cardId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<List<ChecklistItemDTO>> getChecklistItemsByCard(@PathVariable Long cardId) {
        List<ChecklistItemDTO> checklistItems = checklistItemService.getAllByCardId(cardId);
        return ResponseEntity.ok(checklistItems);
    }

    @PostMapping("/card/{cardId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<?> createChecklistItem(@PathVariable Long cardId,
            @Valid @RequestBody ChecklistItemDTO checklistItemDTO,
            BindingResult result) {

        if (result.hasFieldErrors()) {
            return validation(result);
        }

        ChecklistItemDTO createdChecklistItem = checklistItemService.create(cardId, checklistItemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdChecklistItem);
    }

    @PutMapping("/{checklistItemId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<?> updateChecklistItem(@PathVariable Long checklistItemId,
            @Valid @RequestBody ChecklistItemDTO checklistItemDTO,
            BindingResult result) {
        if (result.hasFieldErrors()) {
            return validation(result);
        }

        ChecklistItemDTO updatedChecklistItem = checklistItemService.update(checklistItemId, checklistItemDTO);
        return ResponseEntity.ok(updatedChecklistItem);
    }

    @DeleteMapping("/{checklistItemId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<Void> deleteChecklistItem(@PathVariable Long checklistItemId) {
        checklistItemService.delete(checklistItemId);
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
