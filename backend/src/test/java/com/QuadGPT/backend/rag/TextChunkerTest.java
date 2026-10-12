package com.QuadGPT.backend.rag;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class TextChunkerTest {

    private final TextChunker chunker = new TextChunker();

    @Test
    void shortTextProducesOneChunk() {
        List<String> chunks = chunker.chunk("Un texte court.");

        assertEquals(1, chunks.size());
        assertEquals("Un texte court.", chunks.get(0));
    }

    @Test
    void longTextIsSplitIntoMultipleChunks() {
        String longText = "a".repeat(1200);

        List<String> chunks = chunker.chunk(longText);

        assertEquals(3, chunks.size());
        assertTrue(chunks.get(0).length() <= 500);
    }

    @Test
    void emptyTextProducesNoChunks() {
        List<String> chunks = chunker.chunk("");

        assertTrue(chunks.isEmpty());
    }

}
