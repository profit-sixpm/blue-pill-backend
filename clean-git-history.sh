#!/bin/bash

echo "🔧 Git 히스토리에서 비밀정보 제거 중..."

# BFG Repo-Cleaner 설치 확인
if ! command -v bfg &> /dev/null; then
    echo "⚠️  BFG Repo-Cleaner가 설치되지 않았습니다."
    echo "설치 방법:"
    echo "  brew install bfg  # macOS"
    echo "  또는 https://rtyley.github.io/bfg-repo-cleaner/ 에서 다운로드"
    echo ""
    echo "대안: git filter-branch 사용"
    echo ""
    read -p "git filter-branch로 계속하시겠습니까? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi

    # .env.docker 파일 히스토리에서 제거
    echo "📝 .env.docker 파일 제거 중..."
    git filter-branch --force --index-filter \
        "git rm --cached --ignore-unmatch .env.docker" \
        --prune-empty --tag-name-filter cat -- --all

    # 민감한 내용이 포함된 docker-compose.prod.yml 커밋 제거
    echo "📝 docker-compose.prod.yml 히스토리 정리 중..."
    git filter-branch --force --index-filter \
        "git rm --cached --ignore-unmatch docker-compose.prod.yml || true" \
        --prune-empty --tag-name-filter cat -- --all
else
    # BFG 사용
    echo "🔨 BFG Repo-Cleaner로 정리 중..."
    bfg --delete-files .env.docker
    bfg --replace-text passwords.txt  # passwords.txt에 패스워드 목록 필요
fi

echo ""
echo "📦 Git reflog 정리 중..."
git reflog expire --expire=now --all
git gc --prune=now --aggressive

echo ""
echo "✅ 정리 완료!"
echo ""
echo "⚠️  강제 푸시 필요:"
echo "  git push origin --force --all"
echo ""
echo "⚠️  주의: 다른 팀원이 있다면 저장소를 다시 clone 받아야 합니다!"

