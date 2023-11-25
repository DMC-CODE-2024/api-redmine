package com.gl.ceir.supportmodule.dto;

import lombok.Data;

@Data
public class AttachmentRequest {
    private String token;
    private String filename;
    private String contentType;
}
