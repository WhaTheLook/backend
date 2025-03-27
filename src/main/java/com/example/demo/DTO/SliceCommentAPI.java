package com.example.demo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "댓글 목록을 담고 있는 슬라이스 응답")
public class SliceCommentAPI {

    @Schema(description = "댓글 리스트")
    private List<CommentResponse> content;

    @Schema(description = "페이지 크기", example = "10")
    private int size;

    @Schema(description = "현재 페이지 번호", example = "0")
    private int number;

    @Schema(description = "정렬 정보")
    private SortAPI sort;

    @Schema(description = "첫 번째 페이지 여부", example = "true")
    private boolean first;

    @Schema(description = "마지막 페이지 여부", example = "false")
    private boolean last;

    @Schema(description = "현재 페이지의 게시글 개수", example = "10")
    private int numberOfElements;

    @Schema(description = "페이지네이션 정보")
    private PageableAPI pageable;

    @Schema(description = "현재 페이지가 비어 있는지 여부", example = "false")
    private boolean empty;
}