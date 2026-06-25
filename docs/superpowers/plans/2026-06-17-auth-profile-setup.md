# 로그인 후 프로필 설정 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 소셜 로그인 후 이름 또는 성별이 비어 있는 사용자가 이름과 성별을 필수로 입력하도록 만든다.

**Architecture:** 백엔드는 `GET /api/auth/me`에 `gender`, `needsProfileSetup`을 포함하고 `PATCH /api/auth/profile`로 프로필을 저장한다. 프론트는 `needsProfileSetup=true`일 때 닫을 수 없는 하단 시트를 표시하고 저장 성공 후 사용자 상태를 갱신한다.

**Tech Stack:** Spring Boot 3, Spring MVC, Spring Security, JPA, JUnit 5, Mockito, React 19, Vite, axios

**Commit Policy:** 사용자 요청이 있기 전까지 커밋하지 않는다.

---

## 파일 구조

- Modify: `src/main/java/com/tennisfolio/Tennisfolio/user/domain/User.java`
  - `gender` 필드와 프로필 변경 메서드를 추가한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/user/repository/UserEntity.java`
  - `tb_user.GENDER` 컬럼 매핑을 추가한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/user/dto/AuthMeResponse.java`
  - `gender`, `needsProfileSetup`을 추가한다.
- Create: `src/main/java/com/tennisfolio/Tennisfolio/user/dto/AuthProfileUpdateRequest.java`
  - 이름과 성별 입력 요청 DTO.
- Create: `src/main/java/com/tennisfolio/Tennisfolio/user/service/AuthProfileService.java`
  - 프로필 저장, 검증, 응답 생성을 담당한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/user/service/AuthQueryService.java`
  - `AuthMeResponse` 생성 시 새 필드가 포함되도록 유지한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/user/api/AuthController.java`
  - `PATCH /api/auth/profile` 추가.
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/user/service/AuthQueryServiceTest.java`
  - `needsProfileSetup` 계산 검증.
- Create: `src/test/java/com/tennisfolio/Tennisfolio/user/service/AuthProfileServiceTest.java`
  - 프로필 저장과 검증 실패 케이스 테스트.
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/user/api/AuthControllerTest.java`
  - 프로필 저장 API가 서비스로 위임되는지 테스트.
- Modify: `src/tennisFolio/src/utils/authApi.js`
  - `updateProfile` API 추가.
- Modify: `src/tennisFolio/src/App.jsx`
  - 프로필 설정 완료 시 `currentUser` 갱신.
- Create: `src/tennisFolio/src/components/auth/ProfileSetupSheet.jsx`
  - 이름/성별 필수 입력 하단 시트.
- Create: `src/tennisFolio/src/components/auth/ProfileSetupSheet.css`
  - 모바일 하단 시트 스타일.

---

### Task 1: User 모델과 AuthMeResponse에 gender/needsProfileSetup 추가

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/user/domain/User.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/user/repository/UserEntity.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/user/dto/AuthMeResponse.java`
- Test: `src/test/java/com/tennisfolio/Tennisfolio/user/service/AuthQueryServiceTest.java`

- [ ] **Step 1: 실패 테스트 작성**

`AuthQueryServiceTest`에 다음 케이스를 추가한다.

```java
@Test
void getCurrentUser_marksProfileSetupNeededWhenNicknameMissing() {
    User user = User.builder()
            .userId(1L)
            .email("user@test.com")
            .nickName(null)
            .gender(User.Gender.MALE)
            .status(UserStatus.ACTIVE)
            .build();
    when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE))
            .thenReturn(Optional.of(user));

    AuthMeResponse response = authQueryService.getCurrentUser(1L);

    assertThat(response.isNeedsProfileSetup()).isTrue();
}

@Test
void getCurrentUser_marksProfileSetupNeededWhenGenderMissing() {
    User user = User.builder()
            .userId(1L)
            .email("user@test.com")
            .nickName("민수")
            .gender(null)
            .status(UserStatus.ACTIVE)
            .build();
    when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE))
            .thenReturn(Optional.of(user));

    AuthMeResponse response = authQueryService.getCurrentUser(1L);

    assertThat(response.isNeedsProfileSetup()).isTrue();
}

