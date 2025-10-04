package com.study.api_gateway.dto.comment.request;

import lombok.Data;

@Data
public class CommentUpdateRequest {
    private String writerId;
    private String contents;
}
