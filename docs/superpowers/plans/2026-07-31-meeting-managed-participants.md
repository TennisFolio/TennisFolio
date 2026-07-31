# 모임 관리자 참가자 추가 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 모임장이 개인 모임 게스트와 클럽 모임의 멤버·게스트를 관리 화면에서 참가자로 등록한다.

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

### Task 3: 회귀 확인과 문서 갱신

**Files:**
- Modify: `docs/features/meeting-managed-participants.md`
- Modify: `docs/superpowers/specs/2026-07-31-meeting-managed-participants.md`
- Modify: `docs/superpowers/plans/2026-07-31-meeting-managed-participants.md`

- [ ] **Step 1: backend 테스트를 실행한다**

  Run: `rtk .\\gradlew.bat test --tests com.tennisfolio.Tennisfolio.meeting.service.MeetingAttendanceCommandServiceTest --tests com.tennisfolio.Tennisfolio.meeting.api.MeetingControllerTest`

  Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: 문서의 체크박스와 Verification Log를 갱신한다**

  실제 추가·검증한 항목만 완료 처리하고, 프런트엔드 build/test는 프로젝트 규칙상 사용자 요청이 없으므로 실행하지 않았음을 남긴다.

- [ ] **Step 3: 변경 범위를 점검한다**

  Run: `rtk git diff --check`

  Expected: 출력 없음.

## Self-review

- Spec의 개인/클럽 권한, 세 상태, 정원 규칙, 검색, 인라인 관리 화면 요구는 Task 1~2에 각각 대응한다.
- 일괄 등록·자동 승급·알림은 작업에 포함하지 않았다.
- DTO, service method, API client의 이름은 모두 `addManagedParticipant`로 통일했다.
