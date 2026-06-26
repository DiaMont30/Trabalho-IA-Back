package com.plataforma.conversacional.storage;

import org.springframework.stereotype.Service;
import java.io.InputStream;

@Service
public class LocalFileStorageService implements FileStorageService {

    @Override
    public String store(String fileName, byte[] content) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public InputStream retrieve(String storagePath) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(String storagePath) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
