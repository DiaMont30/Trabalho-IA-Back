package com.plataforma.conversacional.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AllowedFileTypeValidator implements ConstraintValidator<AllowedFileType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
