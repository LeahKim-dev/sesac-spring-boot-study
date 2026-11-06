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
    // service : 참조를 저장하는 필드

    // 의존성 주입(DI): 필요한 객체를 외부(Spring)에서 전달받는 것
    // @Service가 붙은 StudyService 클래스를 바탕으로
    // Spring이 객체(Bean)를 생성하고 관리하며,
    // StudyController의 생성자에 그 객체를 전달한다.
    public StudyController(StudyService service) {
        this.service = service; // Controller의 필드에 전달받은 객체의 참조를 저장
    }

    // 참가자 신청
    @PostMapping("/study/participants") // POST 방식으로 /study/participants에 요청이 들어오면 아래 메서드를 실행하라
    public String studies(@RequestParam(name = "name", required = false) String name) {
        return service.studies(name);
    }
    // POST /study/participants : name = "민지"
    // StudyController : @RequestParam으로 name받기, service.studies(name)호출
    // StudyService : studies("민지") 실행 -> 참가자 등록후 결과 문자열 리턴
    // 신청완료

    // 명단 조회
    @GetMapping("/study/participants") // 브라우저에서 주소로 GET요청을 보내면 아래 메서드를 실행하라
    public String getParticipants(){
        return service.getParticipants(); // StudyService의 메서드를 호줄 -> Service가 반환한 문자열을 Controller에서도 반환
    }
    // /study/participants 접속
    // 브라우저: GET /study/participants 요청
    // Spring Boot: 요청 주소에 맞는 Controller 메서드 호출
    // StudyController: service.getParticipants() 호출
    // StudyService: 참가자 조회 후 결과 반환

}
