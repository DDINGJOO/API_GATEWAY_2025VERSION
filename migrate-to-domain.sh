#!/bin/bash

# 도메인 중심 구조로 리팩토링하는 자동화 스크립트
# 이 스크립트는 모든 파일을 이동하고 패키지 선언과 import를 자동으로 수정합니다

set -e  # 에러 발생 시 즉시 중단

PROJECT_ROOT="/Users/ddingjoo/IdeaProjects/BanderProject/SERVER/API_GATEWAY"
SRC_PATH="$PROJECT_ROOT/src/main/java/com/study/api_gateway"

echo "🚀 도메인 중심 구조 리팩토링 시작..."
echo "📂 작업 디렉토리: $SRC_PATH"
echo ""

# 함수: 파일 이동 및 패키지 수정
move_and_fix_package() {
    local src_file=$1
    local dest_dir=$2
    local dest_file="$dest_dir/$(basename $src_file)"
    local old_package=$3
    local new_package=$4

    if [ -f "$src_file" ]; then
        mkdir -p "$dest_dir"
        cp "$src_file" "$dest_file"

        # 패키지 선언 변경
        sed -i '' "s/package $old_package;/package $new_package;/" "$dest_file"

        echo "  ✓ $(basename $src_file) → $new_package"
    fi
}

# Step 1: Auth 도메인
echo "📦 Step 1: Auth 도메인 마이그레이션..."
AUTH_DOMAIN="$SRC_PATH/domain/auth"

# AuthController
move_and_fix_package \
    "$SRC_PATH/controller/auth/AuthController.java" \
    "$AUTH_DOMAIN" \
    "com.study.api_gateway.controller.auth" \
    "com.study.api_gateway.domain.auth"

# AuthClient
move_and_fix_package \
    "$SRC_PATH/client/AuthClient.java" \
    "$AUTH_DOMAIN" \
    "com.study.api_gateway.client" \
    "com.study.api_gateway.domain.auth"

# Auth DTOs
for file in $SRC_PATH/dto/auth/request/*.java; do
    [ -f "$file" ] && move_and_fix_package \
        "$file" \
        "$AUTH_DOMAIN/dto" \
        "com.study.api_gateway.dto.auth.request" \
        "com.study.api_gateway.domain.auth.dto"
done

for file in $SRC_PATH/dto/auth/response/*.java; do
    [ -f "$file" ] && move_and_fix_package \
        "$file" \
        "$AUTH_DOMAIN/dto" \
        "com.study.api_gateway.dto.auth.response" \
        "com.study.api_gateway.domain.auth.dto"
done

# Auth Enums
for file in $SRC_PATH/dto/auth/enums/*.java; do
    [ -f "$file" ] && move_and_fix_package \
        "$file" \
        "$AUTH_DOMAIN/enums" \
        "com.study.api_gateway.dto.auth.enums" \
        "com.study.api_gateway.domain.auth.enums"
done

echo "✅ Auth 도메인 완료"
echo ""

# Step 2: Common 패키지 (먼저 이동해야 다른 도메인에서 참조 가능)
echo "📦 Step 2: Common 패키지 마이그레이션..."
COMMON="$SRC_PATH/common"

move_and_fix_package \
    "$SRC_PATH/dto/BaseResponse.java" \
    "$COMMON/dto" \
    "com.study.api_gateway.dto" \
    "com.study.api_gateway.common.dto"

move_and_fix_package \
    "$SRC_PATH/util/ResponseFactory.java" \
    "$COMMON/response" \
    "com.study.api_gateway.util" \
    "com.study.api_gateway.common.response"

move_and_fix_package \
    "$SRC_PATH/util/RequestPathHelper.java" \
    "$COMMON/util" \
    "com.study.api_gateway.util" \
    "com.study.api_gateway.common.util"

move_and_fix_package \
    "$SRC_PATH/config/GlobalExceptionHandler.java" \
    "$COMMON/exception" \
    "com.study.api_gateway.config" \
    "com.study.api_gateway.common.exception"

echo "✅ Common 패키지 완료"
echo ""

# Step 3: 전역 import 수정
echo "📝 Step 3: Import 문 수정 중..."

# 모든 Java 파일에서 import 수정
find "$SRC_PATH/domain" -name "*.java" -type f | while read file; do
    # BaseResponse import 수정
    sed -i '' 's/import com\.study\.api_gateway\.dto\.BaseResponse;/import com.study.api_gateway.common.dto.BaseResponse;/g' "$file"

    # ResponseFactory import 수정
    sed -i '' 's/import com\.study\.api_gateway\.util\.ResponseFactory;/import com.study.api_gateway.common.response.ResponseFactory;/g' "$file"

    # AuthClient import 수정
    sed -i '' 's/import com\.study\.api_gateway\.client\.AuthClient;/import com.study.api_gateway.domain.auth.AuthClient;/g' "$file"

    # Auth DTO imports 수정
    sed -i '' 's/import com\.study\.api_gateway\.dto\.auth\.request\./import com.study.api_gateway.domain.auth.dto./g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.auth\.response\./import com.study.api_gateway.domain.auth.dto./g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.auth\.enums\./import com.study.api_gateway.domain.auth.enums./g' "$file"
done

echo "✅ Import 수정 완료"
echo ""

echo "🎉 Auth 도메인 및 Common 패키지 마이그레이션 완료!"
echo ""
echo "다음 단계:"
echo "1. ./gradlew compileJava 로 빌드 확인"
echo "2. 정상이면 git commit -m 'refactor: migrate auth domain and common packages'"
echo "3. 나머지 도메인도 순차적으로 마이그레이션"
