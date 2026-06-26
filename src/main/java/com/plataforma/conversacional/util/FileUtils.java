package com.plataforma.conversacional.util;

import java.util.Set;
import java.util.UUID;

public final class FileUtils {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "txt");

    private FileUtils() {}

    public static String getExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    public static boolean isAllowedType(String extension) {
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }

    public static String generateUniqueFileName(String originalName) {
        return UUID.randomUUID() + "." + getExtension(originalName);
    }
}
