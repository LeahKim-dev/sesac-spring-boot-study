package com.example.sesac_spring_boot_study;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service // Spring에게 이 클래스를 서비스 객체로 관리하라고 알려줌
public class StudyService {
    private final List<StudyMember> members = new ArrayList<>();
    // Spring Bean은 기본적으로 싱글톤 범위로 관리되기 때문에 같은 StudyService 객체를 여러 요청에서 재사용함
    
    private int capacity = 3;

    private long nextId = 1;

    public String studies(String name) {
        // 0. 이름 누락·빈 문자열·공백만 입력한 경우 거절
        if (name ==null) { return "이름을 입력해주세요.";}

        name = name.strip();

        if (name.isEmpty()) {return "이름을 입력해주세요.";}

        // 1. 기존 참가자와 이름이 중복되면 신청 거절
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
        if (confirmedCount < capacity) {
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

    public String cancel(String memberId) {
        //0. ID 누락·빈 값·숫자가 아닌 입력 처리
        if (memberId == null) {return "취소할 참가자의 ID를 입력해주세요.";}
        memberId = memberId.strip();
        if (memberId.isEmpty()) {return "취소할 참가자의 ID를 입력해주세요.";}

        long id;
        try {
            id = Long.parseLong(memberId);
        } catch (NumberFormatException e) {
            return "참가자 ID는 숫자로 입력해주세요.";
        }

        StudyMember target = null;
        // 1. 취소할 참가자 찾기
        for (StudyMember member: members) {
            if (member.getId() == id) {
                target = member;
                break;
            }
        }

        // 2. 참가자가 존재하지 않는 경우
        if (target == null) {
            return "존재하지 않는 참가자입니다.";
        }

        // 3. 참가자가 존재한다면 상태 확인
        if (target.getStatus() == StudyMember.Status.CONFIRMED) {
            // 확정자일 때: 취소하고 다음 대기자 확정
            members.remove(target);

            for (StudyMember member : members) {
                if (member.getStatus() == StudyMember.Status.WAITING) {
                    member.setStatus(StudyMember.Status.CONFIRMED);
                    return "취소 완료: " + target.getName()
                            +" / 확정 완료: " + member.getName();
                }
            }
        } else {
            // 대기자일 때: 취소
            members.remove(target);
            return "취소 완료: " + target.getName();
        }
        // 확정자 취소 + 대기자 없음
        return "취소 완료: " + target.getName();
    }

    public String changeCapacity(String capacityInput) {
       // 정원 입력값 검증
       if (capacityInput == null) {return "정원 변경 실패: 변경할 정원을 입력해주세요. / 현재 정원 " + capacity + "명";}

        capacityInput = capacityInput.strip();

        if (capacityInput.isEmpty()) {return "정원 변경 실패: 변경할 정원을 입력해주세요. / 현재 정원 " + capacity + "명";}

        int newCapacity;

        try {
            newCapacity = Integer.parseInt(capacityInput);

        } catch (NumberFormatException e) {
            return "정원 변경 실패: 정원은 정수로 입력해주세요. / 현재 정원 " + capacity + "명";
        }
        if (newCapacity <1 || newCapacity >10) {
                return "정원 변경 실패: 정원은 1명 이상 10명 이하로 입력해주세요. / 현재 정원 " + capacity + "명";
        }

        // 동일한 정원 변경 요청 시 명단 유지
        if (newCapacity == capacity) {
            return "현재 정원과 동일합니다: / 현재 정원 " + capacity + "명";
        }

        // 현재 확정 인원 확인
        int confirmedCount = 0;

        for (StudyMember member : members) {
            if (member.getStatus() == StudyMember.Status.CONFIRMED) {
                confirmedCount++;
            }
        }

        // 변경 실패
        if (newCapacity < confirmedCount) {
            return "정원 변경 실패: 현재 확정 인원보다 정원을 작게 변경할 수 없습니다. / 현재 정원 " + capacity + "명";
        }

        // 정원 변경 및 대기자 승격
        capacity = newCapacity;

        int availableSlots = newCapacity - confirmedCount;

        int promotedCount = 0;

        for (StudyMember member : members) {
            if (availableSlots == 0) {break;}
            if (member.getStatus() == StudyMember.Status.WAITING) {
                member.setStatus(StudyMember.Status.CONFIRMED);
                promotedCount++;
                availableSlots--;
            }
        } return "정원 변경 완료: 현재 정원 " + capacity + "명 "
                + "/ 이번에 확정된 대기자 " + promotedCount + "명 확정";
    }
}
