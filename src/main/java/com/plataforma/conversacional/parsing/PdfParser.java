package com.plataforma.conversacional.parsing;

import com.plataforma.conversacional.exception.ParsingException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class PdfParser implements DocumentParser {

    @Override
    public String parse(byte[] content, String contentType) {
        if (content == null) {
            throw new ParsingException("Content must not be null");
        }

        try (PDDocument document = Loader.loadPDF(content)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new ParsingException("Failed to parse PDF content", e);
        }
    }
}
