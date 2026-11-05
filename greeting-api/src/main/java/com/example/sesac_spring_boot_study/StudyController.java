package com.example.sesac_spring_boot_study;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController // 이 클래스는 http요청을 받아서 응답하는 controller
public class StudyController {
    private final StudyService service;
    // Controller는 HTTP요청을 받는 역할
    // 실제 신청이나 취소 처리는 Service가 담당
    // Controller가 Service의 메서드를 호출하려면 StudyService객체를 참조할 방법이 필요
    // service : 해서 참조를 저장하는 필드
    public StudyController(StudyService service) {
        this.service = service;
    }

    // 명단 조회
    @GetMapping("/study/participants") // 브라우저에서 주소로 GET요청을 보내면 아래 메서드를 실행하라
    public String getParticipants(String name){
        return service.getParticipants(); // StudyService의 메서드를 호줄 -> Service가 반환한 문자열을 Controller에서도 반환
    }

}
