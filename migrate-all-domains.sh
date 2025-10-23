#!/bin/bash

# 나머지 모든 도메인을 마이그레이션하는 스크립트

set -e
PROJECT_ROOT="/Users/ddingjoo/IdeaProjects/BanderProject/SERVER/API_GATEWAY"
SRC_PATH="$PROJECT_ROOT/src/main/java/com/study/api_gateway"

echo "🚀 나머지 모든 도메인 마이그레이션 시작..."

# 함수: 파일 이동 및 패키지 수정
move_and_fix() {
    local src=$1
    local dest_dir=$2
    local old_pkg=$3
    local new_pkg=$4

    if [ -f "$src" ]; then
        mkdir -p "$dest_dir"
        cp "$src" "$dest_dir/"
        local dest_file="$dest_dir/$(basename $src)"
        sed -i '' "s/package $old_pkg;/package $new_pkg;/" "$dest_file"
        echo "  ✓ $(basename $src)"
    fi
}

# Profile 도메인
echo ""
echo "📦 Profile 도메인..."
PROFILE="$SRC_PATH/domain/profile"

move_and_fix "$SRC_PATH/controller/profile/ProfileController.java" "$PROFILE" \
    "com.study.api_gateway.controller.profile" "com.study.api_gateway.domain.profile"

move_and_fix "$SRC_PATH/client/ProfileClient.java" "$PROFILE" \
    "com.study.api_gateway.client" "com.study.api_gateway.domain.profile"

