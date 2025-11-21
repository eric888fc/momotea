package demo.bigwork.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import demo.bigwork.service.DeepSeekService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ai")
public class DeepSeekController {

    private final DeepSeekService deepSeekService;

    
    public DeepSeekController(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    @GetMapping("/search")
    public Mono<String> search(@RequestParam("q") String query) {
        return deepSeekService.ask(query);
    }
    
}
