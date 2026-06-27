package com.plataforma.conversacional.parsing;

import com.plataforma.conversacional.exception.ParsingException;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

@Component
public class TxtParser implements DocumentParser {

    @Override
    public String parse(byte[] content, String contentType) {
        if (content == null) {
            throw new ParsingException("Content must not be null");
        }
        return new String(content, StandardCharsets.UTF_8);
    }
}