@Test
void getCurrentUser_marksProfileSetupCompleteWhenNicknameAndGenderExist() {
    User user = User.builder()
            .userId(1L)
            .email("user@test.com")
            .nickName("민수")
            .gender(User.Gender.MALE)
            .status(UserStatus.ACTIVE)
            .build();
    when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE))
            .thenReturn(Optional.of(user));

    AuthMeResponse response = authQueryService.getCurrentUser(1L);

    assertThat(response.getGender()).isEqualTo("MALE");
    assertThat(response.isNeedsProfileSetup()).isFalse();
}
```

- [ ] **Step 2: 실패 확인**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.user.service.AuthQueryServiceTest"
```

Expected: `User.Gender`, `getGender`, `isNeedsProfileSetup`이 없어 실패한다.

- [ ] **Step 3: User 모델 수정**

`User`에 enum과 필드를 추가한다.

```java
public enum Gender {
    MALE, FEMALE
}

private Gender gender;

public User updateProfile(String nickName, Gender gender) {
    return User.builder()
            .userId(this.userId)
            .email(this.email)
            .nickName(nickName)
            .gender(gender)
            .status(this.status)
            .build();
}
```

- [ ] **Step 4: UserEntity 매핑 수정**

`UserEntity`에 `GENDER` 컬럼을 추가한다.

```java
@Enumerated(EnumType.STRING)
@Column(name = "GENDER")
private User.Gender gender;
```

`fromModel`, `toModel`에 `gender`를 포함한다.

- [ ] **Step 5: AuthMeResponse 수정**

`AuthMeResponse`에 `gender`, `needsProfileSetup`을 추가한다.

```java
private String gender;
private boolean needsProfileSetup;
```

`from(User user)`는 다음 규칙으로 만든다.

```java
String gender = user.getGender() == null ? null : user.getGender().name();
boolean needsProfileSetup =
        user.getNickName() == null ||
        user.getNickName().trim().isEmpty() ||
        user.getGender() == null;
```

- [ ] **Step 6: 테스트 통과 확인**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.user.service.AuthQueryServiceTest"
```

Expected: `BUILD SUCCESSFUL`.

---

### Task 2: 프로필 저장 서비스 구현

**Files:**
- Create: `src/main/java/com/tennisfolio/Tennisfolio/user/dto/AuthProfileUpdateRequest.java`
- Create: `src/main/java/com/tennisfolio/Tennisfolio/user/service/AuthProfileService.java`
- Test: `src/test/java/com/tennisfolio/Tennisfolio/user/service/AuthProfileServiceTest.java`

- [ ] **Step 1: 실패 테스트 작성**

`AuthProfileServiceTest`를 만든다.

```java
package com.tennisfolio.Tennisfolio.user.service;

import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.dto.AuthMeResponse;
import com.tennisfolio.Tennisfolio.user.dto.AuthProfileUpdateRequest;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthProfileServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AuthProfileService authProfileService;

    @Test
    void updateProfile_savesTrimmedNameAndGender() {
        User user = User.builder()
                .userId(1L)
                .email("user@test.com")
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthMeResponse response = authProfileService.updateProfile(
                1L,
                new AuthProfileUpdateRequest(" 민수 ", "MALE")
        );

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getNickName()).isEqualTo("민수");
        assertThat(captor.getValue().getGender()).isEqualTo(User.Gender.MALE);
        assertThat(response.isNeedsProfileSetup()).isFalse();
    }

    @Test
    void updateProfile_rejectsBlankName() {
        assertThatThrownBy(() -> authProfileService.updateProfile(
                1L,
                new AuthProfileUpdateRequest(" ", "MALE")
        )).isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("400");
    }

    @Test
    void updateProfile_rejectsNameLongerThanTen() {
        assertThatThrownBy(() -> authProfileService.updateProfile(
                1L,
                new AuthProfileUpdateRequest("12345678901", "MALE")
        )).isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("400");
    }

    @Test
    void updateProfile_rejectsInvalidGender() {
        assertThatThrownBy(() -> authProfileService.updateProfile(
                1L,
                new AuthProfileUpdateRequest("민수", "UNKNOWN")
        )).isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("400");
    }
}
```

- [ ] **Step 2: 실패 확인**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.user.service.AuthProfileServiceTest"
```

Expected: 서비스와 요청 DTO가 없어 실패한다.

