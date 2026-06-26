package com.plataforma.conversacional.validation;

import com.plataforma.conversacional.util.FileUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AllowedFileTypeValidator implements ConstraintValidator<AllowedFileType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return FileUtils.isAllowedType(FileUtils.getExtension(value));
    }
}
