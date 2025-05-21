// package com.project.management.springboot.backend.project_management.IA.controller;

// import com.project.management.springboot.backend.project_management.IA.DTO.AICardsDTO;
// import com.project.management.springboot.backend.project_management.IA.model.AICards;
// import com.project.management.springboot.backend.project_management.IA.service.AICardsService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;
// import java.util.stream.Collectors;

// @RestController
// @RequestMapping("/api/ai/cards")
// public class AICardsController {

//     @Autowired
//     private AICardsService aiCardsService;

//     @Value("${AI.cards.prompt.template}")
//     private String promptTemplate;

//     @PostMapping("/analyze")
//     public ResponseEntity<String> analyzeCards(@RequestBody AICardsDTO dto) {

//         List<AICards> cards = aiCardsService.getAICardsForCurrentUser(dto, 1.0);

//         String cardsText = cards.stream()
//                 .map(card -> {
//                     StringBuilder sb = new StringBuilder();
//                     sb.append("Tarea: ").append(card.getCardTitle())
//                       .append(" | Prioridad: ").append(card.getPriority())
//                       .append(" | Inicio: ").append(card.getStartDate())
//                       .append(" | Fin: ").append(card.getEndDate())
//                       .append(" | Miembros: ").append(card.getUsersCount())
//                       .append(" | Estado: ").append(card.getFinished() ? "Finalizada" : "Pendiente");
//                     if (card.getChecklists() != null && !card.getChecklists().isEmpty()) {
//                         sb.append(" | Apartados: ");
//                         card.getChecklists().forEach(checklist -> {
//                             sb.append(checklist.getTitle()).append(" [");
//                             if (checklist.getItems() != null) {
//                                 sb.append(
//                                     checklist.getItems().stream()
//                                         .map(item -> item.getTitle() + ":" + (item.getCompleted() ? "✔" : "✗"))
//                                         .collect(Collectors.joining(", "))
//                                 );
//                             }
//                             sb.append("] ");
//                         });
//                     }
//                     return sb.toString();
//                 })
//                 .collect(Collectors.joining("\n"));

//         String prompt = promptTemplate + ":\n" + cardsText;

//         String iaResponse = aiCardsService.sendToIAApi(cards.isEmpty() ? null : cards.get(0), prompt);

//         return ResponseEntity.ok(iaResponse);
//     }
// }