- [ ] **Step 3: 요청 DTO 생성**

```java
package com.tennisfolio.Tennisfolio.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthProfileUpdateRequest {
    private String nickName;
    private String gender;
}
```

- [ ] **Step 4: 서비스 구현**

`AuthProfileService`를 만든다.

```java
@Service
public class AuthProfileService {
    private static final int MAX_NICKNAME_LENGTH = 10;

    private final UserRepository userRepository;

    public AuthProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthMeResponse updateProfile(Long userId, AuthProfileUpdateRequest request) {
        String nickName = validateNickName(request.getNickName());
        User.Gender gender = parseGender(request.getGender());
        User user = userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        User saved = userRepository.save(user.updateProfile(nickName, gender));
        return AuthMeResponse.from(saved);
    }

    private String validateNickName(String nickName) {
        String trimmed = nickName == null ? "" : nickName.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_NICKNAME_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must be 1 to 10 characters");
        }
        return trimmed;
    }

    private User.Gender parseGender(String gender) {
        try {
            return User.Gender.valueOf(gender);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gender must be MALE or FEMALE");
        }
    }
}
```

- [ ] **Step 5: 테스트 통과 확인**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.user.service.AuthProfileServiceTest"
```

Expected: `BUILD SUCCESSFUL`.

---

### Task 3: PATCH /api/auth/profile 추가

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/user/api/AuthController.java`
- Test: `src/test/java/com/tennisfolio/Tennisfolio/user/api/AuthControllerTest.java`

- [ ] **Step 1: 실패 테스트 작성**

`AuthControllerTest`에 다음 테스트를 추가한다.

```java
@Mock
AuthProfileService authProfileService;

@Test
void updateProfile_returnsUpdatedCurrentUser() {
    Authentication authentication =
            new UsernamePasswordAuthenticationToken(1L, null, List.of());
    AuthProfileUpdateRequest request =
            new AuthProfileUpdateRequest("민수", "MALE");
    when(authProfileService.updateProfile(1L, request))
            .thenReturn(new AuthMeResponse(1L, "user@test.com", "민수", "MALE", false));

    ResponseEntity<ResponseDTO<AuthMeResponse>> response =
            authController.updateProfile(authentication, request);

    verify(authProfileService).updateProfile(1L, request);
    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody().getData().getNickName()).isEqualTo("민수");
}
```

- [ ] **Step 2: 실패 확인**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.user.api.AuthControllerTest"
```

Expected: `updateProfile` 메서드가 없어 실패한다.

- [ ] **Step 3: 컨트롤러 구현**

`AuthProfileService`를 주입하고 메서드를 추가한다.

```java
@PatchMapping("/profile")
public ResponseEntity<ResponseDTO<AuthMeResponse>> updateProfile(
        Authentication authentication,
        @RequestBody AuthProfileUpdateRequest request
) {
    Long userId = (Long) authentication.getPrincipal();
    AuthMeResponse response = authProfileService.updateProfile(userId, request);
    return ResponseEntity.ok(ResponseDTO.success(response));
}
```

- [ ] **Step 4: SecurityConfig 확인**

`/api/auth/profile`은 인증이 필요하다.

```java
.requestMatchers("/api/auth/me", "/api/auth/profile").authenticated()
```

- [ ] **Step 5: 테스트 통과 확인**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.user.api.AuthControllerTest"
```

Expected: `BUILD SUCCESSFUL`.

---

### Task 4: 프론트 프로필 설정 API와 하단 시트 추가

**Files:**
- Modify: `src/tennisFolio/src/utils/authApi.js`
- Create: `src/tennisFolio/src/components/auth/ProfileSetupSheet.jsx`
- Create: `src/tennisFolio/src/components/auth/ProfileSetupSheet.css`
- Modify: `src/tennisFolio/src/App.jsx`

- [ ] **Step 1: authApi에 updateProfile 추가**

```javascript
export const updateProfile = (profile) =>
  apiRequestSilent.patch('/api/auth/profile', profile);
```

- [ ] **Step 2: ProfileSetupSheet 컴포넌트 생성**

`ProfileSetupSheet.jsx`를 만든다.

