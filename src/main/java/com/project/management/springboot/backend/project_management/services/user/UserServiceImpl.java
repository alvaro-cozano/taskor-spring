package com.project.management.springboot.backend.project_management.services.user;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.project.management.springboot.backend.project_management.DTO.UserReferenceDTO;
import com.project.management.springboot.backend.project_management.entities.models.Role;
import com.project.management.springboot.backend.project_management.entities.models.User;
import com.project.management.springboot.backend.project_management.repositories.RoleRepository;
import com.project.management.springboot.backend.project_management.repositories.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return (List<User>) repository.findAll();
    }

    @Override
    @Transactional
    public User save(User user) {

        Optional<Role> optionalRoleUser = roleRepository.findByName("User");
        List<Role> roles = new ArrayList<>();

        optionalRoleUser.ifPresent(roles::add);

        if (user.isAdmin()) {
            Optional<Role> optionalRoleAdmin = roleRepository.findByName("Admin");
            optionalRoleAdmin.ifPresent(roles::add);
        }

        user.setRoles(roles);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getProfileImage() == null) {
            byte[] profileImage = generateProfileImage(user.getEmail());
            user.setProfileImage(profileImage);
        }


        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setEnabled(false);
        repository.save(user);

        sendVerificationEmail(user);
        return user;
    }

    @Override
    public void sendVerificationEmail(User user) {
        String subject = "Confirma tu cuenta en Taskor";
        String verificationUrl = appBaseUrl + "/auth/verify?token=" + user.getVerificationToken();

        Context context = new Context();
        context.setVariable("name", user.getUsername());
        context.setVariable("verificationUrl", verificationUrl);

        String htmlContent = templateEngine.process("verification-email", context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            ClassPathResource logo = new ClassPathResource("static/img/logo.png");
            if (logo.exists()){
                 helper.addInline("logo", logo);
            } else {
                System.err.println("Logo image not found at classpath:static/img/logo.png for verification email");
            }
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] generateProfileImage(String email) {
        char initial = email.charAt(0);
        String avatarUrl = "https://ui-avatars.com/api/?name=" + initial +
                "&color=ffffff&background=" + getRandomColor();

        try {
            URL url = new URL(avatarUrl);
            BufferedImage image = ImageIO.read(url);

            if (image == null) {
                image = getDefaultImage();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return getDefaultImageByteArray();
        }
    }

    private static final List<String> predefinedColors = Arrays.asList(
            "ff5733",
            "33ff57",
            "3357ff",
            "f1c40f",
            "8e44ad",
            "2ecc71",
            "e74c3c",
            "3498db");

    public static String getRandomColor() {
        Random random = new Random();
        return predefinedColors.get(random.nextInt(predefinedColors.size()));
    }

    private BufferedImage getDefaultImage() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        return img;
    }

    private byte[] getDefaultImageByteArray() {
        try {
            BufferedImage img = getDefaultImage();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "PNG", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    @Override
    @Transactional
    public User updateProfile(Long id, User incomingUser) {
        User existingUser = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        if (incomingUser.getFirst_name() != null && !incomingUser.getFirst_name().isBlank()) {
            existingUser.setFirst_name(incomingUser.getFirst_name());
        }

        if (incomingUser.getLast_name() != null && !incomingUser.getLast_name().isBlank()) {
            existingUser.setLast_name(incomingUser.getLast_name());
        }

        if (incomingUser.getProfileImage() != null && incomingUser.getProfileImage().length > 0) {
            existingUser.setProfileImage(incomingUser.getProfileImage());
        }

        if (incomingUser.getUsername() != null && !incomingUser.getUsername().isBlank()) {
            if (!existingUser.getUsername().equals(incomingUser.getUsername()) && repository.existsByUsername(incomingUser.getUsername())) {
                throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
            }
            existingUser.setUsername(incomingUser.getUsername());
        }

        if (incomingUser.getPassword() != null && !incomingUser.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(incomingUser.getPassword()));
        }

        return repository.save(existingUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserReferenceDTO> findAllEmails() {
        List<UserReferenceDTO> usersWithEmailsAndImages = new ArrayList<>();
        List<User> users = (List<User>) repository.findAll();

        for (User user : users) {
            String email = user.getEmail();
            byte[] imageBytes = user.getProfileImage();

            String base64Image = null;
            if (imageBytes != null) {
                base64Image = Base64.getEncoder().encodeToString(imageBytes);
            }

            UserReferenceDTO dto = new UserReferenceDTO();
            dto.setEmail(email);
            dto.setProfileImageBase64(base64Image);

            usersWithEmailsAndImages.add(dto);
        }

        return usersWithEmailsAndImages;
    }

    @Override
    public String findEmailByUsername(String username) {
        Optional<User> userOpt = findByUsername(username);
        return userOpt.map(User::getEmail).orElse(null);
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }


    @Override
    @Transactional
    public void requestEmailChange(String currentUsername, String newEmail) throws MessagingException, IOException {
        User user = repository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + currentUsername));

        if (user.getEmail().equalsIgnoreCase(newEmail)) {
            throw new IllegalArgumentException("El nuevo correo electrónico es el mismo que el actual.");
        }

        if (repository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("El nuevo correo electrónico ya está registrado por otro usuario.");
        }

        Optional<User> userWithPendingEmail = repository.findByPendingEmail(newEmail);
        if (userWithPendingEmail.isPresent() && !userWithPendingEmail.get().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Este correo electrónico ya está pendiente de confirmación para otro usuario.");
        }

        String token = UUID.randomUUID().toString();
        user.setPendingEmail(newEmail);
        user.setEmailChangeToken(token);
        user.setEmailChangeTokenExpiryDate(LocalDateTime.now().plusHours(24));
        repository.save(user);

        sendEmailChangeConfirmationMail(user, newEmail, token);
    }

    private void sendEmailChangeConfirmationMail(User user, String newEmail, String token) throws MessagingException, IOException {
        String subject = "Confirma tu nuevo correo electrónico en Taskor";
        String confirmationUrl = appBaseUrl + "/auth/confirm-email-change?token=" + token;

        Context context = new Context();
        context.setVariable("name", user.getFirst_name() != null ? user.getFirst_name() : user.getUsername());
        context.setVariable("newEmail", newEmail);
        context.setVariable("confirmationUrl", confirmationUrl);

        String htmlContent = templateEngine.process("confirm-new-email", context);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(newEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        ClassPathResource logo = new ClassPathResource("static/img/logo.png");
        if (logo.exists()) {
            helper.addInline("logo", logo);
        } else {
            System.err.println("Logo image not found at classpath:static/img/logo.png for email change confirmation");
        }

        mailSender.send(mimeMessage);
    }

    @Override
    @Transactional
    public boolean confirmEmailChange(String token) {
        Optional<User> userOptional = repository.findByEmailChangeToken(token);
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        if (user.getEmailChangeTokenExpiryDate().isBefore(LocalDateTime.now())) {
            user.setPendingEmail(null);
            user.setEmailChangeToken(null);
            user.setEmailChangeTokenExpiryDate(null);
            repository.save(user);
            return false;
        }

        if (user.getPendingEmail() == null) {
            user.setEmailChangeToken(null);
            user.setEmailChangeTokenExpiryDate(null);
            repository.save(user);
            return false; // O lanzar una excepción
        }
        
        Optional<User> existingUserWithNewEmail = repository.findByEmail(user.getPendingEmail());
        if (existingUserWithNewEmail.isPresent() && !existingUserWithNewEmail.get().getId().equals(user.getId())) {

            user.setPendingEmail(null);
            user.setEmailChangeToken(null);
            user.setEmailChangeTokenExpiryDate(null);
            repository.save(user);
            throw new IllegalArgumentException("El correo electrónico '" + user.getPendingEmail() + "' ya ha sido registrado por otro usuario. Por favor, intenta con otro.");
        }


        user.setEmail(user.getPendingEmail());
        user.setPendingEmail(null);
        user.setEmailChangeToken(null);
        user.setEmailChangeTokenExpiryDate(null);
        repository.save(user);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    @Transactional
    public void addRoleToUser(Long userId, String roleName) {
        Optional<User> userOptional = repository.findById(userId);
        Optional<Role> roleOptional = roleRepository.findByName(roleName);

        if (userOptional.isPresent() && roleOptional.isPresent()) {
            User user = userOptional.get();
            Role role = roleOptional.get();
            if (user.getRoles() == null) {
                user.setRoles(new ArrayList<>());
            }
            if (!user.getRoles().contains(role)) {
                user.getRoles().add(role);
                repository.save(user);
            }
        } else {
            // Considera loggear un aviso si el usuario o el rol no se encuentran
            System.err.println("Error al añadir rol: Usuario con ID " + userId + " o Rol con nombre " + roleName + " no encontrado.");
        }
    }

    @Override
    @Transactional
    public void removeRoleFromUser(Long userId, String roleName) {
        Optional<User> userOptional = repository.findById(userId);
        Optional<Role> roleOptional = roleRepository.findByName(roleName);

        if (userOptional.isPresent() && roleOptional.isPresent()) {
            User user = userOptional.get();
            Role role = roleOptional.get();
            if (user.getRoles() != null && user.getRoles().contains(role)) {
                user.getRoles().remove(role);
                repository.save(user);
            }
        } else {
            // Considera loggear un aviso
             System.err.println("Error al quitar rol: Usuario con ID " + userId + " o Rol con nombre " + roleName + " no encontrado.");
        }
    }
}
