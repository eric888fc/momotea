package demo.bigwork.service.Impl;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import demo.bigwork.service.DeepSeekService;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
public class DeepSeekServiceImpl implements DeepSeekService {

    private final WebClient webClient;

    public DeepSeekServiceImpl() throws Exception {
        // Read API key from a file (make sure this path is correct)
        String apiKey = Files.readString(Path.of("C:\\JavaWeb_Workspace\\deepseek.key")).trim();

        this.webClient = WebClient.builder()
                .baseUrl("https://api.deepseek.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public Mono<String> ask(String question) {
        Map<String, Object> body = Map.of(
                "model", "deepseek-chat",
                "messages", List.of(Map.of("role", "user", "content", question))
        );

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    return (String) message.get("content");
                });
    }

	@Override
	public String search(String query) {
		// TODO Auto-generated method stub
		return null;
	}
}
