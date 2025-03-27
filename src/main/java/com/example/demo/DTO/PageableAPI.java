package com.example.demo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "페이지네이션 정보")
public class PageableAPI {

    @Schema(description = "페이지 오프셋", example = "0")
    private long offset;

    @Schema(description = "정렬 정보")
    private SortAPI sort;

    @Schema(description = "페이지가 페이징되어 있는지 여부", example = "true")
    private boolean paged;

    @Schema(description = "페이지 크기", example = "10")
    private int pageSize;

    @Schema(description = "페이지 번호", example = "0")
    private int pageNumber;

    @Schema(description = "페이지가 페이징되지 않은 상태인지 여부", example = "false")
    private boolean unpaged;
}