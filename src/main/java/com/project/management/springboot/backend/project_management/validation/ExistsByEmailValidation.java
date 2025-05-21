package com.project.management.springboot.backend.project_management.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.management.springboot.backend.project_management.services.user.UserService;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class ExistsByEmailValidation implements ConstraintValidator<ExistsByEmail, String> {

    @Autowired
    private UserService service;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (service == null) {
            return true;
        }
        return !service.existsByEmail(email);
    }
}
