// package com.project.management.springboot.backend.project_management.IA.mapper;

// import com.project.management.springboot.backend.project_management.IA.DTO.AICardsDTO;
// import com.project.management.springboot.backend.project_management.IA.model.AICards;
// import com.project.management.springboot.backend.project_management.entities.models.Card;
// import com.project.management.springboot.backend.project_management.entities.models.Board;
// import com.project.management.springboot.backend.project_management.DTO.LabelDTO;
// import com.project.management.springboot.backend.project_management.entities.models.ChecklistItem;

// import java.text.SimpleDateFormat;
// import java.util.List;
// import java.util.TimeZone;
// import java.util.stream.Collectors;

// public class AICardsMapper {

//     public static AICards toAICards(Card card, Board board, List<LabelDTO> labels, AICardsDTO dto, Double temperature) {
//         AICards aiCards = new AICards();
//         aiCards.setId(card.getId());
//         aiCards.setCardTitle(card.getCardTitle());
//         aiCards.setBoardName(board.getBoardName());
//         aiCards.setStartDate(formatDate(card.getStart_date()));
//         aiCards.setEndDate(formatDate(card.getEnd_date()));
//         aiCards.setPriority(card.getPriority() != null ? card.getPriority().intValue() : null);
//         aiCards.setFinished(card.getFinished());

//         String labelName = null;
//         if (card.getLabel() != null && card.getLabel().getId() != null && labels != null) {
//             labelName = labels.stream()
//                     .filter(l -> l.getId().equals(card.getLabel().getId()))
//                     .map(LabelDTO::getTitle)
//                     .findFirst()
//                     .orElse(null);
//         }
//         aiCards.setLabel(labelName);

//         aiCards.setUsersCount(card.getUsers() != null ? card.getUsers().size() : 0);
//         aiCards.setChecklists(card.getChecklistItems() != null
//                 ? card.getChecklistItems().stream()
//                     .collect(Collectors.groupingBy(ChecklistItem::getTitle))
//                     .entrySet().stream()
//                     .map(entry -> {
//                         AICards.Checklist checklist = new AICards.Checklist();
//                         checklist.setTitle(entry.getKey());
//                         checklist.setItems(entry.getValue().stream().map(item -> {
//                             AICards.ChecklistItem checklistItem = new AICards.ChecklistItem();
//                             checklistItem.setTitle(item.getTitle());
//                             checklistItem.setCompleted(item.getCompleted());
//                             return checklistItem;
//                         }).collect(Collectors.toList()));
//                         return checklist;
//                     }).collect(Collectors.toList())
//                 : null
//         );
//         aiCards.setTemperature(1.0);
//         aiCards.setLanguage(dto.getLanguage());
//         return aiCards;
//     }

//     private static String formatDate(java.util.Date date) {
//         if (date == null) return null;
//         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//         sdf.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
//         return sdf.format(date);
//     }
// }
