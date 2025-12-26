# 🚀 AWS 배포 가이드

## 📋 사전 요구사항

- AWS EC2 인스턴스 (Ubuntu 20.04+)
- Docker & Docker Compose 설치
- 포트 8080, 5432 오픈 (보안 그룹 설정)

## 🔧 1. EC2 인스턴스 초기 설정

```bash
# Docker 설치
sudo apt update
sudo apt install -y docker.io docker-compose
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER

# 재로그인 필요
exit
```

## 📦 2. 프로젝트 파일 업로드

```bash
# EC2에 접속
ssh -i your-key.pem ubuntu@your-ec2-ip

# 작업 디렉토리 생성
mkdir -p ~/blue-pill-backend
cd ~/blue-pill-backend

# 파일 업로드 (로컬에서 실행)
scp -i your-key.pem docker-compose.prod.yml ubuntu@your-ec2-ip:~/blue-pill-backend/
scp -i your-key.pem deploy.sh ubuntu@your-ec2-ip:~/blue-pill-backend/
scp -i your-key.pem check-db.sh ubuntu@your-ec2-ip:~/blue-pill-backend/
scp -i your-key.pem .env.example ubuntu@your-ec2-ip:~/blue-pill-backend/

# EC2에서 실행
cd ~/blue-pill-backend

# .env 파일 생성
cp .env.example .env
vi .env  # 실제 값으로 수정

# 실행 권한 부여
chmod +x deploy.sh check-db.sh
```

### 📝 .env 파일 설정 예시

```bash
# PostgreSQL Configuration
POSTGRES_DB=defaultdb
POSTGRES_USER=avnadmin
POSTGRES_PASSWORD=your_secure_password_here

# Backend Database Configuration
DB_URL=jdbc:postgresql://postgres:5432/defaultdb
DB_USERNAME=avnadmin
DB_PASSWORD=your_secure_password_here

# OpenAI Configuration
OPENAI_API_KEY=sk-xxxxxxxxxxxxx
OPENAI_CHAT_MODEL=gpt-4
OPENAI_BASE_URL=https://api.openai.com/v1

# Upstage Configuration
UPSTAGE_API_KEY=up_xxxxxxxxxxxxx

# LH API Configuration
LH_API_SERVICE_KEY=99cca683d5d61074f88fd17c1dca7d9dabdc0909abac016a7e7db29ecf466108
```

## 🚀 3. 배포 실행

```bash
cd ~/blue-pill-backend

# 배포 스크립트 실행
./deploy.sh
```

## 📊 4. 상태 확인

```bash
# 컨테이너 상태 확인
docker-compose -f docker-compose.prod.yml ps

# 로그 확인
docker-compose -f docker-compose.prod.yml logs -f backend
docker-compose -f docker-compose.prod.yml logs -f postgres

# DB 확인
./check-db.sh

# 또는 직접 접속
docker exec -it cheongyak-postgres psql -U avnadmin -d defaultdb
```

## 🔄 5. 업데이트 배포

```bash
# 로컬에서 이미지 빌드 & 푸시
docker build -t wlsdn2165/cheongyak-be:latest .
docker push wlsdn2165/cheongyak-be:latest

# EC2에서 재배포
cd ~/blue-pill-backend
./deploy.sh
```

## 🛑 6. 중지 & 제거

```bash
# 컨테이너 중지
docker-compose -f docker-compose.prod.yml stop

# 컨테이너 제거 (데이터 유지)
docker-compose -f docker-compose.prod.yml down

# 컨테이너 & 데이터 모두 제거 (⚠️ 주의!)
docker-compose -f docker-compose.prod.yml down -v
```

## 📝 7. PostgreSQL 백업 & 복구

### 백업
```bash
# 전체 DB 백업
docker exec cheongyak-postgres pg_dump -U avnadmin defaultdb > backup_$(date +%Y%m%d_%H%M%S).sql

# 특정 테이블만 백업
docker exec cheongyak-postgres pg_dump -U avnadmin -t announcements defaultdb > announcements_backup.sql
```

### 복구
```bash
# 백업 복구
docker exec -i cheongyak-postgres psql -U avnadmin defaultdb < backup.sql
```

## 🔍 8. 트러블슈팅

### .env 파일 없음 에러
```bash
# .env 파일 생성
cp .env.example .env
vi .env  # 실제 값으로 수정
```

### PostgreSQL 연결 실패
```bash
# 컨테이너 상태 확인
docker-compose -f docker-compose.prod.yml ps

# PostgreSQL 로그 확인
docker-compose -f docker-compose.prod.yml logs postgres

# 헬스체크 확인
docker exec cheongyak-postgres pg_isready -U avnadmin -d defaultdb
```

### 백엔드 연결 실패
```bash
# 환경변수 확인
docker exec cheongyak-backend env | grep DB_

# 네트워크 확인
docker network inspect blue-pill-backend_cheongyak-network
```

## 🌐 9. API 테스트

```bash
# Health Check
curl http://your-ec2-ip:8080/health

# 청약 공고 조회
curl -X POST http://your-ec2-ip:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{"announcementDate": "20251227"}'
```

## 📌 주요 명령어 모음

```bash
# 컨테이너 시작
docker-compose -f docker-compose.prod.yml up -d

# 컨테이너 중지
docker-compose -f docker-compose.prod.yml stop

# 컨테이너 재시작
docker-compose -f docker-compose.prod.yml restart

# 로그 실시간 보기
docker-compose -f docker-compose.prod.yml logs -f

# 특정 서비스만 재시작
docker-compose -f docker-compose.prod.yml restart backend

# PostgreSQL 직접 접속
docker exec -it cheongyak-postgres psql -U avnadmin -d defaultdb

# 볼륨 확인
docker volume ls

# 디스크 사용량 확인
docker system df
```

## ⚙️ 환경변수 (.env 파일)

필수 환경변수:
- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `LH_API_SERVICE_KEY`

선택 환경변수:
- `OPENAI_API_KEY`, `OPENAI_CHAT_MODEL`, `OPENAI_BASE_URL`
- `UPSTAGE_API_KEY`

## 🔐 보안 그룹 설정 (AWS)

| Type | Protocol | Port | Source |
|------|----------|------|--------|
| SSH | TCP | 22 | Your IP |
| Custom TCP | TCP | 8080 | 0.0.0.0/0 |
| PostgreSQL | TCP | 5432 | Security Group (내부) |

⚠️ **주의**: PostgreSQL 5432 포트는 외부에서 접근할 필요 없으므로 보안 그룹에서 열지 마세요!

## 📈 모니터링

```bash
# 리소스 사용량 실시간 모니터링
docker stats

# 디스크 정리
docker system prune -a
```

## 🔒 보안 권장사항

1. **`.env` 파일은 절대 Git에 커밋하지 마세요**
2. **강력한 비밀번호 사용**
3. **PostgreSQL은 외부 노출 금지**
4. **정기적인 백업 수행**
5. **AWS IAM 역할 사용 권장**

