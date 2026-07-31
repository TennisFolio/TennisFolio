# Feature: 모임 관리자 참가자 추가

## 1. Goal

모임 관리자는 참가자가 직접 응답하지 않아도 클럽 멤버 또는 게스트를 명단에 추가할 수 있다. 개인 모임은 게스트 입력을, 클럽 모임은 클럽 멤버 선택과 게스트 입력을 모두 지원한다.

## 2. User Flow

```text
관리 화면에서 참가자 추가
-> 개인 모임은 게스트 입력, 클럽 모임은 멤버 선택 또는 게스트 입력
-> 참석·대기·불참 상태 선택 후 저장
-> 권한·중복·정원·모임 상태 검증
-> 참가자 명단과 정원 표시 갱신
```

## 3. Spec

- 관리자 전용 API는 `POST /api/meetings/{publicId}/participants`로 둔다. 기존 `attendances` API는 참가자 본인의 응답 용도로 유지한다.
- 개인 모임에서는 모임장만 게스트를 등록할 수 있다. 게스트 요청에는 `participantName`, `gender`, `attendanceStatus`가 필요하다.
- 클럽 모임에서는 활성 클럽 관리자만 등록할 수 있다. 클럽 멤버 요청에는 `clubMemberId`, `attendanceStatus`가 필요하며 서버가 멤버의 이름·성별을 사용한다. 게스트 요청도 가능하다.
- `attendanceStatus`는 `ATTENDING`, `WAITING`, `NOT_ATTENDING`을 허용한다. 정원 검증은 `ATTENDING`일 때만 적용한다.
- 같은 이름의 신규 게스트 등록, 비활성 또는 다른 클럽의 멤버, 참석 마감, 대진표 생성 후 변경은 거절한다. 단, 클럽원이 같은 이름·성별의 기존 게스트와 일치하면 기존 게스트 참석자를 클럽원으로 승격한다.
- 모임 생성·수정 화면에는 참가자 추가를 넣지 않는다. 별도 참가자 추가 화면이나 URL도 만들지 않고, 모임 생성 후 `/meetings/{publicId}/manage`의 참석자 명단 바로 위에 인라인 패널을 둔다. 개인 모임에서는 게스트 입력만 보이고, 클럽 모임에서는 클럽 멤버 선택과 게스트 입력을 전환해 제공한다. 클럽 멤버 탭은 이름 검색으로 활성 멤버 목록을 좁히며, 이미 등록된 멤버는 선택할 수 없다. 성공하면 상세 정보를 다시 불러온다. 게스트가 클럽원으로 승격된 경우에는 이를 토스트로 알린다. 화면안은 `docs/features/meeting/meeting-managed-participants-mockup.html`을 따른다.

## 4. Design Review

### Responsibility

- Controller / API: `participants` 요청과 인증 사용자 ID를 서비스에 전달한다.
- Service / Application: 모임 유형별 관리자 권한을 확인하고, 참가자 해석·규칙 검증·저장을 순서대로 수행한다.
- Domain: `MeetingAttendance`에 클럽 멤버 또는 게스트의 참가자 유형과 선택한 상태를 저장한다.
- Repository / Persistence: 기존 참석자·클럽 멤버 조회 및 정원 집계 repository를 재사용한다.
- Security: 개인 모임은 `ownerUserId`, 클럽 모임은 활성 `ADMIN` 역할로 제한한다.
- Frontend / UI: `MeetingManage`에 참가자 추가 패널과 클럽 멤버 이름 검색을 구성하고 클럽 멤버 목록 API를 사용한다.

### Decisions

- 자기 응답과 관리자 등록은 권한과 참가자 해석이 다르므로 API와 서비스 진입점을 분리한다.
- 별도 테이블이나 배치 등록 API는 만들지 않는다. 한 명씩 등록해 기존 삭제·대진표 생성 흐름과 일관되게 유지한다.
- 계정이 연결된 클럽 멤버도 관리자 선택으로 등록할 수 있으며, 이는 본인의 참석 응답을 생성하지 않는다.
- 클럽원 승격은 기존 참석자의 `id`와 참석 상태를 유지하고 `participantType`만 `CLUB_MEMBER`로, `clubMemberId`만 선택한 멤버로 연결한다. 같은 이름이지만 성별이 다르거나 이미 클럽원으로 연결된 참석자는 중복으로 거절한다.

## 5. Plan / Commit Units

- [ ] `feat: add managed meeting participant API`
  - 구현: 요청 DTO, controller route, 서비스 권한·참가자 해석·저장
  - 테스트: 개인 모임장/클럽 관리자 성공, 권한·중복·정원·마감 실패
  - 검증: 관련 `MeetingAttendanceCommandService` 테스트

- [ ] `feat: add managed participant UI`
  - 구현: 관리 화면 추가 패널, 클럽 멤버 이름 검색·선택, 게스트 입력, API 연동
  - 테스트: 프런트엔드 테스트는 작성하지 않음
  - 검증: 수동으로 개인/클럽 모임의 등록·상태별 명단 갱신 확인

- [ ] `fix: promote matching guest to club member`
  - 구현: 클럽원 선택 시 같은 이름·성별의 게스트 참석자를 클럽원으로 연결
  - 테스트: 승격 성공과 상태 유지, 성별 불일치·기존 클럽원 중복 거절
  - 검증: 관련 `MeetingAttendanceCommandService` 테스트

## 6. Development Validation

- [ ] 개인 모임장이 게스트를 각 상태로 등록할 수 있다
- [ ] 클럽 관리자가 멤버와 게스트를 각 상태로 등록할 수 있다
- [ ] 권한·중복·정원·마감·대진표 생성 후 제한이 적용된다
- [ ] 참가자 유형과 상태가 상세 응답 및 화면 명단에 반영된다
- [ ] 같은 이름·성별의 게스트를 클럽원 선택으로 승격하면 참석 상태가 유지된다
- [ ] spec, plan, code가 서로 어긋나지 않는다

## 7. Final QA Checklist

- [ ] 개인/클럽 모임에서 처음부터 참가자 추가를 완료할 수 있다
- [ ] 실패 메시지로 다음 행동을 알 수 있다
- [ ] 기존 자기 참석 응답과 관리자 삭제 기능이 깨지지 않는다
- [ ] 테스트 및 수동 검증 결과를 설명할 수 있다

## 8. Change Log

| Date | Change | Reason |
|---|---|---|
| 2026-07-31 | 초기 작성 | 관리자 참가자 추가 기능 설계 |
| 2026-07-31 | 게스트-클럽원 승격 규칙 추가 | 게스트를 먼저 등록한 뒤 클럽원을 선택해도 중복으로 막히지 않게 함 |
