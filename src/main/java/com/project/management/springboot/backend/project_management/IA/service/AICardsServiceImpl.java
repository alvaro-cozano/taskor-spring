// package com.project.management.springboot.backend.project_management.IA.service;

// import com.project.management.springboot.backend.project_management.IA.DTO.AICardsDTO;
// import com.project.management.springboot.backend.project_management.IA.mapper.AICardsMapper;
// import com.project.management.springboot.backend.project_management.IA.model.AICards;
// import com.project.management.springboot.backend.project_management.DTO.LabelDTO;
// import com.project.management.springboot.backend.project_management.entities.models.Board;
// import com.project.management.springboot.backend.project_management.entities.models.Card;
// import com.project.management.springboot.backend.project_management.entities.models.User;
// import com.project.management.springboot.backend.project_management.repositories.CardRepository;
// import com.project.management.springboot.backend.project_management.repositories.LabelRepository;
// import com.project.management.springboot.backend.project_management.repositories.UserRepository;
// import com.project.management.springboot.backend.project_management.utils.mapper.LabelMapper;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.node.ObjectNode;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.*;
// import org.springframework.web.client.HttpClientErrorException;
// import org.springframework.web.client.RestTemplate;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.List;
// import java.util.stream.Collectors;

// @Service
// public class AICardsServiceImpl implements AICardsService {

//     @Autowired
//     private UserRepository userRepository;

//     @Autowired
//     private CardRepository cardRepository;

//     @Autowired
//     private LabelRepository labelRepository;

//     @Value("${OpenAI.api.key}")
//     private String apiKey;

//     @Value("${AI.cards.prompt.template}")
//     private String aiCardTemplate;

//     private final RestTemplate restTemplate;

//     public AICardsServiceImpl(RestTemplate restTemplate) {
//         this.restTemplate = restTemplate;
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public List<AICards> getAICardsForCurrentUser(AICardsDTO dto, Double temperature) {
//         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//         String username = authentication.getName();

//         User currentUser = userRepository.findByUsername(username)
//                 .orElseThrow(() -> new RuntimeException("Usuario actual no encontrado"));

//         List<Card> cards = cardRepository.findByUsers_Id(currentUser.getId());

//         return cards.stream().map(card -> {
//             Board board = card.getBoard();
//             List<LabelDTO> labels = labelRepository.findByBoard_Id(board.getId())
//                     .stream()
//                     .map(LabelMapper::toDTO)
//                     .collect(Collectors.toList());
//             return AICardsMapper.toAICards(card, board, labels, dto, temperature);
//         }).collect(Collectors.toList());
//     }

//     @Override
//     public String sendToIAApi(AICards aiCards, String prompt) {
//         String apiUrl = "https://api.openai.com/v1/chat/completions";
//         ObjectMapper mapper = new ObjectMapper();
//         ObjectNode requestJson = mapper.createObjectNode();

//         requestJson.put("model", "gpt-4");
//         requestJson.put("temperature", aiCards.getTemperature());

//         ObjectNode message = mapper.createObjectNode();
//         message.put("role", "user");
//         message.put("content", prompt);
//         requestJson.putArray("messages").add(message);

//         HttpHeaders headers = new HttpHeaders();
//         headers.setContentType(MediaType.APPLICATION_JSON);
//         headers.setBearerAuth(apiKey);

//         HttpEntity<String> request = new HttpEntity<>(requestJson.toString(), headers);

//         try {
//             ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);
//             return response.getBody();
//         } catch (HttpClientErrorException e) {
//             return "Error al obtener respuesta de IA: " + e.getResponseBodyAsString();
//         }
//     }
// }
