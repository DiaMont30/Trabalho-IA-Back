package com.plataforma.conversacional.parsing;

public interface DocumentParser {

    String parse(byte[] content, String contentType);
}
