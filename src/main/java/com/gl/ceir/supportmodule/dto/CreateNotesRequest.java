package com.gl.ceir.supportmodule.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateNotesRequest {
    private String notes;
    private Boolean privateNotes = false;
    private List<AttachmentRequest> attachments;
}
