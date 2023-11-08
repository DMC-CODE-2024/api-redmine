package com.gl.ceir.supportmodule.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class PaginatedResponse<T> {
    private List<IssueResponse> data;
    private int currentPage;
    private int totalPages;
    private int numberOfElements;
    private long totalElements;
}
