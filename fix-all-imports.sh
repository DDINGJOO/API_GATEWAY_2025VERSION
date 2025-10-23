#!/bin/bash

# 모든 import 문을 수정하는 스크립트

set -e
SRC_PATH="/Users/ddingjoo/IdeaProjects/BanderProject/SERVER/API_GATEWAY/src/main/java/com/study/api_gateway"

echo "🔧 모든 파일의 import 문 수정 중..."

# domain, config, shared 하위의 모든 Java 파일
find "$SRC_PATH" -name "*.java" -type f | while read file; do
    # Common imports
    sed -i '' 's/import com\.study\.api_gateway\.dto\.BaseResponse/import com.study.api_gateway.common.dto.BaseResponse/g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.util\.ResponseFactory/import com.study.api_gateway.common.response.ResponseFactory/g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.util\.RequestPathHelper/import com.study.api_gateway.common.util.RequestPathHelper/g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.config\.GlobalExceptionHandler/import com.study.api_gateway.common.exception.GlobalExceptionHandler/g' "$file"

    # Auth domain
    sed -i '' 's/import com\.study\.api_gateway\.client\.AuthClient/import com.study.api_gateway.domain.auth.AuthClient/g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.auth\.request\./import com.study.api_gateway.domain.auth.dto./g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.auth\.response\./import com.study.api_gateway.domain.auth.dto./g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.auth\.enums\./import com.study.api_gateway.domain.auth.enums./g' "$file"

    # Profile domain
    sed -i '' 's/import com\.study\.api_gateway\.client\.ProfileClient/import com.study.api_gateway.domain.profile.ProfileClient/g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.profile\.request\./import com.study.api_gateway.domain.profile.dto./g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.profile\.response\./import com.study.api_gateway.domain.profile.dto./g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.profile\.ProfileSearchCriteria/import com.study.api_gateway.domain.profile.dto.ProfileSearchCriteria/g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.profile\.enums\./import com.study.api_gateway.domain.profile.enums./g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.util\.ProfileEnrichmentUtil/import com.study.api_gateway.domain.profile.ProfileEnrichmentService/g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.util\.cache\./import com.study.api_gateway.domain.profile.cache./g' "$file"

    # Article domain
    sed -i '' 's/import com\.study\.api_gateway\.client\.ArticleClient/import com.study.api_gateway.domain.article.client.ArticleClient/g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.client\.NoticeClient/import com.study.api_gateway.domain.article.client.NoticeClient/g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.client\.EventClient/import com.study.api_gateway.domain.article.client.EventClient/g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.Article\.request\./import com.study.api_gateway.domain.article.dto./g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.Article\.response\./import com.study.api_gateway.domain.article.dto./g' "$file"

    # Comment domain
    sed -i '' 's/import com\.study\.api_gateway\.client\.CommentClient/import com.study.api_gateway.domain.comment.CommentClient/g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.comment\.request\./import com.study.api_gateway.domain.comment.dto./g' "$file"

    # Like domain (gaechu)
    sed -i '' 's/import com\.study\.api_gateway\.client\.GaechuClient/import com.study.api_gateway.domain.like.LikeClient/g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.gaechu\./import com.study.api_gateway.domain.like.dto./g' "$file"

    # Feed domain (activity)
    sed -i '' 's/import com\.study\.api_gateway\.client\.ActivityClient/import com.study.api_gateway.domain.feed.FeedClient/g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.activity\.request\./import com.study.api_gateway.domain.feed.dto./g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.activity\.response\./import com.study.api_gateway.domain.feed.dto./g' "$file"

    # Image domain
    sed -i '' 's/import com\.study\.api_gateway\.client\.ImageClient/import com.study.api_gateway.domain.image.ImageClient/g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.service\.ImageConfirmService/import com.study.api_gateway.domain.image.ImageConfirmService/g' "$file"

    # Support domain
    sed -i '' 's/import com\.study\.api_gateway\.client\.InquiryClient/import com.study.api_gateway.domain.support.client.InquiryClient/g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.client\.ReportClient/import com.study.api_gateway.domain.support.client.ReportClient/g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.client\.FaqClient/import com.study.api_gateway.domain.support.client.FaqClient/g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.support\.inquiry\.request\./import com.study.api_gateway.domain.support.dto.inquiry./g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.support\.inquiry\.response\./import com.study.api_gateway.domain.support.dto.inquiry./g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.support\.inquiry\./import com.study.api_gateway.domain.support.enums./g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.support\.report\.request\./import com.study.api_gateway.domain.support.dto.report./g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.support\.report\.response\./import com.study.api_gateway.domain.support.dto.report./g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.support\.report\./import com.study.api_gateway.domain.support.enums./g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.support\.faq\.response\./import com.study.api_gateway.domain.support.dto.faq./g' "$file"
    sed -i '' 's/import com\.study\.api_gateway\.dto\.support\.faq\./import com.study.api_gateway.domain.support.enums./g' "$file"

    # ProfileEnrichmentUtil 클래스명 변경
    sed -i '' 's/ProfileEnrichmentUtil /ProfileEnrichmentService /g' "$file"
    sed -i '' 's/private final ProfileEnrichmentUtil/private final ProfileEnrichmentService/g' "$file"

    # GaechuClient → LikeClient
    sed -i '' 's/private final GaechuClient/private final LikeClient/g' "$file"
    sed -i '' 's/GaechuClient gaechuClient/LikeClient likeClient/g' "$file"
    sed -i '' 's/gaechuClient\./likeClient./g' "$file"

    # ActivityClient → FeedClient
    sed -i '' 's/private final ActivityClient/private final FeedClient/g' "$file"
    sed -i '' 's/ActivityClient activityClient/FeedClient feedClient/g' "$file"
    sed -i '' 's/activityClient\./feedClient./g' "$file"
done

echo "✅ Import 수정 완료"
echo ""
echo "🔧 Config 파일의 빈 주입도 수정..."

# Config 파일들의 빈 이름 수정
for config in "$SRC_PATH"/config/*.java; do
    if [ -f "$config" ]; then
        sed -i '' 's/@Qualifier("gaechuWebClient")/@Qualifier("likeWebClient")/g' "$config"
        sed -i '' 's/@Qualifier("activitiesClient")/@Qualifier("feedClient")/g' "$config"
        sed -i '' 's/public WebClient gaechuWebClient/public WebClient likeWebClient/g' "$config"
        sed -i '' 's/public WebClient activitiesClient/public WebClient feedClient/g' "$config"
    fi
done

echo "✅ 모든 수정 완료!"
