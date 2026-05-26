
---

# 🎮 SavePoint (세이브포인트)

> **Vultr VPS와 Cloudflare R2를 활용한 백엔드 인프라 구축 및 게이머 전용 아카이빙 플랫폼**

SavePoint는 게이머 커플/듀오를 위한 상태 공유 및 미디어 아카이브 서비스입니다. 초기 AWS 프리티어 만료에 따른 인프라 비용 부담을 해결하기 위해 독립 VPS 및 S3 호환 스토리지로 마이그레이션하며 가성비를 최적화했습니다.

---

## 📅 Project Overview

| 항목 | 상세 내용 |
| :--- | :--- |
| **개발 인원** | 개인 프로젝트 (1인 개발) |
| **주요 역할** | 백엔드 아키텍처 설계, REST API 구현, 클라우드 인프라 배포 및 비용 최적화 |
| **서비스 환경** | 모바일 앱 (Android 우선 지원 / iOS 추후 지원 환경) |
| **소스 코드** | [Backend Repository](본인_깃허브_백엔드_링크) / [Frontend Repository](본인_깃허브_프론트_링크) |

### 🎯 핵심 개발 목표
1. **비용 효율적인 인프라 구축:** AWS 프리티어 만료 이후를 고려하여, VPS와 가성비 스토리지를 활용한 실생용 독립 배포 환경을 경험하여 비용 감축과 높은 성능 목표로 합니다.
2. **안정적인 도메인 설계:** 게이머 커플 간의 유기적인 데이터 흐름을 다루며, 확장 가능하고 백엔드 API 구조를 확립합니다.
---

## 🛠️ 기술 스택 & 인프라

### 🖥️ Backend & Database

* **Framework:** Java 17 / Spring Boot 3.4.5 (Build: Gradle)
* **Database & Auth:** Supabase (PostgreSQL)
* **API Docs:** Springdoc-OpenAPI (Swagger UI)

#### 📸 API Verification & Test
<summary>📸 핵심 API 명세서 (Swagger) 스크린샷 보기</summary>
* 보안상의 문제로 실제 url이 아닌 캡처본으로 대처합니다 *
<table width="100%">
  <tr>
    <td width="33.3%" align="center" valign="top">
      <img src="https://github.com/user-attachments/assets/d24d98cd-1aaf-471c-81cf-d13a5d10661b" alt="Swagger 1" style="width:100%; border-radius:4px;">
    </td>
    <td width="33.3%" align="center" valign="top">
      <img src="https://github.com/user-attachments/assets/21eddf33-85ef-45fc-a5ae-99a33bec1862" alt="Swagger 3" style="width:100%; border-radius:4px;">
    </td>
    <td width="33.3%" align="center" valign="top">
      <img src="https://github.com/user-attachments/assets/7873fed0-eb73-481f-bbdb-74dd24d9b428" alt="Swagger 2" style="width:100%; border-radius:4px;">
    </td>
  </tr>
</table>
### 📱 Frontend & Cloud

* **Frontend:** React Native (Expo)
* **Compute:** Vultr VPS (Ubuntu OS) / DOCKERFILE
* **Storage:** Cloudflare R2 (S3-Compatible)
* **Notification:** Firebase Cloud Messaging (FCM)

---

## 🦾 Core Features (기능 명세)

* **배틀 로그 관리 API:** 주요 게임(LoL, 발로란트, 오버워치2, FF14 등) 전적 결과, KDA, 메모 데이터를 적재하는 전적 API 구현.
* **커플 연동 시스템:** 고유 `connection_code` 기반의 매핑 로직을 통해 커플 상태(`ACTIVE`, `PENDING`) 관리 및 일정(`events`) 데이터 공동 처리.
* **유튜브 링크 연동 기반 아카이브:** 대용량 영상 업로드 리소스를 절감하기 위해 유튜브 고유 ID(`youtubeVideoId`) 파싱 및 썸네일/조회수 상태 동기화 API 구현.
* **Cloudflare R2 사진 앨범:** 커플 공동 앨범 사진 저장을 위해 S3 호환 스토리지인 Cloudflare R2 연동 및 댓글(`comments`) CRUD API 구축.

---

## 🗄️ Database Architecture

* **`users` / `couples`:** 유저 정보(`email`, `fcmToken`) 및 초대 코드를 통한 커플 매핑 구조 설계.
* **`battle_data` / `game_sessions`:** 주요 게임 타입(Enum)별 전적 및 게임 시작/종료 세션 지속 시간 기록.
* **`photos` / `comments` / `replays`:** 사진 정보(R2 Key 주소), 댓글 정합성을 위한 생명주기(`@PrePersist`) 적용, 유튜브 비디오 ID 데이터 관리.

---

