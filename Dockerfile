FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# 1. 모듈 다운로드에 필요한 설정 파일만 '먼저' 복사.
COPY build.gradle settings.gradle ./

# 2. [핵심] 소스 코드가 없는 상태에서 라이브러리(의존성 모듈)만 미리 다운로드해서 캐시에 굽기.
RUN gradle dependencies --no-daemon || true

# 3. 소스 코드를 복사.
COPY src ./src

# 4. 이미 다운로드된 모듈들을 재사용해서 내 코드만 빌드.
RUN gradle build -x test --no-daemon

FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
