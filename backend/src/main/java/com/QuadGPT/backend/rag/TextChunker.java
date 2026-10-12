package com.QuadGPT.backend.rag;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TextChunker {

    private static final int CHUNK_SIZE = 500;

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            chunks.add(text.substring(start, end).trim());
            start = end;
        }

        return chunks;
    }
}