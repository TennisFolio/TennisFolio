# 모임 관리자 참가자 추가·수정 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 모임장이 개인 모임 게스트와 클럽 모임의 멤버·게스트를 관리 화면에서 등록하고 수정한다.

**Architecture:** 기존 `MeetingAttendance`와 정원 검증을 재사용한다. 자기 참석 응답과 분리된 `participants` command를 `MeetingAttendanceCommandService`에 두고, `MeetingManage`가 관리 화면 안의 패널을 통해 이를 호출한다.

**Tech Stack:** Spring Boot, JPA, JUnit 5/Mockito, React, Axios.

---

## File structure

- Create: `src/main/java/com/tennisfolio/Tennisfolio/meeting/dto/ManagedMeetingParticipantCreateRequest.java` — 관리자 등록 요청 DTO.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/meeting/api/MeetingController.java` — `POST /participants` route.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/meeting/service/MeetingAttendanceCommandService.java` — 권한, 참가자 해석, 저장 orchestration.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/club/repository/ClubMemberRepository.java` — 활성 클럽 멤버를 ID와 클럽으로 조회하는 query.
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/meeting/service/MeetingAttendanceCommandServiceTest.java` — command 서비스 계약 테스트.
- Modify: `src/tennisFolio/src/utils/meetingApi.js` — `addManagedParticipant` API client.
- Create: `src/tennisFolio/src/components/meeting/manage/MeetingParticipantAddPanel.jsx` — 모임 관리 화면의 추가 패널.
- Modify: `src/tennisFolio/src/page/MeetingManage.jsx` — 패널 state, 클럽 멤버 검색, 저장·새로고침 연결.
- Modify: `src/tennisFolio/src/page/Meeting.css` — 패널의 반응형 스타일.
- Create: `src/main/java/com/tennisfolio/Tennisfolio/meeting/dto/ManagedMeetingParticipantUpdateRequest.java` — 관리자 수정 요청 DTO.
- Create: `src/tennisFolio/src/components/meeting/manage/MeetingParticipantEditPanel.jsx` — 참가자 수정 바텀시트.
- Modify: `src/tennisFolio/src/components/meeting/shared/AttendanceChip.jsx` — 삭제와 공존하는 명단 선택 동작.
- Modify: `src/tennisFolio/src/components/meeting/shared/RosterPanel.jsx` — 수정 선택 handler 전달.
- Modify: `src/tennisFolio/src/components/meeting/shared/MeetingRosterSections.jsx` — 수정 선택 handler 전달.

### Task 1: 관리자 참가자 command 테스트와 API

