# 🧀 Cheese321 API Server

H-Smile 사진관 추천 서비스 백엔드 API

## 🚀 빠른 시작

### 1. 프로젝트 클론
```bash
git clone https://github.com/3-2-1-Cheese/cheese-be.git
cd cheese321
```

### 2. 데이터베이스 실행
```bash
# PostgreSQL + PostGIS 실행
docker-compose up -d

# 실행 확인
docker ps
```

### 3. 애플리케이션 실행
```bash
# IDE에서 ApiApplication.kt 실행
# 또는 Gradle로:
./gradlew :api:bootRun
```

### 4. 확인
- **API 서버**: http://localhost:8080
- **API 문서**: http://localhost:8080/swagger-ui.html
- **헬스체크**: http://localhost:8080/actuator/health

## 🛠️ 개발 환경

- **Java 21**
- **Spring Boot 3.3.4**
- **PostgreSQL + PostGIS** (Docker)
- **JWT 인증**

## 배포

**로컬 Docker 테스트:**
```bash
# 1. JAR 빌드
./gradlew :api:build
cp api/build/libs/api-0.0.1-SNAPSHOT.jar .deploy/cheese321-api.jar
# 주의: 로컬 서버 포트 충돌 주의

# 2. Docker 실행
cd .deploy
cp .env.local .env
# 필요시 .env 파일 수정 (DB 포트 등)
docker-compose -f docker-compose-local.yml up -d
```

**운영 배포:**
[.deploy/README.md] 참조