package com.plataforma.conversacional.constants;

public final class ApiConstants {

    public static final String API_VERSION = "/api/v1";

    public static final String SESSION_PATH = "/sessions";
    public static final String MESSAGE_PATH = "/messages";
    public static final String DOCUMENT_PATH = "/documents";
    public static final String HEALTH_PATH = "/health";

    public static final String SESSION_ID_VARIABLE = "sessionId";
    public static final String DOCUMENT_ID_VARIABLE = "documentId";

    public static final int MAX_FILE_SIZE = 10 * 1024 * 1024;
    public static final int MAX_MESSAGE_LENGTH = 5000;
    public static final int MIN_MESSAGE_LENGTH = 1;
    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private ApiConstants() {}
}
