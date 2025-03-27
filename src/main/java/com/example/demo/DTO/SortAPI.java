package com.example.demo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "정렬 정보")
public class SortAPI {

    @Schema(description = "정렬 옵션이 비어 있는지 여부", example = "true")
    private boolean empty;

    @Schema(description = "정렬되지 않은 상태인지 여부", example = "true")
    private boolean unsorted;

    @Schema(description = "정렬된 상태인지 여부", example = "false")
    private boolean sorted;
}