#!/bin/bash

echo "🚀 Starting deployment..."

# .env 파일 존재 확인
if [ ! -f .env ]; then
    echo "❌ Error: .env file not found!"
    echo "Please create .env file from .env.example"
    echo "  cp .env.example .env"
    echo "  vi .env  # Edit with your actual values"
    exit 1
fi

# 기존 컨테이너 중지 및 제거
echo "📦 Stopping existing containers..."
docker-compose -f docker-compose.prod.yml down

# 최신 이미지 pull
echo "🔄 Pulling latest image..."
docker pull wlsdn2165/cheongyak-be:latest

# 컨테이너 시작
echo "▶️  Starting containers..."
docker-compose -f docker-compose.prod.yml up -d

# 로그 확인
echo "📋 Checking logs..."
sleep 5
docker-compose -f docker-compose.prod.yml logs --tail=50

echo "✅ Deployment completed!"
echo ""
echo "Check status: docker-compose -f docker-compose.prod.yml ps"
echo "View logs: docker-compose -f docker-compose.prod.yml logs -f"

