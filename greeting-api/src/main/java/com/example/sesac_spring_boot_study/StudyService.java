package com.example.sesac_spring_boot_study;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service // Spring에게 이 클래스를 서비스 객체로 관리하라고 알려줌
public class StudyService {
    private final List<StudyMember> members = new ArrayList<>();
    // Spring Bean은 기본적으로 싱글톤 범위로 관리되기 때문에 같은 StudyService 객체를 여러 요청에서 재사용함

    private long nextId = 1;

    public String studies(String name) {
        // 0. 공백 제거
        if (name ==null) { return "이름을 입력해주세요.";}

        name = name.strip();

        //공백을 제거한 결과가 빈문자열이면
        if (name.isEmpty()) {return "이름을 입력해주세요.";}

        // 1. 중복 거절
        for (StudyMember member: members) {
            if (member.getName().equals(name)) {
                return "이미 신청한 이름입니다.";
            }
        }

        // 2. 기존 확정자 수 계산
        int confirmedCount = 0;

        for (StudyMember member : members) {
            if (member.getStatus() == StudyMember.Status.CONFIRMED) {
                confirmedCount++;
            }
        }

        // 3. 새로운 참가자 상태 결정
        StudyMember.Status status;
        if (confirmedCount < 3) {
            status = StudyMember.Status.CONFIRMED;
        } else {
            status = StudyMember.Status.WAITING;
        }

        // 4. 새로운 참가자 생성
        StudyMember member = new StudyMember(nextId, name, status);
        nextId++;
        members.add(member);
        return "신청완료: "+  member.getId() + "/"
                + member.getName() + "/"
                + member.getStatus();
    }

    public String getParticipants() {
        if (members.isEmpty()) {
            return "참가자가 없습니다.";
        }
        StringBuilder stringBuilder = new StringBuilder();

        for (StudyMember member: members) {
            String memberInfo = member.getId() + "/"
                    + member.getName() + "/"
                    + member.getStatus();
            stringBuilder.append(memberInfo);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
