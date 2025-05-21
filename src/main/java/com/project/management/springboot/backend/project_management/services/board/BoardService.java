package com.project.management.springboot.backend.project_management.services.board;

import java.util.List;
import java.util.Optional;

import com.project.management.springboot.backend.project_management.DTO.BoardDTO;

public interface BoardService {

    List<BoardDTO> findAll();

    Optional<BoardDTO> findById(Long id);

    BoardDTO save(BoardDTO boardDTO);

    Optional<BoardDTO> update(Long id, BoardDTO boardDTO);

    Optional<BoardDTO> delete(Long id);

    void reorganizarPosicionesPorUsuario(Long userId, Long deletedBoardId);

    void leaveBoard(Long boardId);

    void transferAdminRole(Long boardId, String newAdminEmail);

    List<BoardDTO> findBoardsByCurrentUser();
}
