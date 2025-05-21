package com.project.management.springboot.backend.project_management;

import java.sql.Date;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.project.management.springboot.backend.project_management.entities.connection.User_board;
import com.project.management.springboot.backend.project_management.entities.connection.User_card;
import com.project.management.springboot.backend.project_management.entities.connection.User_roles;
import com.project.management.springboot.backend.project_management.entities.models.Board;
import com.project.management.springboot.backend.project_management.entities.models.Card;
import com.project.management.springboot.backend.project_management.entities.models.ChecklistItem;
import com.project.management.springboot.backend.project_management.entities.models.ChecklistSubItem;
import com.project.management.springboot.backend.project_management.entities.models.Label;
import com.project.management.springboot.backend.project_management.entities.models.Role;
import com.project.management.springboot.backend.project_management.entities.models.Status;
import com.project.management.springboot.backend.project_management.entities.models.User;
import com.project.management.springboot.backend.project_management.repositories.BoardRepository;
import com.project.management.springboot.backend.project_management.repositories.CardRepository;
import com.project.management.springboot.backend.project_management.repositories.ChecklistItemRepository;
import com.project.management.springboot.backend.project_management.repositories.ChecklistSubItemRepository;
import com.project.management.springboot.backend.project_management.repositories.LabelRepository;
import com.project.management.springboot.backend.project_management.repositories.RoleRepository;
import com.project.management.springboot.backend.project_management.repositories.StatusRepository;
import com.project.management.springboot.backend.project_management.repositories.UserRepository;
import com.project.management.springboot.backend.project_management.repositories.connection.User_boardRepository;
import com.project.management.springboot.backend.project_management.repositories.connection.User_cardRepository;
import com.project.management.springboot.backend.project_management.repositories.connection.User_rolesRepository;
import com.project.management.springboot.backend.project_management.services.user.UserServiceImpl;

@Configuration
public class DataSeeder {

        @Autowired
        private UserServiceImpl userService;

