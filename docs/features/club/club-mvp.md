# Feature: Club MVP

## 1. Goal

로그인 사용자가 클럽을 만들고, 내 클럽 목록과 클럽원 명단을 관리할 수 있게 한다. 1차 범위는 Club과 ClubMember 관리까지만 포함하며 Meeting 연결, 참석자 확장, 계정 연결 API는 후속 단계로 미룬다.

## 2. User Flow

```text
로그인 사용자 -> 클럽 생성 -> 생성자가 ADMIN 클럽원으로 저장됨
로그인 사용자 -> 내 클럽 목록 조회 -> userId가 연결된 활성 클럽만 표시됨
클럽 ADMIN -> 클럽 상세/수정 -> 클럽 기본 정보 관리
클럽 ADMIN -> 클럽원 추가/수정/삭제 -> 수동 명단 관리
```

## 3. Spec

- 기준 설계: `docs/superpowers/specs/2026-07-06-club-api-design.md`
- 포함 API: `GET/POST /api/clubs`, `GET/PATCH /api/clubs/{clubPublicId}`, `GET/POST /api/clubs/{clubPublicId}/members`, `PATCH/DELETE /api/clubs/{clubPublicId}/members/{memberId}`
- 신규 도메인: `Club`, `ClubMember`, `ClubMemberRole`
- 생성자는 `ClubMember.userId`를 현재 인증 사용자 ID로 저장하고, `role=ADMIN`, `active=true`로 자동 등록한다.
- 운영자가 추가하는 클럽원은 1차에서 `userId=null`로 저장한다.
- 같은 클럽 안의 활성 클럽원 이름은 중복 불가다.
- 마지막 `ADMIN` 삭제 또는 `MEMBER` 강등은 `409 Conflict`로 거부한다.
- Club API는 로그인 필수이며, 조회는 활성 클럽원, 변경은 `ADMIN`만 허용한다.

## 4. Design Review

### Responsibility

- Controller / API: `src/main/java/com/tennisfolio/Tennisfolio/club/api/ClubController.java`
- Service / Application: `ClubCommandService`, `ClubQueryService`, `ClubMemberCommandService`, `ClubAccessService`
- Domain: `Club`, `ClubMember`, `ClubMemberRole`
- Repository / Persistence: `ClubRepository`, `ClubMemberRepository`
- Security / External: `Authentication.getPrincipal()`의 `Long userId` 사용
- Frontend / UI: 1차 API 구현 계획에는 포함하지 않음

### Review Questions

- Club MVP는 Meeting 변경 없이 독립적으로 동작한다.
- 권한 판정은 `ClubAccessService`에 모아 서비스별 중복을 줄인다.
- `ClubMember.active=false`로 삭제해 후속 Meeting/Attendance 연결에서 참조 안정성을 유지한다.
- 1차에서는 사용자 검색과 계정 연결 API를 만들지 않아 개인정보 정책 결정을 미룬다.

## 5. Plan / Commit Units

- [x] `feat: add club domain model` - done, verified, committed
  - 구현: `club/entity`에 `Club`, `ClubMember`, `ClubMemberRole` 추가, `club/repository`에 JPA repository 추가
  - 테스트 설명: repository 테스트로 Club 저장, ADMIN 클럽원 저장, 활성 클럽원 이름 조회를 검증
  - 검증 기준: `.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.repository.ClubRepositoryTest`

- [x] `feat: add club command services` - done, verified
  - 구현: `ClubCommandService`, `ClubMemberCommandService`, `ClubAccessService`에서 생성/수정/삭제/권한/중복/마지막 ADMIN 규칙 구현
  - 테스트 설명: 생성자 ADMIN 자동 등록, 비로그인 거부, 중복 이름 거부, 마지막 ADMIN 삭제/강등 거부, 일반 멤버의 관리자 작업 거부를 service 테스트로 검증
  - 검증 기준: `.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.service.*`

- [x] `feat: add club query services` - done, verified
  - 구현: `ClubQueryService`에서 내 클럽 목록, 클럽 상세, 클럽원 목록과 keyword 검색 구현
  - 테스트 설명: 로그인 사용자에게 연결된 활성 클럽만 반환, 클럽 멤버가 아닌 사용자의 상세 조회 거부, keyword 검색 결과 필터링을 검증
  - 검증 기준: `.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.service.ClubQueryServiceTest`

- [ ] `feat: expose club api`
  - 구현: `ClubController`와 request/response DTO 추가, `SecurityConfig`에 `/api/clubs/**` 인증 요구 추가
  - 테스트 설명: controller 또는 MVC 테스트로 인증 사용자 ID 전달, JSON 응답 shape, 비로그인 401, 권한 실패 403/409 전달을 검증
  - 검증 기준: `.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.api.ClubControllerTest --tests com.tennisfolio.Tennisfolio.config.ClubSecurityConfigTest`

- [ ] `docs: align club mvp spec`
  - 구현: 구현 중 바뀐 API 필드명이나 정책이 있으면 `docs/superpowers/specs/2026-07-06-club-api-design.md`와 이 문서를 함께 갱신
  - 테스트 설명: 문서와 controller DTO 필드명이 일치하는지 수동 검토
  - 검증 기준: `git diff -- docs/features/club-mvp.md docs/superpowers/specs/2026-07-06-club-api-design.md`

## 6. Development Validation

- [ ] 성공 흐름이 동작한다
- [ ] 실패 흐름이 동작한다
- [ ] 권한, 인증, 토큰, 세션 등 보안 조건이 검증됐다
- [ ] DB 저장, 수정, 삭제 결과가 의도와 일치한다
- [ ] 프론트엔드와 백엔드 계약이 일치한다
- [ ] 관련 단위 테스트 또는 통합 테스트를 실행했다
- [ ] spec, plan, code가 서로 어긋나지 않는다

## 7. Final QA Checklist

- [ ] 사용자가 처음부터 끝까지 기능을 완료할 수 있다
- [ ] 실패 시 사용자가 다음 행동을 알 수 있다
- [ ] 새 기능이 기존 핵심 흐름을 깨지 않는다
- [ ] 테스트 결과와 수동 검증 결과를 PR에 설명할 수 있다

## 8. Change Log

| Date | Change | Reason |
|---|---|---|
| 2026-07-06 | Club MVP 1차 구현 계획 작성 | Club 전체 범위를 단계화하고 1차 API 개발 범위 확정 |
