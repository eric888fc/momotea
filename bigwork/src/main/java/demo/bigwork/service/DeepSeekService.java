
package demo.bigwork.service;

import reactor.core.publisher.Mono;

public interface DeepSeekService {

    /**
     * Send a question to DeepSeek AI and return the response
     * @param question The user message
     * @return AI response as Mono<String>
     */
    Mono<String> ask(String question);

	String search(String query);
}
