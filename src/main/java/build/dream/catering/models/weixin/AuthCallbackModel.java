package build.dream.catering.models.weixin;

import build.dream.common.annotations.InstantiateObjectKey;
import build.dream.common.models.BasicModel;

import javax.validation.constraints.NotNull;

public class AuthCallbackModel extends BasicModel {
    @NotNull
    private String clientType;

    @NotNull
    private Long tenantId;

    @NotNull
    private String componentAppId;

    @NotNull
    @InstantiateObjectKey(name = "auth_code")
    private String authCode;

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getComponentAppId() {
        return componentAppId;
    }

    public void setComponentAppId(String componentAppId) {
        this.componentAppId = componentAppId;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }
}
