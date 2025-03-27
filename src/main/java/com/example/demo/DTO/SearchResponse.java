package com.example.demo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Slice;

@Data
@Builder
@Schema(description = "검색된 게시물 반환 정보")
public class SearchResponse {

    @Schema(description = "검색된 게시물 개수", example = "10")
    private long total;
    @Schema(description = "검색된 게시물 목록")
    private Slice<PostResponse> posts;
}
