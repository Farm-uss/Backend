# BE
팜어스 백엔드

## Deploy (main)
- GitHub Actions: `.github/workflows/deploy.yml`
- 배포 방식:
  - Docker 이미지를 `latest`와 `commit SHA` 두 태그로 푸시
  - 서버 `.env`를 Actions에서 재작성
  - `docker-compose.override.yml`로 실제 실행 이미지를 `${DOCKER_USERNAME}/farmus-be:${IMAGE_TAG}`로 고정
- 템플릿 파일:
  - `deploy/docker-compose.server.template.yml`
  - `deploy/.env.server.template`

### Required GitHub Secrets
- `DOCKER_USERNAME`
- `DOCKER_PASSWORD`
- `DB_HOST` (SSH 서버 호스트)
- `SSH_PORT`
- `USERNAME` (SSH 계정)
- `PASSWORD` (SSH 비밀번호)
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `GOOGLE_MAP_API_KEY`
- `SWAGGER_SERVER_URL`
- `FILE_UPLOAD_DIR`
- `GDD_SERVICE_KEY`
- `GDD_SCHEDULER_CRON`
- `CAMERA_STREAM_URL`
- `CAMERA_STREAM_PROTOCOL`
- `CAMERA_STREAM_TTL_MINUTES`

## Local Docker (team shared)
- File: `docker-compose.local.yml`
- Example env: `.env.local.example`

### 1) First setup
```bash
cp .env.local.example .env.local
docker compose --env-file .env.local -f docker-compose.local.yml up -d
```

### 2) Check DB container
```bash
docker compose --env-file .env.local -f docker-compose.local.yml ps
```

### 3) Run backend with same DB
```bash
SPRING_PROFILES_ACTIVE=dev \
DB_HOST=localhost \
DB_PORT=5433 \
DB_NAME=mydb \
DB_USERNAME=admin \
DB_PASSWORD=strongpass \
JWT_SECRET=dev-secret-key-at-least-32-bytes-long-12345 \
GOOGLE_MAP_API_KEY=dummy \
GDD_SERVICE_KEY=dummy \
FILE_UPLOAD_DIR=/tmp/farmus_images/ \
AI_INFERENCE_BASE_URL=http://127.0.0.1:8001 \
./gradlew bootRun
```

### 4) Stop
```bash
docker compose --env-file .env.local -f docker-compose.local.yml down
```

### Note
- Compose config is shared in git, but container data is local for each teammate.
- If you need same seed data, share SQL seed files and apply them after startup.
