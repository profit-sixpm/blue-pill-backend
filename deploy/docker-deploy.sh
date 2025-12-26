#!/bin/bash

# Docker 배포 스크립트

set -e

echo "======================================"
echo "Docker 배포 스크립트"
echo "======================================"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 에러 출력 함수
error() {
    echo -e "${RED}[ERROR] $1${NC}"
    exit 1
}

# 성공 출력 함수
success() {
    echo -e "${GREEN}[SUCCESS] $1${NC}"
}

# 경고 출력 함수
warning() {
    echo -e "${YELLOW}[WARNING] $1${NC}"
}

# 환경변수 파일 확인
check_env_file() {
    if [ ! -f .env ]; then
        warning ".env 파일이 없습니다."

        if [ -f .env.docker ]; then
            echo "==> .env.docker 파일을 .env로 복사하시겠습니까? (y/n)"
            read -r response
            if [[ "$response" == "y" ]]; then
                cp .env.docker .env
                success ".env 파일이 생성되었습니다. 값을 확인하고 필요시 수정하세요."
                echo "==> nano .env 또는 vi .env로 편집할 수 있습니다."
                read -p "지금 편집하시겠습니까? (y/n): " edit_now
                if [[ "$edit_now" == "y" ]]; then
                    ${EDITOR:-nano} .env
                fi
            else
                error ".env 파일이 필요합니다. .env.docker를 참고하여 생성하세요."
            fi
        else
            error ".env 파일이 필요합니다."
        fi
    fi
    success ".env 파일 확인 완료"
}

# Gradle 빌드
build_gradle() {
    echo ""
    echo "==> Gradle 빌드 시작..."

    if [ ! -f ./gradlew ]; then
        error "gradlew 파일을 찾을 수 없습니다."
    fi

    chmod +x ./gradlew
    ./gradlew clean build -x test

    if [ $? -eq 0 ]; then
        success "Gradle 빌드 완료"
    else
        error "Gradle 빌드 실패"
    fi
}

# Docker 이미지 빌드
build_docker_image() {
    echo ""
    echo "==> Docker 이미지 빌드 시작..."

    docker build -t blue-pill-backend:latest .

    if [ $? -eq 0 ]; then
        success "Docker 이미지 빌드 완료"
    else
        error "Docker 이미지 빌드 실패"
    fi
}

# 기존 컨테이너 중지 및 제거
stop_existing_container() {
    echo ""
    echo "==> 기존 컨테이너 확인 중..."

    if [ "$(docker ps -q -f name=blue-pill-backend)" ]; then
        echo "==> 실행 중인 컨테이너 중지 중..."
        docker stop blue-pill-backend
        success "컨테이너 중지 완료"
    fi

    if [ "$(docker ps -aq -f name=blue-pill-backend)" ]; then
        echo "==> 기존 컨테이너 제거 중..."
        docker rm blue-pill-backend
        success "컨테이너 제거 완료"
    fi
}

# Docker Compose로 실행
start_with_compose() {
    echo ""
    echo "==> Docker Compose로 애플리케이션 시작..."

    # .env 파일 로드하여 환경변수 확인
    source .env

    docker-compose up -d

    if [ $? -eq 0 ]; then
        success "애플리케이션 시작 완료"
    else
        error "애플리케이션 시작 실패"
    fi
}

# 로그 확인
show_logs() {
    echo ""
    echo "==> 컨테이너 로그 (Ctrl+C로 종료):"
    echo ""
    docker-compose logs -f
}

# 상태 확인
check_status() {
    echo ""
    echo "======================================"
    echo "컨테이너 상태 확인"
    echo "======================================"
    docker-compose ps

    echo ""
    echo "======================================"
    echo "헬스체크 (10초 대기 후)"
    echo "======================================"
    sleep 10

    if curl -f http://localhost:8080/health > /dev/null 2>&1; then
        success "애플리케이션이 정상적으로 실행 중입니다!"
        echo "==> Swagger UI: http://localhost:8080/swagger-ui/index.html"
    else
        warning "헬스체크 실패. 로그를 확인하세요."
        echo "==> docker-compose logs -f 명령어로 로그 확인"
    fi
}

# 환경변수 확인 (디버깅용)
verify_env_vars() {
    echo ""
    echo "==> 컨테이너 내부 환경변수 확인..."
    docker exec blue-pill-backend env | grep -E "DB_|JWT_|AWS_|SPRING_" || warning "환경변수를 찾을 수 없습니다"
}

# 메인 함수
main() {
    echo ""
    echo "배포 옵션을 선택하세요:"
    echo "1) 전체 배포 (빌드 + Docker 이미지 생성 + 실행)"
    echo "2) Docker 이미지만 생성"
    echo "3) 기존 이미지로 실행"
    echo "4) 중지"
    echo "5) 로그 확인"
    echo "6) 상태 확인"
    echo "7) 환경변수 확인 (디버깅)"
    echo ""

    read -p "선택 (1-7): " choice

    case $choice in
        1)
            check_env_file
            build_gradle
            build_docker_image
            stop_existing_container
            start_with_compose
            check_status
            ;;
        2)
            build_gradle
            build_docker_image
            ;;
        3)
            check_env_file
            stop_existing_container
            start_with_compose
            check_status
            ;;
        4)
            echo "==> 컨테이너 중지 중..."
            docker-compose down
            success "컨테이너가 중지되었습니다"
            ;;
        5)
            show_logs
            ;;
        6)
            check_status
            verify_env_vars
            ;;
        7)
            verify_env_vars
            ;;
        *)
            error "잘못된 선택입니다"
            ;;
    esac
}

main

echo ""
echo "======================================"
echo "유용한 명령어:"
echo "======================================"
echo "로그 확인: docker-compose logs -f"
echo "컨테이너 중지: docker-compose down"
echo "컨테이너 재시작: docker-compose restart"
echo "환경변수 확인: docker exec blue-pill-backend env"
echo "컨테이너 접속: docker exec -it blue-pill-backend /bin/bash"
echo "======================================"

