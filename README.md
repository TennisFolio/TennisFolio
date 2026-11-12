# TennisFolio

테니스 클럽의 모임과 경기 운영을 관리하는 웹 서비스입니다.

기존에는 ATP/WTA 경기 정보 제공을 목적으로 개발했지만, 현재는 테니스 클럽 운영에 필요한 기능을 중심으로 개발하고 있습니다.

**URL:** https://tennisfolio.net

## 주요 기능

### 1. 클럽 관리
- 테니스 클럽 생성 및 관리
- 클럽별 회원 관리
- 클럽 단위로 모임과 경기 정보 관리

### 2. 모임 관리
- 클럽 내 테니스 모임 생성 및 관리
- 모임별 참가 회원 관리
- 모임과 대진표 연계

### 3. 대진표 자동 생성
- 참가 인원, 코트 수, 경기 시간에 따른 대진표 생성
- 남자복식, 여자복식, 혼합복식 등 경기 유형 설정
- 참가자별 경기 횟수 및 휴식 균등 배분
- 동일 파트너와 상대방의 중복 최소화
- 참가자 실력 등급을 고려한 대진 구성
- 경기별 점수 입력 및 결과 관리

### 4. BackOffice
- 클럽 및 모임 운영 현황 조회
- 모임 참여 기록을 기반으로 회원별 참여율 확인
- 대진표와 모임 데이터를 활용한 회원 활동 현황 조회

### 5. 테니스 경기 정보
- ATP/WTA 경기 일정 및 결과 조회
- 선수 랭킹 및 대회 정보 제공
- 실시간 경기 정보 조회

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| Backend | Java 17, Spring Boot, JPA, Spring Security |
| Frontend | React, Vite |
| Database / Cache | MariaDB, Redis |
| Infra | AWS EC2, S3, CloudFront, Route 53 |
| Deploy | Docker, Nginx, GitHub Actions |
| Monitoring | Prometheus, Grafana |

## 아키텍처

<img width="1752" height="897" alt="TennisFolio_architecture" src="https://github.com/user-attachments/assets/d184ae52-09b3-4525-ac39-c9065dfe8f03" />


React 빌드 파일은 S3에 저장하고 CloudFront로 제공합니다. 브라우저에서 `api.tennisfolio.net`으로 요청하면 EC2에서 실행 중인 Nginx를 거쳐 Spring Boot로 전달됩니다.

EC2의 Docker 환경에서 Spring Boot, Nginx, MariaDB, Redis, Prometheus, Grafana를 실행하고 있습니다. Redis는 경기 데이터 캐싱과 Refresh Token 저장에 사용합니다. Prometheus로 메트릭을 수집하고 Grafana에서 확인합니다.

## 개발하면서 개선한 부분

### 경기 정보 조회 캐싱

처음에는 경기 정보를 조회할 때 외부 API를 직접 호출했습니다. 응답이 느렸고, 요청이 몰리면 API 호출 제한에 걸리는 문제가 있었습니다.

외부 데이터를 주기적으로 가져와 Redis에 저장하고 조회 API에서는 캐시를 사용하도록 변경했습니다.

- 평균 응답 시간: **1.35초 → 80ms**
- JMeter 테스트 중 Rate Limit 오류 발생률: **36.6% → 0%**

### 데이터 수집 작업 분리

외부 API 호출과 DB 저장을 순차적으로 처리할 때는 약 8,000번의 API 요청과 6만 건의 데이터 처리에 4시간이 걸렸습니다.

수집과 저장을 Producer–Consumer 구조로 분리해 병렬로 처리하도록 변경했습니다.

- 수집 및 저장 시간: **4시간 → 30분**

### 외부 API 재시도

데이터 수집 중 HTTP 429 응답으로 작업이 중단되는 경우가 있어 Exponential Backoff를 적용했습니다. 요청 제한이 발생했을 때 대기 후 재시도하도록 처리했습니다.

### 대진표 생성

참가 인원과 코트 수, 경기 시간을 기준으로 전체 경기 수를 계산하고 대진표를 생성합니다.

대진표를 생성할 때는 다음 조건을 고려합니다.

- 참가자별 경기 횟수와 휴식 횟수 균등 배분
- 동일 파트너 및 상대방 중복 최소화
- 남자복식, 여자복식, 혼합복식 등 경기 유형별 참가자 배정
- 참가자 실력 등급을 고려한 경기 구성
---

서비스: https://tennisfolio.net
