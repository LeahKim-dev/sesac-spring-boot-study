package com.example.sesac_spring_boot_study;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {
    private final GreetingService service;
    public GreetingController(GreetingService service) {
        this.service = service;
    }

    @GetMapping("/greet")
    public String greet(
            @RequestParam(name = "name", defaultValue = "방문자") String name) {
        return service.greet(name);
    }
    // 브라우저: GET /greet?name=민지
    // @GetMapping: /greet 요청을 처리할 메서드 선택
    // @RequestParam: name 매개변수에 "민지"전달
    // greet("민지") 실행: service.greet(name) 호출
}