## ⚠️ 아키텍처 다변화 및 트러블 슈팅
**1. 고정 인프라 비용 절감 (Railway & AWS-> Vultr & Cloudflare R2)**
문제 상황: 초기 AWS 및 Railway 환경에서 빌드 후, 프리티어 만료에 따른 고정비 및 고화질 미디어 다운로드 시 발생하는 S3 Egress(데이터 전송) 비용 부담이 크게 발생할 것으로 예상됨.

해결 방안: 백엔드 애플리케이션 배포 환경을 가성비가 높은 Vultr VPS로 전면 마이그레이션하고, 스토리지는 전송 수수료가 전혀 없는 Cloudflare R2로 전환하여 인프라 유지 비용을 최적화함. 또한 가장 작은 서버 내에서 최대한 해결보기위해 DOCKER을 이용해 이미지만을 업로드 하도록 결정. 

**2. 기획 변경에 따른 아키텍처 경량화 (Electron -> Mobile Centered)**
문제 상황: 초기에는 PC용 에이전트를 구축하여 컴퓨터 내 프로세스를 스캔하고 실시간 접속 상태를 감지하는 방식을 검증했으나, 사생활 침해 우려(UX 허들) 및 상시 WebSocket 연결 유지에 따른 서버 비용 가중 리스크를 정의함.

해결 방안: 유저 관점의 프라이버시와 서비스 지속 가능성을 위해 기존 실시간 구조를 과감히 제거하고, 모바일 앱 중심의 데이터 적재 방식으로 아키텍처를 간소화하여 리소스를 효율화함.

**3. 생산성 극대화를 위한 AI 페어 프로그래밍 도입** 
내용: 빠르게 서비스 프로토타입을 빌드하기 위해 AI 도구를 활용함. AI가 생성한 보일러플레이트 코드의 맹목적 수용을 지양하고, 프로젝트 공통 구조에 맞춰 예외 처리(GlobalExceptionHandler) 및 비즈니스 로직의 정합성을 직접 검증하며 개발 속도와 코드 퀄리티를 모두 확보함.

**4. 외부 라이브러리 연동 시 인코딩 및 환경 변수 매핑 오류 수정 (Firebase Admin SDK)**
문제 상황: Docker 및 클라우드 배포 환경에서 환경 변수로 주입된 Firebase PRIVATE_KEY 인증 실패(Invalid PKCS#8 data) 및 Spring Relaxed Binding 규칙 불일치로 인한 서버 구동 실패 현상 발생.

원인 분석: 환경 변수로 주입되는 과정에서 줄바꿈 문자(\n)가 시스템 내부적으로 이중 이스케이프(\\n) 처리되어 규격이 깨진 것과 대소문자 매핑 불일치가 원인임을 직접 디버그 로그로 추적하여 검거함.

해결 방안: .env 환경 변수 키값을 대문자_언더스코어 표준 규격으로 통일하고, 자바 초기화 로직(@PostConstruct) 내에서 이중 이스케이프를 실제 줄바꿈 문자열로 치환(replace("\\\\n", "\n"))하여 연동 정합성을 확보함.

**5. IPv6 호환성으로 인한 데이터베이스 연결 실패 우회 (Supabase)**
문제 상황: Supabase(PostgreSQL) DB 연동 시 배포 인프라 및 로컬 개발 환경 간 Not IPv4 compatible 에러와 함께 데이터베이스 커넥션 거부 현상 발생.

원인 분석: Supabase의 Direct Connection 방식이 IPv6 주소 체계 전용으로 제공되어, 일반적인 IPv4 기반 환경에서 네트워크 충돌이 일어남을 확인.

해결 방안: 백엔드 데이터베이스 엔드포인트 주소를 Direct Connection에서 IPv4 환경을 원활하게 지원하는 Session Pooler 호스트 정보로 안전하게 우회 설정하여 네트워크 연결을 안정화함.

**6. 클라이언트-백엔드 간 HTTP 통신 정책 및 네트워크 예외 처리 (Android / JWT)**
문제 상황: 모바일 에뮬레이터 환경에서 백엔드 API 요청 시 Network request failed 통신 오류가 발생하거나, JWT 토큰 만료 시 후속 API 요청이 연쇄 실패하는 현상 발생.

원인 분석: Android 9 이상 OS 레벨에서 기본적으로 HTTP(Cleartext Traffic) 통신을 차단하는 보안 정책과, 토큰 만료(403 Forbidden) 시 클라이언트 단에서 파트너 데이터 예외 처리가 누락되어 발생한 연쇄 버그임을 확인.

해결 방안: 개발 단계에서 매니페스트 설정(usesCleartextTraffic: true)을 명시하여 통신 장벽을 해제하고, 로깅(console.log) 및 에러 상태 코드 분기 처리를 통해 토큰 만료 흐름을 직관적으로 추적할 수 있도록 개선함.

...
