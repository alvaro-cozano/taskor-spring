package com.project.management.springboot.backend.project_management.services.board;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.management.springboot.backend.project_management.DTO.BoardDTO;
import com.project.management.springboot.backend.project_management.DTO.UserBoardReferenceDTO;
import com.project.management.springboot.backend.project_management.DTO.UserReferenceDTO;
import com.project.management.springboot.backend.project_management.DTO.User_boardDTO;
import com.project.management.springboot.backend.project_management.entities.connection.User_board;
import com.project.management.springboot.backend.project_management.entities.models.Board;
import com.project.management.springboot.backend.project_management.entities.models.Card;
import com.project.management.springboot.backend.project_management.entities.models.User;
import com.project.management.springboot.backend.project_management.repositories.BoardRepository;
import com.project.management.springboot.backend.project_management.repositories.CardRepository;
import com.project.management.springboot.backend.project_management.repositories.StatusRepository;
import com.project.management.springboot.backend.project_management.repositories.UserRepository;
import com.project.management.springboot.backend.project_management.repositories.connection.User_boardRepository;
import com.project.management.springboot.backend.project_management.repositories.connection.User_cardRepository;
import com.project.management.springboot.backend.project_management.utils.mapper.BoardMapper;
import com.project.management.springboot.backend.project_management.utils.mapper.User_BoardMapper;

@Service
public class BoardServiceImpl implements BoardService {

    @Autowired
    private BoardRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private User_boardRepository userBoardRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private User_cardRepository userCardRepository;

    @Autowired
    private StatusRepository statusRepository;

