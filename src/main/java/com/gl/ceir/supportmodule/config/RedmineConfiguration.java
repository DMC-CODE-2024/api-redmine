package com.gl.ceir.supportmodule.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "redmine.config")
@Data
public class RedmineConfiguration {
    private String baseUrl;
    private String endUserApiKey;
    private int projectId;
    private int trackerId;
    private int createdStatusId;
    private String createdStatusName;
    private int progressStatusId;
    private String progressStatusName;
    private int resolvedStatusId;
    private String resolvedStatusName;
    private int closedStatusId;
    private String closedStatusName;
}