```jsx
import { useMemo, useState } from 'react';
import './ProfileSetupSheet.css';

function ProfileSetupSheet({ onSubmit }) {
  const [nickName, setNickName] = useState('');
  const [gender, setGender] = useState('');
  const trimmedName = nickName.trim();
  const canSubmit = useMemo(
    () => trimmedName.length >= 1 && trimmedName.length <= 10 && Boolean(gender),
    [trimmedName, gender]
  );

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!canSubmit) return;
    await onSubmit({ nickName: trimmedName, gender });
  };

  return (
    <div className="profile-setup-backdrop">
      <form className="profile-setup-sheet" onSubmit={handleSubmit}>
        <h2>프로필 설정</h2>
        <label className="profile-field">
          <span>이름</span>
          <input
            value={nickName}
            maxLength={10}
            onChange={(event) => setNickName(event.target.value)}
          />
        </label>
        <div className="profile-field">
          <span>성별</span>
          <div className="gender-options">
            <button type="button" className={gender === 'MALE' ? 'selected' : ''} onClick={() => setGender('MALE')}>남성</button>
            <button type="button" className={gender === 'FEMALE' ? 'selected' : ''} onClick={() => setGender('FEMALE')}>여성</button>
          </div>
        </div>
        <button type="submit" className="profile-submit" disabled={!canSubmit}>
          저장
        </button>
      </form>
    </div>
  );
}

export default ProfileSetupSheet;
```

- [ ] **Step 3: ProfileSetupSheet CSS 생성**

모바일 하단 시트와 데스크톱 중앙 모달 스타일을 만든다. 버튼은 8px 이하 radius를 유지한다.

- [ ] **Step 4: App에 연결**

`App.jsx`에서 `ProfileSetupSheet`와 `updateProfile`을 import한다.

```javascript
import ProfileSetupSheet from './components/auth/ProfileSetupSheet';
import { getCurrentUser, updateProfile } from './utils/authApi';
```

`App` 내부에 저장 핸들러를 추가한다.

```javascript
const handleProfileSubmit = async (profile) => {
  const response = await updateProfile(profile);
  setCurrentUser(response.data.data);
};
```

`Layout` 아래 또는 `Routes` 아래에 조건부 렌더링한다.

```jsx
{currentUser?.needsProfileSetup && (
  <ProfileSetupSheet onSubmit={handleProfileSubmit} />
)}
```

- [ ] **Step 5: 수정 파일 lint 확인**

Run:

```powershell
npx.cmd eslint src/App.jsx src/utils/authApi.js src/components/auth/ProfileSetupSheet.jsx
```

Workdir:

```text
src/tennisFolio
```

Expected: exit code 0.

---

### Task 5: 전체 검증

**Files:**
- No direct edits.

- [ ] **Step 1: 백엔드 관련 테스트**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.user.service.AuthQueryServiceTest" --tests "com.tennisfolio.Tennisfolio.user.service.AuthProfileServiceTest" --tests "com.tennisfolio.Tennisfolio.user.api.AuthControllerTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: 백엔드 컴파일**

Run:

```powershell
.\gradlew.bat compileJava
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: 프론트 수정 파일 lint**

Run:

```powershell
npx.cmd eslint src/App.jsx src/Layout.jsx src/utils/authApi.js src/components/auth/ProfileSetupSheet.jsx
```

Workdir:

```text
src/tennisFolio
```

Expected: exit code 0.

- [ ] **Step 4: 수동 확인**

1. 닉네임 또는 성별이 없는 계정으로 로그인한다.
2. 로그인 후 프로필 설정 시트가 뜨는지 확인한다.
3. 이름 없이 저장할 수 없는지 확인한다.
4. 11자 이름을 입력할 수 없거나 저장할 수 없는지 확인한다.
5. 성별 미선택 상태에서 저장할 수 없는지 확인한다.
6. 저장 후 시트가 닫히고 `내 계정` 시트에서 이메일이 보이는지 확인한다.

---

## 자체 검토

- 이름은 사용자 표시 라벨로 쓰고 API는 `nickName`을 유지한다.
- 이름 정책은 1~10자, 중복 허용이다.
- 성별은 필수이며 `MALE`, `FEMALE`만 허용한다.
- 닫을 수 없는 프로필 설정 하단 시트로 로그인 직후 필수 입력을 보장한다.
- 컨트롤러는 repository를 직접 호출하지 않고 서비스만 호출한다.