**Files:**
- Create: `src/main/java/com/tennisfolio/Tennisfolio/meeting/dto/ManagedMeetingParticipantCreateRequest.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/meeting/api/MeetingController.java`
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/meeting/service/MeetingAttendanceCommandServiceTest.java`

- [ ] **Step 1: 실패하는 service 테스트를 추가한다**

  개인 모임장 게스트 `ATTENDING` 성공, 클럽 관리자 멤버 `WAITING` 성공, 일반 사용자·다른 클럽 멤버·중복 이름·정원 초과의 실패를 각각 검증한다. 요청은 다음 필드로 고정한다.

  ```java
  new ManagedMeetingParticipantCreateRequest(
          null, "김테니스", "MALE", "ATTENDING"
  );
  new ManagedMeetingParticipantCreateRequest(
          clubMemberId, null, null, "WAITING"
  );
  ```

- [ ] **Step 2: 해당 테스트가 실패하는 것을 확인한다**

  Run: `rtk .\\gradlew.bat test --tests com.tennisfolio.Tennisfolio.meeting.service.MeetingAttendanceCommandServiceTest`

  Expected: `ManagedMeetingParticipantCreateRequest` 및 `addManagedParticipant`가 없어 컴파일 또는 테스트 실패.

- [ ] **Step 3: 요청 DTO와 controller route를 만든다**

  ```java
  @PostMapping("/meetings/{publicId}/participants")
  public ResponseEntity<ResponseDTO<MeetingAttendanceResponse>> addManagedParticipant(
          Authentication authentication,
          @PathVariable String publicId,
          @RequestBody ManagedMeetingParticipantCreateRequest request
  ) {
      return ResponseEntity.ok(ResponseDTO.success(
              attendanceCommandService.addManagedParticipant(
                      publicId, request, resolveAuthenticatedUserId(authentication))));
  }
  ```

- [ ] **Step 4: service를 최소 구현한다**

  `addManagedParticipant`는 `findActiveMeetingForAttendanceUpdate`, `ensureAttendanceEditable`, 관리자 권한 확인, 참가자 해석, 이름 중복·정원 검증, `MeetingAttendance` 저장 순서만 orchestration한다. 클럽 멤버는 `CLUB_MEMBER`, 게스트는 `GUEST`를 저장하고 요청 상태를 그대로 사용한다.

- [ ] **Step 5: service 테스트를 통과시킨다**

  Run: `rtk .\\gradlew.bat test --tests com.tennisfolio.Tennisfolio.meeting.service.MeetingAttendanceCommandServiceTest`

  Expected: `BUILD SUCCESSFUL`.

### Task 2: 클럽 멤버 검색과 관리 화면 패널

**Files:**
- Create: `src/tennisFolio/src/components/meeting/manage/MeetingParticipantAddPanel.jsx`
- Modify: `src/tennisFolio/src/page/MeetingManage.jsx`
- Modify: `src/tennisFolio/src/utils/meetingApi.js`
- Modify: `src/tennisFolio/src/page/Meeting.css`

- [ ] **Step 1: API client를 추가한다**

  ```js
  export const addManagedParticipant = (publicId, participant) =>
    apiRequestSilent.post(`/api/meetings/${publicId}/participants`, participant);
  ```

- [ ] **Step 2: 패널 component를 만든다**

  `MeetingParticipantAddPanel`은 `isClubMeeting`, `members`, `query`, `onQueryChange`, `onSubmit`, `isSubmitting` props를 받는다. 개인 모임에서는 게스트 이름·성별·상태를, 클럽 모임에서는 탭 전환과 검색된 멤버 선택 또는 게스트 입력을 렌더링한다.

- [ ] **Step 3: 관리 화면에 연결한다**

  `MeetingManage`에서 클럽 모임일 때 `getClubMembers(clubPublicId, memberQuery)`를 호출한다. 저장 payload는 멤버면 `{ clubMemberId, attendanceStatus }`, 게스트면 `{ participantName, gender, attendanceStatus }`로 만들고 성공 후 `loadMeeting()`을 호출한다. 현재 참석자 `clubMemberId`는 선택 목록에서 제외한다.

- [ ] **Step 4: 모바일 스타일을 추가한다**

  `Meeting.css`에서 패널을 참석자 명단 전에 배치하고 1열 layout, 탭, 검색 input, 3개 상태 버튼을 휴대폰 폭에서 터치하기 쉽게 만든다. 모임 생성·수정 화면에는 import하지 않는다.

- [ ] **Step 5: 수동 검증한다**

  개인 모임에서 게스트를 각 상태로 저장하고, 클럽 모임에서 이름 검색·멤버 선택·게스트 입력을 각각 저장한다. 정원 초과와 이미 등록된 멤버의 선택 불가를 확인한다.

### Task 3: 같은 이름 게스트의 클럽원 승격

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/meeting/service/MeetingAttendanceCommandService.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/meeting/repository/MeetingAttendanceRepository.java`
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/meeting/service/MeetingAttendanceCommandServiceTest.java`

- [ ] **Step 1: 실패하는 service 테스트를 추가한다**

  클럽 관리자가 클럽원을 선택했을 때 같은 이름·성별의 게스트가 있으면 기존 참석자의 `id`와 상태를 유지한 채 `CLUB_MEMBER`로 연결되는지 검증한다. 같은 이름이지만 성별이 다른 게스트와 이미 클럽원인 참석자는 `CONFLICT`인지 검증한다.

- [ ] **Step 2: 해당 테스트가 실패하는 것을 확인한다**

  Run: `rtk .\gradlew.bat test --tests com.tennisfolio.Tennisfolio.meeting.service.MeetingAttendanceCommandServiceTest`

  Expected: 기존 중복 이름 검증 때문에 승격 성공 테스트가 실패한다.

- [ ] **Step 3: 게스트 승격 조회와 command 흐름을 구현한다**

`MeetingAttendanceRepository`에 모임·이름·성별·미삭제 기준의 참석자 조회를 추가한다. `addManagedParticipant`에서 클럽원 해석 후 같은 이름·성별의 기존 게스트가 있으면 새 엔티티를 저장하거나 정원을 다시 차감하지 않고, `assignParticipant(CLUB_MEMBER, clubMemberId)`만 수행한다. 상태는 요청값으로 바꾸지 않는다. 관리 화면은 승격 성공 시 안내 토스트를 표시한다.

- [ ] **Step 4: service 테스트를 통과시킨다**

  Run: `rtk .\gradlew.bat test --tests com.tennisfolio.Tennisfolio.meeting.service.MeetingAttendanceCommandServiceTest`

  Expected: `BUILD SUCCESSFUL`.

### Task 4: 클럽 모임 게스트 등급 저장과 표시

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/meeting/entity/MeetingAttendance.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/meeting/dto/ManagedMeetingParticipantCreateRequest.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/meeting/dto/MeetingAttendanceResponse.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/meeting/service/MeetingAttendanceCommandService.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/club/repository/ClubSkillTierRepository.java`
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/meeting/service/MeetingAttendanceCommandServiceTest.java`
- Modify: `src/tennisFolio/src/components/meeting/manage/MeetingParticipantAddPanel.jsx`
- Modify: `src/tennisFolio/src/page/MeetingManage.jsx`

- [ ] **Step 1: 실패하는 service 테스트를 추가한다**

  클럽 관리자 게스트가 해당 클럽의 현재 등급을 선택해 저장하는 성공, 개인 모임의 등급 요청·다른 클럽 등급 요청·존재하지 않는 등급 요청의 `BAD_REQUEST` 실패를 검증한다. 게스트 응답에 등급 ID와 이름이 포함되는지도 검증한다.

- [ ] **Step 2: 해당 테스트가 실패하는 것을 확인한다**

  Run: `rtk .\gradlew.bat test --tests com.tennisfolio.Tennisfolio.meeting.service.MeetingAttendanceCommandServiceTest`

  Expected: 게스트 등급 필드와 검증·응답 매핑이 없어 테스트가 실패한다.

- [ ] **Step 3: 게스트 등급을 저장하고 응답으로 매핑한다**

  `MeetingAttendance`에 선택한 클럽 등급 ID만 저장한다. 관리자 등록 요청과 응답 DTO에 등급 ID·이름을 추가하고, 상세 조회 시 등급 ID로 현재 이름을 매핑한다. 클럽 모임 게스트의 등급이 해당 클럽 소속인지 확인하며, 개인 모임 게스트는 등급 없이 유지한다.

- [ ] **Step 4: 관리 바텀시트에 등급 선택을 연결한다**

  클럽 모임의 게스트 탭에서 등급 선택을 노출하고 `{ participantName, gender, attendanceStatus, clubSkillTierId }`를 전송한다. 개인 모임과 클럽원 탭에는 등급 입력을 노출하지 않는다. 명단 칩에는 게스트 등급 이름을 표시한다.

- [ ] **Step 5: service 테스트를 통과시킨다**

  Run: `rtk .\gradlew.bat test --tests com.tennisfolio.Tennisfolio.meeting.service.MeetingAttendanceCommandServiceTest`

  Expected: `BUILD SUCCESSFUL`.

### Task 5: 관리자 참가자 수정 command와 API

**Files:**
- Create: `src/main/java/com/tennisfolio/Tennisfolio/meeting/dto/ManagedMeetingParticipantUpdateRequest.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/meeting/api/MeetingController.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/meeting/service/MeetingAttendanceCommandService.java`
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/meeting/service/MeetingAttendanceCommandServiceTest.java`