    private BoardDTO enrichBoardWithUserBoard(Board board, User currentUser) {
        BoardDTO boardDTO = BoardMapper.toDTO(board);

        Optional<User_board> userBoardOpt = userBoardRepository.findByUserIdAndBoardId(currentUser.getId(),
                board.getId());
        userBoardOpt.ifPresent(userBoard -> {
            User_boardDTO userBoardDTO = User_BoardMapper.toDTO(userBoard);
            boardDTO.setUserBoardReference(new UserBoardReferenceDTO(
                    userBoardDTO.getPosX(),
                    userBoardDTO.getPosY(),
                    userBoardDTO.getIsAdmin()));
        });
        return boardDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardDTO> findAll() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> optionalCurrentUser = userRepository.findByUsername(username);
        if (optionalCurrentUser.isEmpty())
            return List.of();
        User currentUser = optionalCurrentUser.get();

        List<Board> boards = (List<Board>) repository.findAll();
        return boards.stream()
                .map(board -> enrichBoardWithUserBoard(board, currentUser))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BoardDTO> findById(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> optionalCurrentUser = userRepository.findByUsername(username);
        if (optionalCurrentUser.isEmpty())
            return Optional.empty();
        User currentUser = optionalCurrentUser.get();

        return repository.findById(id).map(board -> enrichBoardWithUserBoard(board, currentUser));
    }

    @Override
    @Transactional
    public BoardDTO save(BoardDTO boardDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> optionalCurrentUser = userRepository.findByUsername(username);
        if (optionalCurrentUser.isEmpty())
            throw new RuntimeException("Usuario actual no encontrado");
        User currentUser = optionalCurrentUser.get();

        List<User_board> currentUserBoards = userBoardRepository.findByUserId(currentUser.getId());
        for (User_board ub : currentUserBoards) {
            Optional<Board> board = repository.findById(ub.getBoard_id());
            if (board.isPresent() && board.get().getBoardName().equalsIgnoreCase(boardDTO.getBoardName())) {
                throw new RuntimeException("Ya existe un tablero con ese nombre para este usuario.");
            }
        }

        List<User> users = new ArrayList<>();
        users.add(currentUser);
        if (boardDTO.getUsers() != null) {
            for (UserReferenceDTO userDTO : boardDTO.getUsers()) {
                userRepository.findByEmail(userDTO.getEmail()).ifPresent(users::add);
            }
        }
        users = users.stream().distinct().toList();

        Board board = BoardMapper.toEntity(boardDTO);
        board.setUsers(users);
        Board savedBoard = repository.save(board);

        for (User user : users) {
            List<User_board> existingUserBoards = userBoardRepository.findByUserId(user.getId());
            Set<String> occupiedPositions = existingUserBoards.stream()
                    .map(ub -> ub.getPosX() + "," + ub.getPosY())
                    .collect(Collectors.toSet());

            int posX = 0;
            int posY = 0;
            boolean positionFound = false;

            outer: for (int row = 0; row < 100; row += 2) {
                for (int col = 0; col <= 2; col += 2) {
                    String key = col + "," + row;
                    if (!occupiedPositions.contains(key)) {
                        posX = col;
                        posY = row;
                        positionFound = true;
                        break outer;
                    }
                }
            }

            if (!positionFound)
                throw new RuntimeException("No se encontró una posición disponible para el usuario " + user.getEmail());

            User_board userBoard = new User_board();
            userBoard.setBoard_id(savedBoard.getId());
            userBoard.setUser_id(user.getId());
            userBoard.setAdmin(user.equals(currentUser));
            userBoard.setPosX(posX);
            userBoard.setPosY(posY);
            userBoardRepository.save(userBoard);
        }

        return enrichBoardWithUserBoard(savedBoard, currentUser);
    }

    @Override
    @Transactional
    public Optional<BoardDTO> delete(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> optionalCurrentUser = userRepository.findByUsername(username);
        if (optionalCurrentUser.isEmpty())
            return Optional.empty();
        User currentUser = optionalCurrentUser.get();

        Optional<Board> boardOptional = repository.findById(id);
        boardOptional.ifPresent(boardDb -> {
            Optional<User_board> userBoardOptional = userBoardRepository.findByUserIdAndBoardId(currentUser.getId(),
                    id);
            if (userBoardOptional.isPresent() && userBoardOptional.get().getIsAdmin()) {

                Hibernate.initialize(boardDb.getUsers());

                List<User_board> relaciones = userBoardRepository.findByBoardId(id);
                Set<Long> usuariosInvolucrados = relaciones.stream()
                        .map(User_board::getUser_id)
                        .collect(Collectors.toSet());

                List<Long> cardIds = cardRepository.findByBoardId(id).stream().map(Card::getId).toList();
                if (!cardIds.isEmpty())
                    userCardRepository.deleteByCardIdIn(cardIds);
                cardRepository.deleteByBoardId(id);
                userBoardRepository.deleteByBoardId(id);
                statusRepository.deleteByBoardId(id);
                repository.delete(boardDb);

                for (Long userId : usuariosInvolucrados) {
                    reorganizarPosicionesPorUsuario(userId, id);
                }

            } else {
                throw new RuntimeException("No tienes permiso para eliminar este tablero");
            }
        });

        return boardOptional.map(board -> enrichBoardWithUserBoard(board, currentUser));
    }

    @Transactional
    public void reorganizarPosicionesPorUsuario(Long userId, Long deletedBoardId) {
        List<User_board> userBoards = userBoardRepository.findByUserId(userId);

        if (userBoards.isEmpty()) {
            return;
        }

        List<User_board> sortedBoards = userBoards.stream()
                .filter(userBoard -> userBoard.getBoard_id() != deletedBoardId)
                .collect(Collectors.toList());

        int currentX = 0;
        int currentY = 0;

        for (User_board userBoard : sortedBoards) {
            userBoard.setPosX(currentX);
            userBoard.setPosY(currentY);

            userBoardRepository.save(userBoard);

            if (currentX == 0) {
                currentX = 2;
            } else {
                currentX = 0;
                currentY += 2;
            }
        }
    }

    @Override
    @Transactional
    public Optional<BoardDTO> update(Long id, BoardDTO boardDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> optionalCurrentUser = userRepository.findByUsername(username);
        if (optionalCurrentUser.isEmpty()) {
            return Optional.empty();
        }
        User currentUser = optionalCurrentUser.get();

        Optional<User_board> userBoardRelation = userBoardRepository.findByUserIdAndBoardId(currentUser.getId(), id);
        if (userBoardRelation.isEmpty() || !userBoardRelation.get().getIsAdmin()) {
            return Optional.empty();
        }

        List<User_board> userBoards = userBoardRepository.findByUserId(currentUser.getId());
        for (User_board ub : userBoards) {
            if (!ub.getBoard_id().equals(id)) {
                Optional<Board> board = repository.findById(ub.getBoard_id());
                if (board.isPresent() && board.get().getBoardName().equalsIgnoreCase(boardDTO.getBoardName())) {
                    throw new RuntimeException("Ya existe otro tablero con ese nombre para este usuario.");
                }
            }
        }

        List<User> usersToAssociate = new ArrayList<>();
        usersToAssociate.add(currentUser);
        if (boardDTO.getUsers() != null) {
            for (UserReferenceDTO userDTO : boardDTO.getUsers()) {
                Optional<User> userFromDb = userRepository.findByEmail(userDTO.getEmail());
                userFromDb.ifPresent(usersToAssociate::add);
            }
        }
        usersToAssociate = usersToAssociate.stream().distinct().collect(Collectors.toList());

        Optional<Board> optionalBoard = repository.findById(id);
        if (optionalBoard.isEmpty()) {
            return Optional.empty();
        }

        Board existingBoard = optionalBoard.get();
        existingBoard.setBoardName(boardDTO.getBoardName());
        existingBoard.setUsers(usersToAssociate);
        Board savedBoard = repository.save(existingBoard);

        for (User_board existingRelation : userBoardRepository.findByBoardId(savedBoard.getId())) {
            if (!usersToAssociate.stream().anyMatch(u -> u.getId().equals(existingRelation.getUser_id()))) {
                userBoardRepository.delete(existingRelation);

                if (!existingRelation.getUser_id().equals(currentUser.getId())) {
                    reorganizarPosicionesPorUsuario(existingRelation.getUser_id(), savedBoard.getId());
                }
            }
        }

        for (User user : usersToAssociate) {
            if (!userBoardRepository.existsByUserIdAndBoardId(user.getId(), savedBoard.getId())) {
                int posX = 0;
                int posY = 0;

                List<User_board> existingUserBoards = userBoardRepository.findByUserId(user.getId());
                Set<String> occupiedPositions = existingUserBoards.stream()
                        .map(ub -> ub.getPosX() + "," + ub.getPosY())
                        .collect(Collectors.toSet());

                boolean positionFound = false;
                outer: for (int row = 0; row < 100; row += 2) {
                    for (int col = 0; col <= 2; col += 2) {
                        String key = col + "," + row;
                        if (!occupiedPositions.contains(key)) {
                            posX = col;
                            posY = row;
                            positionFound = true;
                            break outer;
                        }
                    }
                }

                if (!positionFound) {
                    throw new RuntimeException(
                            "No se encontró una posición disponible para el usuario " + user.getEmail());
                }

                User_board userBoard = new User_board();
                userBoard.setBoard_id(savedBoard.getId());
                userBoard.setUser_id(user.getId());
                userBoard.setAdmin(false);
                userBoard.setPosX(posX);
                userBoard.setPosY(posY);
                userBoardRepository.save(userBoard);
            }
        }

        return Optional.of(enrichBoardWithUserBoard(savedBoard, currentUser));
    }

    @Override
    @Transactional
    public void leaveBoard(Long boardId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> optionalCurrentUser = userRepository.findByUsername(username);
        if (optionalCurrentUser.isEmpty()) {
            throw new RuntimeException("Usuario no autenticado");
        }
        User currentUser = optionalCurrentUser.get();

        Optional<User_board> userBoardOpt = userBoardRepository.findByUserIdAndBoardId(currentUser.getId(), boardId);
        if (userBoardOpt.isEmpty()) {
            throw new RuntimeException("El usuario no está asociado a este tablero");
        }

        User_board userBoard = userBoardOpt.get();

        if (userBoard.getIsAdmin()) {
            throw new RuntimeException("El administrador no puede abandonar el tablero con este método");
        }

        userBoardRepository.delete(userBoard);
        reorganizarPosicionesPorUsuario(currentUser.getId(), boardId);
    }

    @Override
    @Transactional
    public void transferAdminRole(Long boardId, String newAdminEmail) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Usuario actual no encontrado"));

        User newAdminUser = userRepository.findByEmail(newAdminEmail)
                .orElseThrow(() -> new RuntimeException("Usuario con el correo proporcionado no encontrado"));

        User_board currentAdminRelation = userBoardRepository.findByUserIdAndBoardId(currentUser.getId(), boardId)
                .orElseThrow(() -> new RuntimeException("No estás asociado a este tablero"));

        if (!currentAdminRelation.getIsAdmin()) {
            throw new RuntimeException("Solo un administrador puede transferir la administración");
        }

        User_board newAdminRelation = userBoardRepository.findByUserIdAndBoardId(newAdminUser.getId(), boardId)
                .orElseThrow(() -> new RuntimeException("El nuevo administrador no pertenece a este tablero"));

        currentAdminRelation.setAdmin(false);
        newAdminRelation.setAdmin(true);

        userBoardRepository.save(currentAdminRelation);
        userBoardRepository.save(newAdminRelation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardDTO> findBoardsByCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> optionalCurrentUser = userRepository.findByUsername(username);
        if (optionalCurrentUser.isEmpty())
            return List.of();
        User currentUser = optionalCurrentUser.get();

        List<User_board> userBoards = userBoardRepository.findByUserId(currentUser.getId());
        List<Long> boardIds = userBoards.stream().map(User_board::getBoard_id).collect(Collectors.toList());
        Iterable<Board> iterableBoards = repository.findAllById(boardIds);
        List<Board> boards = StreamSupport.stream(iterableBoards.spliterator(), false).collect(Collectors.toList());

        boards.sort((b1, b2) -> {
            User_board ub1 = userBoards.stream().filter(ub -> ub.getBoard_id().equals(b1.getId())).findFirst()
                    .orElse(null);
            User_board ub2 = userBoards.stream().filter(ub -> ub.getBoard_id().equals(b2.getId())).findFirst()
                    .orElse(null);
            if (ub1 != null && ub2 != null) {
                int cmpX = Integer.compare(ub1.getPosX(), ub2.getPosX());
                return (cmpX != 0) ? cmpX : Integer.compare(ub1.getPosY(), ub2.getPosY());
            }
            return 0;
        });

        return boards.stream()
                .map(board -> enrichBoardWithUserBoard(board, currentUser))
                .collect(Collectors.toList());
    }
}
