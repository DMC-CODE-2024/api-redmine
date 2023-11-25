package com.gl.ceir.supportmodule.dto;

import com.gl.ceir.supportmodule.Constants.ClientTypeEnum;
import com.gl.ceir.supportmodule.Constants.Constant;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.util.Objects;

@Data
@Builder
@MappedSuperclass
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_type")
    private ClientTypeEnum userType;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    public void onCreate() {
        String defaultClientId = Constant.DEFAULT_CLIENT_NAME;
        ClientTypeEnum defaultClientType = ClientTypeEnum.SYSTEM;

        ClientTypeEnum clientType = ClientInfo.getClientType();
        String clientId = ClientInfo.getClientId();
        if (Objects.nonNull(clientId) && Objects.nonNull(clientType)) {
            defaultClientId = clientId;
            defaultClientType = clientType;
        }

        this.setCreatedBy(defaultClientId);
        this.setUpdatedBy(defaultClientId);
        this.setUserType(defaultClientType);
        this.setUserId(defaultClientId);
    }

    @PreUpdate
    public void onUpdate() {
        String defaultClientId = Constant.DEFAULT_CLIENT_NAME;

        String clientId = ClientInfo.getClientId();
        if (Objects.nonNull(clientId)) {
            defaultClientId = clientId;
        }

        this.setUpdatedBy(defaultClientId);
    }
}
