# 클럽 모임 게스트 이름 충돌 방지

## 목표

클럽 모임에서 게스트 이름이 활성 클럽원 이름과 같아 발생하는 참석자 식별 충돌을 사전에 막는다.

## 정책

- 관리자가 게스트를 추가하거나 게스트 이름을 수정할 때, 입력 이름이 활성 클럽원 이름과 같으면 등록을 거절한다.
- 성별과 관계없이 이름만 비교한다. 모임 안의 참석자 이름이 이미 유일해야 하기 때문이다.
- 오류 메시지는 동명이인 게스트가 구분 이름으로 다시 입력해야 함을 안내한다.
- 클럽원을 선택해 추가하거나 클럽원의 참석 상태만 변경하는 흐름은 변경하지 않는다.
- 기존에 잘못 등록된 게스트는 관리 화면에서 이름을 수정하거나 삭제해 해결한다.

## 구현 및 검증

- [x] `MeetingAttendanceCommandService`에 클럽 모임 게스트 이름과 활성 클럽원 이름의 충돌 검증을 추가했다.
- [x] 관리 게스트 추가와 게스트 수정이 모두 409 오류로 거절되는 서비스 테스트를 작성했다.
- [ ] `./gradlew.bat test --tests com.tennisfolio.Tennisfolio.meeting.service.MeetingAttendanceCommandServiceTest`로 검증한다. 사용자 실행 허가 대기.

Definition of Done: docs/workflows/development-workflow.md#definition-of-done
