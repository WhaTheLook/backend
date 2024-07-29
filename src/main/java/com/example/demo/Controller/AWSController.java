package com.example.demo.Controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name="AWS 컨트롤러", description = "AWS 전용 컨트롤러")
public class AWSController {

    @GetMapping("/healthcheck")
    public String healthcheck() {
        return "OK";
    }
}
