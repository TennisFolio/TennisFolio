# Competition 삭제 설계

## 목표

로그인 사용자가 `내 경기` 화면에서 자신이 만든 Competition을 삭제할 수 있게 한다.

삭제된 Competition은 DB에는 보존하지만 서비스에서는 존재하지 않는 경기처럼 취급한다. 공유 링크, 상세 화면, 결과 화면, 참가자/게임 운영 API에서 더 이상 접근할 수 없어야 한다.

## 범위

이번 범위에 포함한다.

- 로그인 사용자가 본인 소유 Competition을 삭제한다.
- 삭제된 Competition은 `내 경기` 목록에서 제외한다.
- 삭제된 Competition은 `publicId` 기반 상세/결과/운영 API에서 조회되지 않는다.
- 삭제 확인 UI는 되돌릴 수 없는 액션임을 명확히 알린다.

이번 범위에서 제외한다.

- 사용자용 복구 기능
- 보관함 화면
- 관리자용 복구 기능
- 관리자 토큰만 가진 비로그인 사용자의 Competition 삭제
- 하위 테이블 row의 물리 삭제

## 삭제 모델

`tb_competition`에 nullable 삭제 시각 컬럼을 추가한다.

```text
tb_competition
- ...
- DEL_DT nullable datetime
```

Java 엔티티에서는 DB 네이밍과 도메인 네이밍을 분리한다.

```java
@Column(name = "DEL_DT")
private LocalDateTime deletedAt;
```

의미는 다음과 같다.

- `deletedAt == null`: 활성 Competition
- `deletedAt != null`: 삭제된 Competition

DB 컬럼은 기존 `CRT_DT`, `UPD_DT` 스타일에 맞춰 `DEL_DT`를 사용한다. Java 필드는 의미가 자연스러운 `deletedAt`을 사용한다.

하위 테이블인 `tb_competition_entry`, `tb_competition_stat`, `tb_game`, `tb_game_entry`에는 이번 단계에서 삭제 컬럼을 추가하지 않는다. `Competition`이 aggregate root이고, 하위 데이터는 Competition을 통해서만 접근 가능한 종속 데이터이기 때문이다.

## 삭제 API

로그인 사용자가 본인 소유 Competition을 삭제한다.

```http
DELETE /api/auth/me/competitions/{publicId}
```

인증이 필요하다. 현재 인증된 사용자 id와 `Competition.ownerUserId`가 일치할 때만 삭제할 수 있다.

응답은 성공 시 `204 No Content`를 반환한다. 삭제 API는 반환할 본문이 없고, 같은 인증 컨트롤러의 로그아웃 API도 본문 없는 성공 응답을 사용하므로 이 흐름과 맞춘다.

권한 규칙은 다음과 같다.

- 비로그인 요청: `401`
- 다른 사용자의 Competition 삭제 요청: `404`
- 존재하지 않는 `publicId`: `404`
- 이미 삭제된 Competition 재삭제 요청: 멱등성을 위해 성공 처리한다.

다른 사용자의 Competition에 대해 `403` 대신 `404`를 사용해 리소스 존재 여부를 노출하지 않는다.

## 조회 차단

삭제된 Competition은 일반 조회 경로에서 없는 리소스로 취급한다.

Repository에는 활성 Competition 조회 메서드를 추가한다.

```java
Optional<Competition> findByPublicIdAndDeletedAtIsNull(String publicId);
```

`publicId` 기반 공개/운영 API는 기존 `findByPublicId(publicId)` 대신 활성 조회를 사용한다.

적용 대상은 다음과 같다.

- `GET /api/competitions/{publicId}`
- `GET /api/competitions/{publicId}/result`
- `GET /api/competitions/{publicId}/entries`
- `POST /api/competitions/{publicId}/entries`
- `PATCH /api/competitions/{publicId}/entries/{entryId}`
- `POST /api/competitions/{publicId}/admin-password`
- `POST /api/competitions/{publicId}/admin-login`
- `PATCH /api/competitions/{publicId}`
- `PATCH /api/competitions/{publicId}/court-count`
- `PATCH /api/competitions/{publicId}/games/{gameId}/entries`
- `POST /api/competitions/{publicId}/courts/{court}/games`
- `PATCH /api/competitions/{publicId}/games/{gameId}/status`
- `DELETE /api/competitions/{publicId}/games/{gameId}`
- `PATCH /api/competitions/{publicId}/games/{gameId}/score`

