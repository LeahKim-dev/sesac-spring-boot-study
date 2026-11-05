package com.example.sesac_spring_boot_study;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service // Spring에게 이 클래스를 서비스 객체로 관리하라고 알려줌
public class StudyService {
    private final List<StudyMember> members = new ArrayList<>();
    private long nextId = 1;

    public String studies(String name) {
        // 1. 기존 확정자 수 계산
        int confirmedCount = 0;

        for (StudyMember member : members) {
            if (member.getStatus() == StudyMember.Status.CONFIRMED) {
                confirmedCount++;
            }
        }
        // 2. 새로운 참가자 상태 결정
        StudyMember.Status status;
        if (confirmedCount < 3) {
            status = StudyMember.Status.CONFIRMED;
        } else {
            status = StudyMember.Status.WAITING;
        }
        // 3. 새로운 참가자 생성
        StudyMember member = new StudyMember(nextId, name, status);
        nextId++;
        members.add(member);
        return "신청완료: "+ status;
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
