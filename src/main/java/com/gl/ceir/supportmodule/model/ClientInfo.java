package com.gl.ceir.supportmodule.model;

import com.gl.ceir.supportmodule.Constants.ClientTypeEnum;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@RequestScope
@Component
public class ClientInfo {
    private static ThreadLocal<ClientTypeEnum> clientType = new ThreadLocal<>();
    private static ThreadLocal<String> clientId = new ThreadLocal<>();

    public static void setClientInfo(ClientTypeEnum name, String apiKey) {
        clientType.set(name);
        clientId.set(apiKey);
    }

    public static ClientTypeEnum getClientType() {
        return clientType.get();
    }

    public static String getClientId() {
        return clientId.get();
    }

    public static void clearClientInfo() {
        clientType.remove();
        clientId.remove();
    }
}

