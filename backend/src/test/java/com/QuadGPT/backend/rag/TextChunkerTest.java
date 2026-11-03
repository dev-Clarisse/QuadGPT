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

    @Test
    void sentencesAreNeverCutInTheMiddle() {
        String sentence = "Chez Quadria, chaque salarié a droit à trente jours de congés payés par an. ";
        String text = sentence.repeat(20);

        List<String> chunks = chunker.chunk(text);

        assertTrue(chunks.size() > 1);
        for (String chunk : chunks) {
            assertTrue(chunk.length() <= 500);
            assertTrue(chunk.startsWith("Chez Quadria"));
            assertTrue(chunk.endsWith("par an."));
        }
    }

    @Test
    void lastSentenceIsRepeatedAtTheStartOfTheNextChunk() {
        String first = "Phrase un. " + "a".repeat(400) + ". ";
        String overlap = "La phrase de liaison.";
        String text = first + overlap + " " + "b".repeat(400) + ".";

        List<String> chunks = chunker.chunk(text);

        assertEquals(2, chunks.size());
        assertTrue(chunks.get(0).endsWith(overlap));
        assertTrue(chunks.get(1).startsWith(overlap));
    }

    @Test
    void pdfLineBreaksAreJoined() {
        List<String> chunks = chunker.chunk("Une phrase coupée\npar la mise en page.\n\n");

        assertEquals(List.of("Une phrase coupée par la mise en page."), chunks);
    }

}
