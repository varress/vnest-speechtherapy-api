package fi.vnest.speechtherapy.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WordController {

    // TODO: Fix me, this is just for environment setup testing
    @GetMapping(value = "/words", produces = "application/json")
    public List<String> getWords(){
        return List.of("one", "two", "tree");
    }

}
