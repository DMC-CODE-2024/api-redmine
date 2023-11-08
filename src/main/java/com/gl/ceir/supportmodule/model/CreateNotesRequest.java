package com.gl.ceir.supportmodule.model;

import lombok.Data;

@Data
public class CreateNotesRequest {
    private String notes;
    private Boolean privateNotes = false;
}
