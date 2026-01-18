package uk.ac.ed.acp.cw2.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class FrontendController {

    @GetMapping("/")
    public String index() throws IOException {
        ClassPathResource htmlFile = new ClassPathResource("frontend/index.html");
        return StreamUtils.copyToString(htmlFile.getInputStream(), StandardCharsets.UTF_8);
    }

    @GetMapping(value = "/script.js", produces = MediaType.APPLICATION_JSON_VALUE)
    public String script() throws IOException {
        ClassPathResource jsFile = new ClassPathResource("frontend/script.js");
        return StreamUtils.copyToString(jsFile.getInputStream(), StandardCharsets.UTF_8);
    }
}