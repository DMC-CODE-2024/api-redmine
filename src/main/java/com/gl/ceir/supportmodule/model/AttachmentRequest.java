package com.gl.ceir.supportmodule.model;

import lombok.Data;

@Data
public class AttachmentRequest {
    private String token;
    private String filename;
    private String contentType;
}