- [x] **Step 1: 실패하는 service 테스트를 추가한다**

  게스트의 이름·성별·상태·등급 수정 성공과 클럽원의 상태 수정 성공을 검증한다. 권한 없음, 이름 중복, `ATTENDING` 정원 초과, 개인 모임 등급 요청, 클럽원의 이름·성별·등급 변경 요청은 실패를 검증한다.

- [x] **Step 2: DTO, controller route, service를 구현한다**

  `PATCH /api/meetings/{publicId}/participants/{attendanceId}`는 인증 사용자 ID와 `ManagedMeetingParticipantUpdateRequest`를 `updateManagedParticipant`로 전달한다. 게스트에는 identity·등급·정원·중복 규칙을, 클럽원에는 불변 필드 거절과 상태 갱신만 적용한다.

- [x] **Step 3: service 테스트를 통과시킨다**

  Run: `rtk .\\gradlew.bat test --tests com.tennisfolio.Tennisfolio.meeting.service.MeetingAttendanceCommandServiceTest`

  Expected: `BUILD SUCCESSFUL` 및 수정 응답의 현재 등급 이름 확인.

### Task 6: 명단 선택과 참가자 수정 바텀시트

**Files:**
- Create: `src/tennisFolio/src/components/meeting/manage/MeetingParticipantEditPanel.jsx`
- Modify: `src/tennisFolio/src/utils/meetingApi.js`
- Modify: `src/tennisFolio/src/components/meeting/shared/AttendanceChip.jsx`
- Modify: `src/tennisFolio/src/components/meeting/shared/RosterPanel.jsx`
- Modify: `src/tennisFolio/src/components/meeting/shared/MeetingRosterSections.jsx`
- Modify: `src/tennisFolio/src/page/MeetingManage.jsx`

