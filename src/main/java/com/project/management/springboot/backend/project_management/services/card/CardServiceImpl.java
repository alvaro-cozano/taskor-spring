package com.project.management.springboot.backend.project_management.services.card;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.management.springboot.backend.project_management.DTO.CardDTO;
import com.project.management.springboot.backend.project_management.DTO.UserReferenceDTO;
import com.project.management.springboot.backend.project_management.DTO.ChecklistItemDTO;
import com.project.management.springboot.backend.project_management.DTO.ChecklistSubItemDTO;
import com.project.management.springboot.backend.project_management.entities.connection.User_card;
import com.project.management.springboot.backend.project_management.entities.models.Card;
import com.project.management.springboot.backend.project_management.entities.models.ChecklistItem;
import com.project.management.springboot.backend.project_management.entities.models.ChecklistSubItem;
import com.project.management.springboot.backend.project_management.entities.models.Label;
import com.project.management.springboot.backend.project_management.entities.models.User;
import com.project.management.springboot.backend.project_management.repositories.BoardRepository;
import com.project.management.springboot.backend.project_management.repositories.CardRepository;
import com.project.management.springboot.backend.project_management.repositories.StatusRepository;
import com.project.management.springboot.backend.project_management.repositories.UserRepository;
import com.project.management.springboot.backend.project_management.repositories.connection.User_boardRepository;
import com.project.management.springboot.backend.project_management.repositories.connection.User_cardRepository;
import com.project.management.springboot.backend.project_management.repositories.ChecklistItemRepository;
import com.project.management.springboot.backend.project_management.repositories.ChecklistSubItemRepository;
import com.project.management.springboot.backend.project_management.repositories.LabelRepository;
import com.project.management.springboot.backend.project_management.utils.mapper.CardMapper;
import com.project.management.springboot.backend.project_management.utils.mapper.ChecklistItemMapper;
import com.project.management.springboot.backend.project_management.utils.mapper.ChecklistSubItemMapper;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CardServiceImpl implements CardService {

    private final User_boardRepository user_boardRepository;
    private final StatusRepository statusRepository;
    private final BoardRepository boardRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private User_cardRepository userCardRepository;

    @Autowired
    private ChecklistItemRepository checklistItemRepository;

    @Autowired
    private ChecklistSubItemRepository checklistSubItemRepository;

    @Autowired
    private LabelRepository labelRepository;

    CardServiceImpl(BoardRepository boardRepository, StatusRepository statusRepository,
            User_boardRepository user_boardRepository) {
        this.boardRepository = boardRepository;
        this.statusRepository = statusRepository;
        this.user_boardRepository = user_boardRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardDTO> findAll() {
        List<Card> cards = (List<Card>) cardRepository.findAll();
        return cards.stream().map(CardMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CardDTO> findById(Long id) {
        return cardRepository.findById(id).map(CardMapper::toDTO);
    }

    @Override
    @Transactional
    public CardDTO save(CardDTO cardDTO) {
        List<Card> cards = cardRepository.findByBoardId(cardDTO.getBoard_id());
        for (Card card : cards) {
            if (!card.getId().equals(cardDTO.getId()) &&
                    card.getCardTitle().equalsIgnoreCase(cardDTO.getCardTitle())) {
                throw new RuntimeException("Ya existe una tarjeta con el mismo título en este tablero");
            }
        }

        if (cardDTO.getBoard_id() != null && cardDTO.getStatus_id() != null) {
            List<Card> cardsInColumn = cardRepository.findByBoardIdAndStatusId(cardDTO.getBoard_id(),
                    cardDTO.getStatus_id());
            for (Card c : cardsInColumn) {
                if (c.getPosition() != null) {
                    c.setPosition(c.getPosition() + 1);
                    cardRepository.save(c);
                }
            }
        }

        List<User> users = new ArrayList<>();
        if (cardDTO.getUsers() != null) {
            for (UserReferenceDTO userDTO : cardDTO.getUsers()) {
                userRepository.findByEmail(userDTO.getEmail()).ifPresent(users::add);
            }
        }
        users = users.stream().distinct().collect(Collectors.toList());

        Card card = CardMapper.toEntity(cardDTO);
        card.setUsers(users);
        card.setAttachedLinks(cardDTO.getAttachedLinks());
        card.setFinished(cardDTO.getFinished() != null ? cardDTO.getFinished() : false);

        card.setPosition(0);

        if (cardDTO.getLabel() != null && cardDTO.getLabel().getId() != null) {
            Optional<Label> optionalLabel = labelRepository.findById(cardDTO.getLabel().getId());
            if (optionalLabel.isPresent()) {
                card.setLabel(optionalLabel.get());
            } else {
                throw new EntityNotFoundException("Etiqueta no encontrada");
            }
        }

        Card savedCard = cardRepository.save(card);

        if (cardDTO.getChecklistItems() != null) {
            for (ChecklistItemDTO checklistItemDTO : cardDTO.getChecklistItems()) {
                ChecklistItem checklistItem = ChecklistItemMapper.toEntity(checklistItemDTO);
                checklistItem.setCard(savedCard);
                checklistItem = checklistItemRepository.save(checklistItem);

                if (checklistItemDTO.getSubItems() != null) {
                    for (ChecklistSubItemDTO subItemDTO : checklistItemDTO.getSubItems()) {
                        ChecklistSubItem subItem = ChecklistSubItemMapper.toEntity(subItemDTO);
                        subItem.setChecklistItem(checklistItem);
                        checklistSubItemRepository.save(subItem);
                    }
                }
            }
        }

        for (User user : users) {
            addCardToUser(savedCard.getId(), user.getId());
        }

        return CardMapper.toDTO(savedCard);
    }

    @Override
    @Transactional
    public Optional<CardDTO> update(Long id, CardDTO cardDTO) {
        Optional<Card> optionalCard = cardRepository.findById(id);
        if (optionalCard.isEmpty()) {
            return Optional.empty();
        }

        Card existingCard = optionalCard.get();

        Long oldStatusId = existingCard.getStatus() != null ? existingCard.getStatus().getId() : null;
        Integer oldPosition = existingCard.getPosition();

        if (cardDTO.getCardTitle() != null && !cardDTO.getCardTitle().equals(existingCard.getCardTitle())) {
            List<Card> cards = cardRepository.findByBoardId(cardDTO.getBoard_id());
            for (Card card : cards) {
                if (!card.getId().equals(cardDTO.getId()) &&
                        card.getCardTitle().equalsIgnoreCase(cardDTO.getCardTitle())) {
                    throw new RuntimeException("Ya existe una tarjeta con el mismo título en este tablero");
                }
            }
        }

        if (cardDTO.getStatus_id() != null && oldStatusId != null && !cardDTO.getStatus_id().equals(oldStatusId)) {

            List<Card> cardsInOldStatus = cardRepository.findByStatusId(oldStatusId);
            for (Card c : cardsInOldStatus) {
                if (c.getPosition() != null && c.getPosition() > oldPosition) {
                    c.setPosition(c.getPosition() - 1);
                    cardRepository.save(c);
                }
            }
        }

        if (cardDTO.getCardTitle() != null)
            existingCard.setCardTitle(cardDTO.getCardTitle());
        if (cardDTO.getDescription() != null)
            existingCard.setDescription(cardDTO.getDescription());
        if (cardDTO.getStartDate() != null)
            existingCard.setStart_date(cardDTO.getStartDate());
        if (cardDTO.getEndDate() != null)
            existingCard.setEnd_date(cardDTO.getEndDate());
        if (cardDTO.getPriority() != null)
            existingCard.setPriority(cardDTO.getPriority());
        if (cardDTO.getPosition() != null)
            existingCard.setPosition(cardDTO.getPosition());
        if (cardDTO.getAttachedLinks() != null)
            existingCard.setAttachedLinks(cardDTO.getAttachedLinks());
        if (cardDTO.getFinished() != null)
            existingCard.setFinished(cardDTO.getFinished());
        if (cardDTO.getBoard_id() != null) {
            existingCard.setBoard(boardRepository.findById(cardDTO.getBoard_id()).orElse(null));
        }
        if (cardDTO.getStatus_id() != null) {
            existingCard.setStatus(statusRepository.findById(cardDTO.getStatus_id()).orElse(null));
        }

        if (cardDTO.getLabel() != null) {
            if (cardDTO.getLabel().getId() != null) {
                labelRepository.findById(cardDTO.getLabel().getId())
                        .ifPresentOrElse(
                                existingCard::setLabel,
                                () -> {
                                    throw new EntityNotFoundException("Etiqueta no encontrada");
                                });
            } else {
                existingCard.setLabel(null);
            }
        } else {
            existingCard.setLabel(null);
        }

        List<User> usersToAssociate = new ArrayList<>();
        if (cardDTO.getUsers() != null) {
            for (UserReferenceDTO userDTO : cardDTO.getUsers()) {
                userRepository.findByEmail(userDTO.getEmail()).ifPresent(usersToAssociate::add);
            }
            usersToAssociate = usersToAssociate.stream().distinct().collect(Collectors.toList());
            existingCard.setUsers(usersToAssociate);
        }

        Card savedCard = cardRepository.save(existingCard);

        if (cardDTO.getUsers() != null) {
            List<User_card> existingRelations = userCardRepository.findByCardId(savedCard.getId());
            for (User_card existing : existingRelations) {
                if (usersToAssociate.stream().noneMatch(u -> u.getId().equals(existing.getUser_id()))) {
                    userCardRepository.delete(existing);
                }
            }

            for (User user : usersToAssociate) {
                Optional<User_card> existing = userCardRepository.findByUserIdAndCardId(user.getId(),
                        savedCard.getId());
                if (existing.isEmpty()) {
                    addCardToUser(user.getId(), savedCard.getId());
                }
            }
        }

        if (cardDTO.getChecklistItems() != null) {
            for (ChecklistItemDTO checklistItemDTO : cardDTO.getChecklistItems()) {
                if (checklistItemDTO.getId() != null) {
                    ChecklistItem checklistItem = checklistItemRepository.findById(checklistItemDTO.getId())
                            .orElseThrow(() -> new EntityNotFoundException("Checklist Item no encontrado"));
                    if (checklistItemDTO.getTitle() != null)
                        checklistItem.setTitle(checklistItemDTO.getTitle());
                    if (checklistItemDTO.getCompleted() != null)
                        checklistItem.setCompleted(checklistItemDTO.getCompleted());
                    checklistItemRepository.save(checklistItem);

                    if (checklistItemDTO.getSubItems() != null) {
                        for (ChecklistSubItemDTO subItemDTO : checklistItemDTO.getSubItems()) {
                            ChecklistSubItem subItem = ChecklistSubItemMapper.toEntity(subItemDTO);
                            subItem.setChecklistItem(checklistItem);
                            checklistSubItemRepository.save(subItem);
                        }
                    }
                }
            }
        }

        return Optional.of(CardMapper.toDTO(savedCard));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarjeta no encontrada"));

        List<ChecklistItem> checklistItems = checklistItemRepository.findByCard_Id(id);
        for (ChecklistItem checklistItem : checklistItems) {
            checklistSubItemRepository.deleteAll(checklistItem.getSubItems());
        }
        checklistItemRepository.deleteAll(checklistItems);

        userCardRepository.deleteByCardId(card.getId());

        Long boardId = card.getBoard().getId();
        Long statusId = card.getStatus().getId();
        Integer deletedCardPosition = card.getPosition();

        cardRepository.delete(card);

        List<Card> cardsToReorder = cardRepository.findByBoardIdAndStatusId(boardId, statusId);
        for (Card c : cardsToReorder) {
            if (c.getPosition() != null && c.getPosition() > deletedCardPosition) {
                c.setPosition(c.getPosition() - 1);
                cardRepository.save(c);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardDTO> findCardsByCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Usuario actual no encontrado");
        }

        User currentUser = optionalUser.get();

        List<User_card> userCards = userCardRepository.findByUserId(currentUser.getId());

        List<Long> cardIds = userCards.stream()
                .map(User_card::getCard_id)
                .collect(Collectors.toList());

        Iterable<Card> cards = cardRepository.findAllById(cardIds);
        List<CardDTO> cardDTOs = new ArrayList<>();
        cards.forEach(card -> cardDTOs.add(CardMapper.toDTO(card)));

        return cardDTOs;
    }

    @Override
    public List<CardDTO> getCardsForBoard(Long board_id, Long user_id) {
        if (!user_boardRepository.existsByUser_idAndBoard_id(user_id, board_id)) {
            throw new AccessDeniedException("El usuario no está autorizado para ver las tarjetas de este tablero.");
        }

        List<Card> cards = cardRepository.findByBoardId(board_id);
        return cards.stream()
                .map(CardMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void addCardToUser(Long card_id, Long user_id) {
        Optional<User_card> existingUserCard = userCardRepository.findByCardIdAndUserId(user_id, card_id);
        if (existingUserCard.isEmpty()) {
            User_card userCard = new User_card(user_id, card_id);
            userCardRepository.save(userCard);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardDTO> getCardsByBoardAndStatus(Long boardId, Long statusId, Long userId) {
        List<Card> cardsByBoardAndStatus = cardRepository.findByBoardIdAndStatusId(boardId, statusId);
        return cardsByBoardAndStatus.stream()
                .map(CardMapper::toDTO)
                .collect(Collectors.toList());
    }
}
