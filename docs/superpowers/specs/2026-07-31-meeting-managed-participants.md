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
- 클럽 모임의 게스트는 해당 클럽의 활성 등급에서 `clubSkillTierId`를 선택할 수 있다. 개인 모임 게스트는 등급을 선택하거나 저장하지 않는다.
- 상태는 `ATTENDING`, `WAITING`, `NOT_ATTENDING`만 허용한다.
- 응답은 기존 `MeetingAttendanceResponse`를 사용한다.

## 권한과 검증

- 개인 모임은 모임장만 게스트를 추가할 수 있다.
- 클럽 모임은 해당 클럽의 활성 `ADMIN`만 멤버·게스트를 추가할 수 있다.
- 다른 클럽 멤버, 비활성 멤버, 개인 모임의 `clubMemberId`는 거절한다.
- 같은 이름의 신규 게스트 등록, 참석 마감, 대진표 생성 후 변경은 거절한다.
- 클럽원을 선택할 때 같은 이름·성별의 기존 게스트 참석자가 있으면 새 참석자를 만들지 않는다. 해당 참석자의 `participantType`을 `CLUB_MEMBER`로 바꾸고 선택한 `clubMemberId`를 연결하며, 기존 `id`와 참석 상태는 유지한다.
- 같은 이름이라도 성별이 다르거나, 같은 이름의 참석자가 이미 `CLUB_MEMBER`이면 중복으로 거절한다.
- `ATTENDING`만 전체 또는 성별 정원을 확인한다. `WAITING`과 `NOT_ATTENDING`은 정원을 소비하지 않는다.
- 게스트가 보낸 `clubSkillTierId`는 해당 클럽 소속의 활성 등급인지 검증한다. 다른 클럽의 등급, 존재하지 않는 등급, 개인 모임의 등급 요청은 거절한다.

## UI

- 개인 모임: 게스트 이름·성별·상태 입력과 `참가자 추가` 버튼.
- 클럽 모임: `클럽 멤버`/`게스트 직접 입력` 탭. 멤버 탭은 이름 검색을 제공하며 이미 명단에 등록된 멤버는 선택 불가다. 게스트 탭은 클럽 등급 선택을 제공한다.
- 응답과 명단에는 게스트의 등급 이름을 함께 제공·표시한다. 클럽원은 기존 클럽 멤버 등급을 사용한다.
- 성공하면 상세 모임을 다시 읽어 명단과 정원 표시를 갱신하고, 실패 메시지는 패널 근처에 표시한다.

## 비범위

- 여러 명 일괄 등록, 대기 자동 승급, 참석자 직접 알림은 구현하지 않는다.

## 참고

- 기능 문서: `docs/features/meeting-managed-participants.md`
- 모바일 목업: `docs/features/meeting/meeting-managed-participants-mockup.html`
