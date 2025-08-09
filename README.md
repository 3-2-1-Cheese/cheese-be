# Cheese321 API Server

## 로컬 실행

### 1. 프로젝트 클론
```bash
git clone https://github.com/3-2-1-Cheese/cheese-be.git
cd cheese321
```

### 2. 데이터베이스 실행
```bash
docker-compose -f .deploy/docker-compose-local.yml up -d cheese321-db
```

### 3. 애플리케이션 실행
```bash
./gradlew :api:bootRun
```

### 4. 확인
- API 서버: http://localhost:8080
- API 문서: http://localhost:8080/swagger-ui/index.html
- 헬스체크: http://localhost:8080/actuator/health

## Docker로 전체 실행

### 1. JAR 빌드
```bash
./gradlew :api:bootJar
cp api/build/libs/api-0.0.1-SNAPSHOT.jar .deploy/cheese321-api.jar
```

### 2. Docker Compose 실행
```bash
docker-compose -f .deploy/docker-compose-local.yml up -d
```

## 운영 환경

### 배포된 서버
- API 서버: https://cheese321-backend-189210153639.asia-northeast3.run.app
- API 문서: https://cheese321-backend-189210153639.asia-northeast3.run.app/swagger-ui/index.html
- 헬스체크: https://cheese321-backend-189210153639.asia-northeast3.run.app/actuator/health

### 자동 배포
- `prod` 브랜치에 푸시하면 자동으로 GCP Cloud Run에 배포됩니다
- GitHub Actions를 통해 빌드 및 배포가 자동 실행됩니다


