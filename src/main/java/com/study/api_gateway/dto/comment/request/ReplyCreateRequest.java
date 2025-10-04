package com.study.api_gateway.dto.comment.request;

import lombok.Data;

@Data
public class ReplyCreateRequest {
    private String writerId;
    private String contents;
}
