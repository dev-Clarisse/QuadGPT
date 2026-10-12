package com.QuadGPT.backend.llm;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestLlmController {

    private final LlmClientService llmClientService;

    public TestLlmController(LlmClientService llmClientService) {
        this.llmClientService = llmClientService;
    }

    @PostMapping("/chat")
    public String testChat(@RequestBody String message) {
        return llmClientService.chat(message);
    }
}