for f in $SRC_PATH/dto/profile/request/*.java; do
    [ -f "$f" ] && move_and_fix "$f" "$PROFILE/dto" \
        "com.study.api_gateway.dto.profile.request" "com.study.api_gateway.domain.profile.dto"
done

for f in $SRC_PATH/dto/profile/response/*.java; do
    [ -f "$f" ] && move_and_fix "$f" "$PROFILE/dto" \
        "com.study.api_gateway.dto.profile.response" "com.study.api_gateway.domain.profile.dto"
done

[ -f "$SRC_PATH/dto/profile/ProfileSearchCriteria.java" ] && move_and_fix \
    "$SRC_PATH/dto/profile/ProfileSearchCriteria.java" "$PROFILE/dto" \
    "com.study.api_gateway.dto.profile" "com.study.api_gateway.domain.profile.dto"

[ -f "$SRC_PATH/dto/profile/enums/City.java" ] && move_and_fix \
    "$SRC_PATH/dto/profile/enums/City.java" "$PROFILE/enums" \
    "com.study.api_gateway.dto.profile.enums" "com.study.api_gateway.domain.profile.enums"

move_and_fix "$SRC_PATH/util/ProfileEnrichmentUtil.java" "$PROFILE" \
    "com.study.api_gateway.util" "com.study.api_gateway.domain.profile"

for f in $SRC_PATH/util/cache/*.java; do
    [ -f "$f" ] && move_and_fix "$f" "$PROFILE/cache" \
        "com.study.api_gateway.util.cache" "com.study.api_gateway.domain.profile.cache"
done

# ProfileEnrichmentUtil을 ProfileEnrichmentService로 이름 변경
if [ -f "$PROFILE/ProfileEnrichmentUtil.java" ]; then
    mv "$PROFILE/ProfileEnrichmentUtil.java" "$PROFILE/ProfileEnrichmentService.java"
    sed -i '' 's/public class ProfileEnrichmentUtil/public class ProfileEnrichmentService/g' \
        "$PROFILE/ProfileEnrichmentService.java"
    echo "  ✓ ProfileEnrichmentUtil → ProfileEnrichmentService"
fi

echo "✅ Profile 완료"

# Article 도메인
echo ""
echo "📦 Article 도메인..."
ARTICLE="$SRC_PATH/domain/article"

for ctrl in ArticleController NoticeController EventController; do
    move_and_fix "$SRC_PATH/controller/article/$ctrl.java" "$ARTICLE/controller" \
        "com.study.api_gateway.controller.article" "com.study.api_gateway.domain.article.controller"
done

for cli in ArticleClient NoticeClient EventClient; do
    move_and_fix "$SRC_PATH/client/$cli.java" "$ARTICLE/client" \
        "com.study.api_gateway.client" "com.study.api_gateway.domain.article.client"
done

for f in $SRC_PATH/dto/Article/request/*.java; do
    [ -f "$f" ] && move_and_fix "$f" "$ARTICLE/dto" \
        "com.study.api_gateway.dto.Article.request" "com.study.api_gateway.domain.article.dto"
done

for f in $SRC_PATH/dto/Article/response/*.java; do
    [ -f "$f" ] && move_and_fix "$f" "$ARTICLE/dto" \
        "com.study.api_gateway.dto.Article.response" "com.study.api_gateway.domain.article.dto"
done

echo "✅ Article 완료"

# Comment 도메인
echo ""
echo "📦 Comment 도메인..."
COMMENT="$SRC_PATH/domain/comment"

move_and_fix "$SRC_PATH/controller/comment/CommentController.java" "$COMMENT" \
    "com.study.api_gateway.controller.comment" "com.study.api_gateway.domain.comment"

move_and_fix "$SRC_PATH/client/CommentClient.java" "$COMMENT" \
    "com.study.api_gateway.client" "com.study.api_gateway.domain.comment"

for f in $SRC_PATH/dto/comment/request/*.java; do
    [ -f "$f" ] && move_and_fix "$f" "$COMMENT/dto" \
        "com.study.api_gateway.dto.comment.request" "com.study.api_gateway.domain.comment.dto"
done

echo "✅ Comment 완료"

# Like 도메인 (gaechu → like)
echo ""
echo "📦 Like 도메인..."
LIKE="$SRC_PATH/domain/like"

move_and_fix "$SRC_PATH/controller/gaechu/GaechuController.java" "$LIKE" \
    "com.study.api_gateway.controller.gaechu" "com.study.api_gateway.domain.like"

move_and_fix "$SRC_PATH/client/GaechuClient.java" "$LIKE" \
    "com.study.api_gateway.client" "com.study.api_gateway.domain.like"

for f in $SRC_PATH/dto/gaechu/*.java; do
    [ -f "$f" ] && move_and_fix "$f" "$LIKE/dto" \
        "com.study.api_gateway.dto.gaechu" "com.study.api_gateway.domain.like.dto"
done

# GaechuController를 LikeController로 이름 변경
if [ -f "$LIKE/GaechuController.java" ]; then
    mv "$LIKE/GaechuController.java" "$LIKE/LikeController.java"
    sed -i '' 's/public class GaechuController/public class LikeController/g' "$LIKE/LikeController.java"
    sed -i '' 's/@RequestMapping("\/bff\/v1\/gaechu")/@RequestMapping("\/bff\/v1\/like")/g' "$LIKE/LikeController.java"
    echo "  ✓ GaechuController → LikeController"
fi

# GaechuClient를 LikeClient로 이름 변경
if [ -f "$LIKE/GaechuClient.java" ]; then
    mv "$LIKE/GaechuClient.java" "$LIKE/LikeClient.java"
    sed -i '' 's/public class GaechuClient/public class LikeClient/g' "$LIKE/LikeClient.java"
    echo "  ✓ GaechuClient → LikeClient"
fi

echo "✅ Like 완료"

# Feed 도메인 (activity → feed)
echo ""
echo "📦 Feed 도메인..."
FEED="$SRC_PATH/domain/feed"

move_and_fix "$SRC_PATH/controller/activity/FeedController.java" "$FEED" \
    "com.study.api_gateway.controller.activity" "com.study.api_gateway.domain.feed"

move_and_fix "$SRC_PATH/client/ActivityClient.java" "$FEED" \
    "com.study.api_gateway.client" "com.study.api_gateway.domain.feed"

for f in $SRC_PATH/dto/activity/request/*.java; do
    [ -f "$f" ] && move_and_fix "$f" "$FEED/dto" \
        "com.study.api_gateway.dto.activity.request" "com.study.api_gateway.domain.feed.dto"
done

for f in $SRC_PATH/dto/activity/response/*.java; do
    [ -f "$f" ] && move_and_fix "$f" "$FEED/dto" \
        "com.study.api_gateway.dto.activity.response" "com.study.api_gateway.domain.feed.dto"
done

# ActivityClient를 FeedClient로 이름 변경
if [ -f "$FEED/ActivityClient.java" ]; then
    mv "$FEED/ActivityClient.java" "$FEED/FeedClient.java"
    sed -i '' 's/public class ActivityClient/public class FeedClient/g' "$FEED/FeedClient.java"
    echo "  ✓ ActivityClient → FeedClient"
fi

echo "✅ Feed 완료"

# Image 도메인
echo ""
echo "📦 Image 도메인..."
IMAGE="$SRC_PATH/domain/image"

move_and_fix "$SRC_PATH/client/ImageClient.java" "$IMAGE" \
    "com.study.api_gateway.client" "com.study.api_gateway.domain.image"

move_and_fix "$SRC_PATH/service/ImageConfirmService.java" "$IMAGE" \
    "com.study.api_gateway.service" "com.study.api_gateway.domain.image"

echo "✅ Image 완료"

# Support 도메인
echo ""
echo "📦 Support 도메인..."
SUPPORT="$SRC_PATH/domain/support"

for ctrl in InquiryController ReportController; do
    move_and_fix "$SRC_PATH/controller/support/$ctrl.java" "$SUPPORT/controller" \
        "com.study.api_gateway.controller.support" "com.study.api_gateway.domain.support.controller"
done

for cli in InquiryClient ReportClient FaqClient; do
    move_and_fix "$SRC_PATH/client/$cli.java" "$SUPPORT/client" \
        "com.study.api_gateway.client" "com.study.api_gateway.domain.support.client"
done

# DTOs
for f in $SRC_PATH/dto/support/inquiry/request/*.java; do
    [ -f "$f" ] && move_and_fix "$f" "$SUPPORT/dto/inquiry" \
        "com.study.api_gateway.dto.support.inquiry.request" "com.study.api_gateway.domain.support.dto.inquiry"
done

for f in $SRC_PATH/dto/support/inquiry/response/*.java; do
    [ -f "$f" ] && move_and_fix "$f" "$SUPPORT/dto/inquiry" \
        "com.study.api_gateway.dto.support.inquiry.response" "com.study.api_gateway.domain.support.dto.inquiry"
done

for f in $SRC_PATH/dto/support/report/request/*.java; do
    [ -f "$f" ] && move_and_fix "$f" "$SUPPORT/dto/report" \
        "com.study.api_gateway.dto.support.report.request" "com.study.api_gateway.domain.support.dto.report"
done

for f in $SRC_PATH/dto/support/report/response/*.java; do
    [ -f "$f" ] && move_and_fix "$f" "$SUPPORT/dto/report" \
        "com.study.api_gateway.dto.support.report.response" "com.study.api_gateway.domain.support.dto.report"
done

for f in $SRC_PATH/dto/support/faq/response/*.java; do
    [ -f "$f" ] && move_and_fix "$f" "$SUPPORT/dto/faq" \
        "com.study.api_gateway.dto.support.faq.response" "com.study.api_gateway.domain.support.dto.faq"
done

# Enums
[ -f "$SRC_PATH/dto/support/inquiry/InquiryCategory.java" ] && move_and_fix \
    "$SRC_PATH/dto/support/inquiry/InquiryCategory.java" "$SUPPORT/enums" \
    "com.study.api_gateway.dto.support.inquiry" "com.study.api_gateway.domain.support.enums"

[ -f "$SRC_PATH/dto/support/inquiry/InquiryStatus.java" ] && move_and_fix \
    "$SRC_PATH/dto/support/inquiry/InquiryStatus.java" "$SUPPORT/enums" \
    "com.study.api_gateway.dto.support.inquiry" "com.study.api_gateway.domain.support.enums"

[ -f "$SRC_PATH/dto/support/report/ReportStatus.java" ] && move_and_fix \
    "$SRC_PATH/dto/support/report/ReportStatus.java" "$SUPPORT/enums" \
    "com.study.api_gateway.dto.support.report" "com.study.api_gateway.domain.support.enums"

[ -f "$SRC_PATH/dto/support/report/ReferenceType.java" ] && move_and_fix \
    "$SRC_PATH/dto/support/report/ReferenceType.java" "$SUPPORT/enums" \
    "com.study.api_gateway.dto.support.report" "com.study.api_gateway.domain.support.enums"

[ -f "$SRC_PATH/dto/support/report/ReportSortType.java" ] && move_and_fix \
    "$SRC_PATH/dto/support/report/ReportSortType.java" "$SUPPORT/enums" \
    "com.study.api_gateway.dto.support.report" "com.study.api_gateway.domain.support.enums"

[ -f "$SRC_PATH/dto/support/report/SortDirection.java" ] && move_and_fix \
    "$SRC_PATH/dto/support/report/SortDirection.java" "$SUPPORT/enums" \
    "com.study.api_gateway.dto.support.report" "com.study.api_gateway.domain.support.enums"

[ -f "$SRC_PATH/dto/support/faq/FaqCategory.java" ] && move_and_fix \
    "$SRC_PATH/dto/support/faq/FaqCategory.java" "$SUPPORT/enums" \
    "com.study.api_gateway.dto.support.faq" "com.study.api_gateway.domain.support.enums"

# 중복 InquiryStatus 삭제
[ -f "$SRC_PATH/dto/support/report/InquiryStatus.java" ] && \
    echo "  ✓ 중복 InquiryStatus 건너뛰기 (inquiry에 있음)"

echo "✅ Support 완료"

# Shared 패키지
echo ""
echo "📦 Shared 패키지..."
SHARED="$SRC_PATH/shared"

move_and_fix "$SRC_PATH/controller/HealthCheckController.java" "$SHARED/controller" \
    "com.study.api_gateway.controller" "com.study.api_gateway.shared.controller"

move_and_fix "$SRC_PATH/controller/enums/EnumsController.java" "$SHARED/controller" \
    "com.study.api_gateway.controller.enums" "com.study.api_gateway.shared.controller"

echo "✅ Shared 완료"

echo ""
echo "🎉 모든 도메인 마이그레이션 완료!"
echo ""
echo "다음 단계: ./gradlew compileJava로 빌드 확인"
