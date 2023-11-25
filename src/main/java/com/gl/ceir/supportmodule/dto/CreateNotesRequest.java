package com.gl.ceir.supportmodule.dto;

import lombok.Data;

@Data
public class CreateNotesRequest {
    private String notes;
    private Boolean privateNotes = false;
}
