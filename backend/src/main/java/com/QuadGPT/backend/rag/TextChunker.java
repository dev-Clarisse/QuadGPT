package com.QuadGPT.backend.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class TextChunker {

    private static final int CHUNK_SIZE = 500;
    // Max length of the previous chunk's last sentence repeated at the start of the next one.
    private static final int OVERLAP_SIZE = 150;

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern SENTENCE_END = Pattern.compile("(?<=[.!?])\\s+");

    // Packs whole sentences into chunks so that no sentence is cut in the middle.
    public List<String> chunk(String text) {
        // PDF extraction breaks lines visually, so line breaks carry no meaning.
        String normalized = WHITESPACE.matcher(text).replaceAll(" ").trim();
        List<String> chunks = new ArrayList<>();
        if (normalized.isEmpty()) {
            return chunks;
        }

        StringBuilder current = new StringBuilder();
        String lastSentence = null;

        for (String sentence : splitIntoSentences(normalized)) {
            if (current.length() > 0 && current.length() + 1 + sentence.length() > CHUNK_SIZE) {
                chunks.add(current.toString());
                current.setLength(0);

                if (lastSentence != null
                        && lastSentence.length() <= OVERLAP_SIZE
                        && lastSentence.length() + 1 + sentence.length() <= CHUNK_SIZE) {
                    current.append(lastSentence);
                }
            }

            if (current.length() > 0) {
                current.append(' ');
            }
            current.append(sentence);
            lastSentence = sentence;
        }

        chunks.add(current.toString());
        return chunks;
    }

    private List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();

        for (String sentence : SENTENCE_END.split(text)) {
            // A sentence longer than a chunk is cut into fixed-size pieces as a last resort.
            for (int start = 0; start < sentence.length(); start += CHUNK_SIZE) {
                int end = Math.min(start + CHUNK_SIZE, sentence.length());
                String piece = sentence.substring(start, end).trim();
                if (!piece.isEmpty()) {
                    sentences.add(piece);
                }
            }
        }

        return sentences;
    }
}
