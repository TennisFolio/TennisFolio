# 클럽 BackOffice 대시보드 설계

## 목적과 범위

클럽 `ADMIN`이 자기 클럽의 구성, 활동, 참여 상태를 빠르게 판단하는 데스크톱 전용 BackOffice 대시보드를 제공한다.

- 첫 릴리스는 대시보드 한 화면만 제공한다.
- 기간은 요청 시점에서 직전 30일로 고정한다.
- 게임, 대진, 승패, 개인 순위와 상세 분석 화면은 범위에서 제외한다.
- 목업 기준: `docs/features/club/club-backoffice-dashboard-mockup.html`

## 사용자와 권한

- 로그인한 활성 `ClubMember` 중 `role=ADMIN`만 조회할 수 있다.
- `ClubAccessService.requireAdmin`으로 서버에서 클럽 소속과 권한을 확인한다.
- 비로그인은 `401`, 비멤버 또는 일반 멤버는 `403`, 삭제됐거나 없는 클럽은 `404`를 반환한다.
- 응답은 선택한 클럽에만 한정하며 다른 클럽 데이터는 포함하지 않는다.

## 화면과 경로

- 프런트 경로: `/clubs/:clubPublicId/dashboard`
- 데스크톱 좌측 메뉴: 대시보드만 활성화한다. 멤버 현황·모임 활동·참여 현황은 1차에서 링크나 화면을 만들지 않는다.
- 상단에는 ADMIN이 운영하는 클럽만 선택 가능한 드롭다운을 둔다. 선택하면 해당 클럽 경로로 이동한다.
- 제목은 `최근 30일 운영 요약`이며 기간 변경 UI는 제공하지 않는다.

## API

`GET /api/clubs/{clubPublicId}/dashboard`

응답은 기존 `ResponseDTO` 성공 형식을 사용한다.

- 프런트는 대시보드 진입 시 이 API만 한 번 호출한다.
- 서버는 권한, 멤버 구성, 모임, 참석, 최근 활동을 목적별 집계 쿼리로 조회해 하나의 응답으로 조립한다. 서로 다른 집계를 한 개의 거대한 SQL로 결합하지 않는다.

```json
{
  "period": { "from": "2026-07-13", "to": "2026-08-11" },
  "activeMemberCount": 42,
  "meetingCount": 8,
  "cancelledMeetingCount": 1,
  "memberParticipation": { "participantCount": 31, "rate": 73, "attendanceCount": 58, "inactiveParticipantCount": 11 },
  "guestAttendanceCount": 12,
  "averageAttendancePerMeeting": 8.8,
  "memberComposition": {
    "genderCounts": [{ "gender": "MALE", "count": 24 }, { "gender": "FEMALE", "count": 18 }],
    "skillTierCounts": [{ "skillTierId": 1, "name": "중급 B", "level": 2, "count": 24 }],
    "unclassifiedSkillMemberCount": 3
  },
  "recentMeetings": [{ "publicId": "...", "startAt": "2026-08-10T09:00:00", "title": "일요 복식 정모", "status": "OPEN", "memberAttendanceCount": 14, "guestAttendanceCount": 2 }]
}
```

## 집계 규칙

- 기간은 서버의 한국 표준시 기준 오늘을 포함한 최근 30일이며, 모임 `startAt`이 이 범위에 있는 클럽 모임만 활동·참여 집계에 사용한다.
- 삭제된 모임과 삭제된 참석은 항상 제외한다.
- `meetingCount`는 `CANCELLED`가 아닌 기간 내 모임 수다. `cancelledMeetingCount`는 같은 기간의 취소 모임 수다.
- 참석 건수는 `attendanceStatus=ATTENDING`만 포함한다.
- 멤버 참석은 `participantType=CLUB_MEMBER`이고 `clubMemberId`가 있는 참석이며, 게스트 참석은 나머지 참석으로 계산한다.
- `participantCount`는 기간 중 한 번 이상 참석한 고유 클럽 멤버 수다. `rate`는 활성 멤버 수가 0이면 `0`, 그 외에는 `participantCount / activeMemberCount * 100`을 반올림한 정수다.
- `inactiveParticipantCount`는 활성 멤버 수에서 `participantCount`를 뺀 값이다.
- `averageAttendancePerMeeting`는 취소되지 않은 기간 내 모임이 0이면 `0`, 그 외에는 멤버·게스트 참석 건수 합계를 `meetingCount`로 나눈 소수 첫째 자리다.
- 성별과 실력 등급 분포는 기간과 무관하게 현재 활성 멤버를 기준으로 한다.
- 실력 등급 막대는 `ClubSkillTier.level` 오름차순으로 표시하고 클럽이 설정한 `name`을 그대로 사용한다. 등급이 없는 활성 멤버는 `미분류`로 별도 표시한다.
- 최근 활동은 기간 내 모임을 `startAt` 내림차순으로 최대 3건 반환한다. 각 항목의 참석 수는 위의 ATTENDING 규칙을 따른다.

## 구현 경계

- API: `club/api`에 BackOffice 조회 엔드포인트와 응답 DTO를 둔다.
- 애플리케이션: `ClubBackofficeQueryService`가 권한 확인, 기간 계산, 조회 결과 조립을 담당한다.
- 영속성: `ClubMemberRepository`, `MeetingRepository`, `MeetingAttendanceRepository`에 집계 전용 조회를 추가한다. 목록을 전부 메모리에서 순회하지 않고 DB 집계와 필요한 최근 모임 조회를 사용한다. 성능 이슈가 확인되기 전에는 별도 집계 테이블이나 캐시를 도입하지 않는다.
- 프런트: `page/club`에 대시보드 페이지를 두고, 섹션 UI와 포맷팅은 `components/club/dashboard`로 분리한다. 기존 클럽 API 유틸리티에 대시보드 조회를 추가한다.
- UI 스타일은 기존 Club 화면의 구조와 프로젝트 스타일 방식을 따른다. 목업의 숫자는 예시이며 API 응답으로 대체한다.

## 상태 처리

- 운영 클럽이 없으면 대시보드 대신 클럽 생성 또는 클럽 목록으로 이동할 수 있는 빈 상태를 표시한다.
- 선택한 클럽에 데이터가 없으면 모든 수치는 `0`, 성별·실력 막대와 최근 활동은 빈 상태로 표시한다.
- 조회 실패 시 기존 클럽 화면과 같은 오류 메시지와 재시도 동작을 제공한다.

## 검증 기준

- ADMIN은 자기 클럽의 30일 요약만 조회한다.
- 일반 멤버와 타 클럽 ADMIN은 조회할 수 없다.
- 취소·삭제 모임과 삭제 참석이 집계에서 올바르게 제외된다.
- 멤버·게스트 참석, 고유 참여 멤버, 평균 참석, 미분류 실력 등급이 규칙대로 계산된다.
- 프런트는 로딩, 빈 상태, 권한/조회 실패, 정상 데이터를 구분해 표시한다.

Definition of Done: `docs/workflows/development-workflow.md#definition-of-done`
