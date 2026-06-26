package com.plataforma.conversacional.storage;

import java.io.InputStream;

public interface FileStorageService {

    String store(String fileName, byte[] content);
    InputStream retrieve(String storagePath);
    void delete(String storagePath);
}
