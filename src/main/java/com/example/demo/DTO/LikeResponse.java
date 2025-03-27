package com.example.demo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "좋아요 상태 응답")
public class LikeResponse {
    @Schema(description = "좋아요 여부", example = "true")
    private boolean likeYN;
    @Schema(description = "좋아요 개수", example = "100")
    private Long likeCount;
}