삭제된 Competition에 접근하면 `NotFoundException(ExceptionCode.NOT_FOUND)`를 사용해 기존 없는 데이터와 동일하게 처리한다.

## 내 경기 목록

`GET /api/auth/me/competitions`는 삭제되지 않은 Competition만 반환한다.

Repository 조회 조건은 다음 의미를 가져야 한다.

```text
OWNER_USER_ID = currentUserId
AND DEL_DT IS NULL
ORDER BY CRT_DT DESC, COMPETITION_ID DESC
```

응답 DTO는 변경하지 않는다.

## 프론트엔드 동작

`MyCompetitions` 목록 항목에 삭제 버튼을 추가한다. 항목 전체가 상세 이동 버튼으로 구현되어 있으므로 삭제 버튼 클릭 시 상세 이동 이벤트가 함께 발생하지 않도록 이벤트 전파를 막아야 한다.

삭제 전 확인 문구는 삭제의 영향을 명확히 알린다.

```text
삭제하면 공유 링크와 경기 운영 화면에 더 이상 접근할 수 없습니다. 삭제할까요?
```

삭제 성공 후에는 목록에서 해당 Competition을 즉시 제거한다.

성공 메시지는 다음 정도로 짧게 표시한다.

```text
경기를 삭제했어요.
```

공유 링크나 결과 링크로 접근한 사용자는 기존 없는 데이터 화면을 본다. 공개 화면에서 "삭제된 경기입니다"라고 별도로 표시하지 않는다. 삭제된 리소스가 과거에 존재했다는 사실을 불필요하게 노출하지 않기 위해서다.

Competition UI 스타일을 수정할 때는 `docs/competition-design-system.md`와 `src/tennisFolio/src/styles/competition-theme.css`의 CSS 변수를 따른다. 삭제 버튼은 danger 계열 변수를 사용한다.

## 테스트 범위

백엔드 테스트는 다음을 검증한다.

- 로그인 사용자는 본인 소유 Competition을 삭제할 수 있다.
- 삭제 시 `Competition.deletedAt`이 채워진다.
- 다른 사용자 소유 Competition 삭제 요청은 `404`로 처리한다.
- 삭제된 Competition은 `GET /api/auth/me/competitions`에서 제외된다.
- 삭제된 Competition은 상세 조회에서 `404`로 처리된다.
- 삭제된 Competition은 결과 조회에서 `404`로 처리된다.
- 삭제된 Competition은 관리자 비밀번호 설정/로그인에서 `404`로 처리된다.
- 삭제된 Competition은 참가자/게임 운영 API에서 `404`로 처리된다.

프론트엔드 테스트는 다음을 검증한다.

- 내 경기 목록에서 삭제 버튼이 보인다.
- 삭제 확인 후 API를 호출한다.
- 삭제 성공 시 해당 항목이 목록에서 제거된다.
- 삭제 실패 시 오류 메시지를 표시한다.

## 구현 메모

`findByPublicId(publicId)` 호출이 여러 서비스에 흩어져 있으므로, 구현 시 삭제 차단 누락이 생기지 않도록 활성 Competition 조회를 공통 메서드로 감싼다.

예시는 다음과 같다.

```java
private Competition findActiveCompetition(String publicId) {
    return competitionRepository.findByPublicIdAndDeletedAtIsNull(publicId)
            .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
}
```

삭제 API는 활성 조회가 아니라 소유자 기준 조회를 사용한다. 이미 삭제된 Competition을 다시 삭제하는 요청도 성공 처리하기 위해서다.

```java
Optional<Competition> findByPublicIdAndOwnerUserId(String publicId, Long ownerUserId);
```

삭제 메서드는 이미 삭제된 경우 `deletedAt`을 덮어쓰지 않는다.

```java
public void delete(LocalDateTime deletedAt) {
    if (this.deletedAt == null) {
        this.deletedAt = deletedAt;
    }
}
```

## 비고

하위 데이터의 삭제 시간이 필요해지는 경우에는 별도 요구사항으로 다룬다. 예를 들어 특정 참가자 삭제 이력, 특정 Game 삭제 이력, 관리자 복구 도구가 필요해지면 각 하위 aggregate 또는 운영 요구에 맞춰 별도 컬럼을 추가한다.
