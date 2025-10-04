package com.study.api_gateway.dto.comment.request;

import lombok.Data;

@Data
public class RootCommentCreateRequest {
    private String articleId;
    private String writerId;
    private String contents;
}