        @Bean
        CommandLineRunner initDatabase(
                        RoleRepository roleRepository,
                        UserRepository userRepository,
                        StatusRepository statusRepository,
                        User_rolesRepository user_rolesRepository,
                        BoardRepository boardRepository,
                        User_boardRepository user_boardRepository,
                        CardRepository cardRepository,
                        User_cardRepository user_cardRepository,
                        LabelRepository labelRepository,
                        ChecklistItemRepository checklistItemRepository,
                        ChecklistSubItemRepository checklistSubItemRepository) {
                return args -> {
                        if (roleRepository.count() == 0) {
                                Role admin = new Role("User", new Date(0), new Date(0));
                                Role user = new Role("Admin", new Date(0), new Date(0));
                                Role premium = new Role("Premium", new Date(0), new Date(0));

                                roleRepository.saveAll(Arrays.asList(admin, user, premium));
                        }
                        ;

                        if (userRepository.count() == 0) {
                                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                                String encodedPassword1 = passwordEncoder.encode("alvaroc");
                                String encodedPassword2 = passwordEncoder.encode("pablocoz");
                                String encodedPassword3 = passwordEncoder.encode("yolandapya");
                                String encodedPassword4 = passwordEncoder.encode("jocapeva");
                                Date currentDate = new Date(0);
                                User user0 = new User(true, "Alvaro", "Cozano", "alvarocozano@gmail.com", "alvaroc",
                                                encodedPassword1, true,
                                                currentDate, currentDate,
                                                userService.generateProfileImage("alvarocozano@gmail.com"));
                                User user1 = new User(true, "Pablo", "Cozano", "pablocozano@gmail.com", "pablocoz",
                                                encodedPassword2, false,
                                                currentDate, currentDate,
                                                userService.generateProfileImage("pablocozano@gmail.com"));
                                User user2 = new User(true, "Yolanda", "Perez", "yolandapya@gmail.com", "yolandapya",
                                                encodedPassword3, true,
                                                currentDate, currentDate,
                                                userService.generateProfileImage("yolandapya@gmail.com"));
                                User user3 = new User(true, "Juan Carlos", "Cozano", "jccozano@gmail.com", "jocapeva",
                                                encodedPassword4,
                                                false, currentDate, currentDate,
                                                userService.generateProfileImage("jccozano@gmail.com"));
                                userRepository.saveAll(
                                                Arrays.asList(user0, user1, user2, user3));
                        }
                        ;

                        if (user_rolesRepository.count() == 0) {
                                User_roles roleUser1 = new User_roles(1L, 1L);
                                User_roles roleUser2 = new User_roles(2L, 1L);
                                User_roles roleUser3 = new User_roles(3L, 1L);
                                User_roles roleUser4 = new User_roles(4L, 1L);
                                User_roles roleAdmin = new User_roles(1L, 2L);
                                User_roles rolePremium = new User_roles(1L, 3L);

                                user_rolesRepository.saveAll(Arrays.asList(roleUser1, roleUser2, roleUser3, roleUser4,
                                                roleAdmin, rolePremium));
                        }
                        ;

                        if (boardRepository.count() == 0) {
                                Board board1 = new Board("Cesur", new Date(0), new Date(0));
                                Board board2 = new Board("Viewnext", new Date(0), new Date(0));
                                boardRepository.saveAll(
                                                Arrays.asList(board1, board2));
                        }
                        ;

                        if (user_boardRepository.count() == 0) {
                                User_board user_board1 = new User_board(1L, 1L, true, 0, 0);
                                User_board user_board2 = new User_board(1L, 2L, true, 2, 0);
                                User_board user_board3 = new User_board(2L, 1L, false, 0, 0);
                                User_board user_board4 = new User_board(2L, 1L, false, 0, 0);
                                User_board user_board5 = new User_board(3L, 1L, false, 0, 0);
                                User_board user_board6 = new User_board(4L, 1L, false, 0, 0);
                                user_boardRepository.saveAll(Arrays.asList(user_board1, user_board2, user_board3,
                                                user_board4, user_board5, user_board6));
                        }
                        ;

                        if (statusRepository.count() == 0) {
                                Board board = boardRepository.findById(1L)
                                                .orElseThrow(() -> new RuntimeException("Board no encontrado"));
                                Status pending = new Status("Pendiente", new Date(0), new Date(0), board);
                                Status finished = new Status("Terminado", new Date(0), new Date(0), board);

                                statusRepository.saveAll(Arrays.asList(pending, finished));
                        }
                        ;

                        if (cardRepository.count() == 0) {
                                Board board = boardRepository.findById(1L)
                                                .orElseThrow(() -> new RuntimeException("Board no encontrado"));
                                Status status1 = statusRepository.findById(1L)
                                                .orElseThrow(() -> new RuntimeException("Status no encontrado"));
                                Status status2 = statusRepository.findById(2L)
                                                .orElseThrow(() -> new RuntimeException("Status no encontrado"));

                                java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());

                                Label labelScreen = new Label("Spring", "#00ffd4", board);
                                Label labelService = new Label("React", "#bd00ff", board);
                                labelRepository.saveAll(Arrays.asList(labelScreen, labelService));

                                Card card1 = new Card(
                                                "Pantallas",
                                                "Diseñar la pantalla de login en react con typescript",
                                                currentDate,
                                                currentDate,
                                                1L,
                                                0,
                                                board,
                                                status1,
                                                false);
                                card1.setAttachedLinks("https://www.youtube.com/");
                                card1.setLabel(labelScreen);

                                Card card2 = new Card(
                                                "Servicios",
                                                "Crear los servicios de login en springboot con JWT",
                                                currentDate,
                                                currentDate,
                                                1L,
                                                0,
                                                board,
                                                status2,
                                                true);
                                card2.setAttachedLinks("https://www.youtube.com/");
                                card2.setLabel(labelService);

                                cardRepository.saveAll(Arrays.asList(card1, card2));

                                ChecklistItem frontendItem1 = new ChecklistItem("Diseñar la pantalla", false, card1);
                                ChecklistItem frontendItem2 = new ChecklistItem(
                                                "Hacer las llamadas a la api de springboot", false, card1);

                                ChecklistSubItem frontendSubItem1 = new ChecklistSubItem(
                                                "Diseñar la estructura del formulario",
                                                false, frontendItem1);
                                ChecklistSubItem frontendSubItem2 = new ChecklistSubItem(
                                                "Crear el css y hacerlo responsive",
                                                false, frontendItem1);
                                frontendItem1.addSubItem(frontendSubItem1);
                                frontendItem1.addSubItem(frontendSubItem2);

                                ChecklistItem backendItem1 = new ChecklistItem("Configurar base de datos con hibernate",
                                                false,
                                                card2);
                                ChecklistItem backendItem2 = new ChecklistItem("Crear la conexión con el front", false,
                                                card2);

                                ChecklistSubItem backendSubItem1 = new ChecklistSubItem("Crear los servicios", false,
                                                backendItem2);
                                ChecklistSubItem backendSubItem2 = new ChecklistSubItem("Crear los controladores",
                                                false,
                                                backendItem2);
                                backendItem2.addSubItem(backendSubItem1);
                                backendItem2.addSubItem(backendSubItem2);

                                checklistItemRepository.saveAll(Arrays.asList(
                                                frontendItem1, frontendItem2,
                                                backendItem1, backendItem2));

                                checklistSubItemRepository.saveAll(Arrays.asList(
                                                frontendSubItem1, frontendSubItem2,
                                                backendSubItem1, backendSubItem2));
                        }
                        ;

                        if (user_cardRepository.count() == 0) {
                                User_card user_card1 = new User_card(1L, 1L);
                                User_card user_card2 = new User_card(1L, 2L);
                                User_card user_card3 = new User_card(2L, 1L);
                                User_card user_card4 = new User_card(3L, 1L);
                                User_card user_card5 = new User_card(4L, 1L);
                                user_cardRepository.saveAll(Arrays.asList(user_card1, user_card2, user_card3,
                                                user_card4, user_card5));
                        }
                        ;
                };
        }
}