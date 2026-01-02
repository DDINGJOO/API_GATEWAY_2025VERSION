#!/bin/bash

# 멀티 아키텍처 이미지 빌드 및 푸시 스크립트
# ARM64와 AMD64 모두 지원하는 이미지를 빌드합니다

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 기본 설정
IMAGE_NAME="ddingsh9/gateway-server"
BUILDER_NAME="multiarch-builder"

# 사용법 출력
usage() {
    echo "사용법: $0 [VERSION]"
    echo "예시: $0 1.1.9"
    echo "      $0 latest"
    exit 1
}

# 버전 파라미터 확인
if [ -z "$1" ]; then
    echo -e "${RED}오류: 버전을 입력해주세요${NC}"
    usage
fi

VERSION=$1

echo -e "${GREEN}=== 멀티 아키텍처 이미지 빌드 시작 ===${NC}"
echo -e "이미지: ${YELLOW}${IMAGE_NAME}:${VERSION}${NC}"
echo -e "플랫폼: ${YELLOW}linux/amd64, linux/arm64${NC}"
echo ""

# Docker 로그인 확인
echo -e "${GREEN}[1/5] Docker 로그인 확인...${NC}"
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}오류: Docker가 실행 중이지 않습니다${NC}"
    exit 1
fi

# buildx 빌더 생성 또는 사용
echo -e "${GREEN}[2/5] Docker buildx 빌더 설정...${NC}"
if ! docker buildx inspect ${BUILDER_NAME} > /dev/null 2>&1; then
    echo "새 빌더 생성 중..."
    docker buildx create --name ${BUILDER_NAME} --use
else
    echo "기존 빌더 사용 중..."
    docker buildx use ${BUILDER_NAME}
fi

# 빌더 부트스트랩
echo -e "${GREEN}[3/5] 빌더 부트스트랩...${NC}"
docker buildx inspect --bootstrap

# 멀티 아키텍처 이미지 빌드 및 푸시
echo -e "${GREEN}[4/5] 멀티 아키텍처 이미지 빌드 및 푸시...${NC}"
docker buildx build \
    --platform linux/amd64,linux/arm64 \
    --tag ${IMAGE_NAME}:${VERSION} \
    --push \
    .

# latest 태그도 함께 푸시할지 확인
if [ "$VERSION" != "latest" ]; then
    read -p "latest 태그도 함께 푸시하시겠습니까? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${GREEN}[5/5] latest 태그 빌드 및 푸시...${NC}"
        docker buildx build \
            --platform linux/amd64,linux/arm64 \
            --tag ${IMAGE_NAME}:latest \
            --push \
            .
    fi
else
    echo -e "${GREEN}[5/5] 완료 (latest 태그 스킵)${NC}"
fi
echo ""
echo -e "${GREEN}=== 빌드 완료 ===${NC}"
echo -e "이미지: ${YELLOW}${IMAGE_NAME}:${VERSION}${NC}"
echo -e "다음 명령어로 이미지를 확인할 수 있습니다:"
echo -e "${YELLOW}docker buildx imagetools inspect ${IMAGE_NAME}:${VERSION}${NC}"
echo ""
echo -e "${GREEN}docker-compose.yml 파일의 이미지 태그를 업데이트하세요:${NC}"
echo -e "${YELLOW}image: ${IMAGE_NAME}:${VERSION}${NC}"
