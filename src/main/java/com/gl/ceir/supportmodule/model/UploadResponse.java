package com.gl.ceir.supportmodule.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UploadResponse {
    private UploadData upload;

    @Data
    @Builder
    @AllArgsConstructor
    public static class UploadData {
        private String token;
    }
}