- [x] **Step 1: 수정 API client와 바텀시트를 만든다**

  `updateManagedParticipant(publicId, attendanceId, participant)`는 `PATCH /api/meetings/{publicId}/participants/{attendanceId}`를 호출한다. 게스트 시트는 이름·성별·클럽 등급·상태를 초기값으로 편집하고, 클럽원 시트는 identity·등급을 읽기 전용으로 표시하며 상태만 변경한다.

- [x] **Step 2: 명단 선택을 수정 흐름에 연결한다**

  `AttendanceChip`은 관리 명단에서 삭제 버튼과 중첩된 `button` 없이 클릭·키보드 선택을 지원한다. 삭제 클릭은 선택으로 전파하지 않고, `MeetingManage`는 모임장 본인을 제외한 선택 참가자와 수정 시트를 관리한다.

- [x] **Step 3: 저장과 안내를 연결한다**

  저장 성공 시 상세 정보를 다시 읽고 시트를 닫은 뒤 `참가자 정보를 수정했습니다.`를 표시한다. 참가자 추가 아래에는 `참가자를 선택하면 정보를 수정할 수 있어요.`를 표시한다.

- [ ] **Step 4: 모바일 화면을 수동 확인한다**

  프런트엔드 테스트·빌드는 사용자 요청이 없으므로 실행하지 않고, 게스트 전체 수정·클럽원 상태 수정·삭제 동작을 수동 확인한다.

### Task 7: 회귀 확인과 문서 갱신

**Files:**
- Modify: `docs/features/meeting-managed-participants.md`
- Modify: `docs/superpowers/specs/2026-07-31-meeting-managed-participants.md`
- Modify: `docs/superpowers/plans/2026-07-31-meeting-managed-participants.md`

- [x] **Step 1: backend 테스트를 실행한다**

  Run: `rtk .\\gradlew.bat test --tests com.tennisfolio.Tennisfolio.meeting.service.MeetingAttendanceCommandServiceTest --tests com.tennisfolio.Tennisfolio.meeting.api.MeetingControllerTest`

  Expected: `BUILD SUCCESSFUL`.

- [x] **Step 2: 문서의 체크박스와 Verification Log를 갱신한다**

  실제 추가·검증한 항목만 완료 처리하고, 프런트엔드 build/test는 프로젝트 규칙상 사용자 요청이 없으므로 실행하지 않았음을 남긴다.

- [x] **Step 3: 변경 범위를 점검한다**

  Run: `rtk git diff --check`

  Expected: 출력 없음.

## Self-review

- Spec의 개인/클럽 권한, 세 상태, 정원 규칙, 검색, 인라인 관리 화면 요구는 Task 1~2에 각각 대응한다.
- 일괄 등록·자동 승급·알림은 작업에 포함하지 않았다.
- DTO, service method, API client의 이름은 모두 `addManagedParticipant`로 통일했다.
