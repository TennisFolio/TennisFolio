# 모임 관리자 참가자 추가 Spec

## 목표

모임 관리자가 참가자의 직접 응답 없이 참가자를 명단에 등록한다. 개인 모임은 게스트만, 클럽 모임은 활성 클럽 멤버 또는 게스트를 등록한다.

## 범위

- 생성·수정 화면이나 별도 URL을 추가하지 않는다.
- 개인 모임의 `/meetings/{publicId}/manage`와 클럽 모임의 관리 화면에서 참석자 명단 바로 위에 인라인 패널을 제공한다.
- `POST /api/meetings/{publicId}/participants`는 기존 자기 참석 응답 `POST /attendances`와 분리한다.

## API 계약

```json
{
  "clubMemberId": 12,
  "participantName": null,
  "gender": null,
  "attendanceStatus": "ATTENDING"
}
```

- 클럽 멤버는 `clubMemberId`와 `attendanceStatus`를 보낸다. 서버가 이름·성별·참가자 유형을 결정한다.
- 게스트는 `participantName`, `gender`, `attendanceStatus`를 보낸다.
- 상태는 `ATTENDING`, `WAITING`, `NOT_ATTENDING`만 허용한다.
- 응답은 기존 `MeetingAttendanceResponse`를 사용한다.

## 권한과 검증

- 개인 모임은 모임장만 게스트를 추가할 수 있다.
- 클럽 모임은 해당 클럽의 활성 `ADMIN`만 멤버·게스트를 추가할 수 있다.
- 다른 클럽 멤버, 비활성 멤버, 개인 모임의 `clubMemberId`는 거절한다.
- 이름 중복, 참석 마감, 대진표 생성 후 변경은 거절한다.
- `ATTENDING`만 전체 또는 성별 정원을 확인한다. `WAITING`과 `NOT_ATTENDING`은 정원을 소비하지 않는다.

## UI

- 개인 모임: 게스트 이름·성별·상태 입력과 `참가자 추가` 버튼.
- 클럽 모임: `클럽 멤버`/`게스트 직접 입력` 탭. 멤버 탭은 이름 검색을 제공하며 이미 명단에 등록된 멤버는 선택 불가다.
- 성공하면 상세 모임을 다시 읽어 명단과 정원 표시를 갱신하고, 실패 메시지는 패널 근처에 표시한다.

## 비범위

- 여러 명 일괄 등록, 대기 자동 승급, 참석자 직접 알림은 구현하지 않는다.

## 참고

- 기능 문서: `docs/features/meeting-managed-participants.md`
- 모바일 목업: `docs/features/meeting/meeting-managed-participants-mockup.html`
