package build.dream.catering.utils;

import build.dream.catering.constants.Constants;
import build.dream.common.api.ApiRest;
import build.dream.common.beans.WebResponse;
import build.dream.common.beans.WeiXinAccessToken;
import build.dream.common.saas.domains.WeiXinPublicAccount;
import build.dream.common.utils.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WeiXinUtils {
    public static WeiXinPublicAccount obtainWeiXinPublicAccount(String tenantId) {
        Map<String, String> obtainWeiXinPublicAccountRequestParameters = new HashMap<String, String>();
        obtainWeiXinPublicAccountRequestParameters.put("tenantId", tenantId);
        ApiRest apiRest = ProxyUtils.doGetWithRequestParameters(Constants.SERVICE_NAME_PLATFORM, "weiXin", "obtainWeiXinPublicAccount", obtainWeiXinPublicAccountRequestParameters);
        ValidateUtils.isTrue(apiRest.isSuccessful(), apiRest.getError());
        WeiXinPublicAccount weiXinPublicAccount = (WeiXinPublicAccount) apiRest.getData();
        return weiXinPublicAccount;
    }

    public static String obtainAccessToken(String appId, String appSecret) {
        Map<String, String> obtainAccessTokenRequestParameters = new HashMap<String, String>();
        obtainAccessTokenRequestParameters.put("appId", appId);
        obtainAccessTokenRequestParameters.put("appSecret", appSecret);

        ApiRest apiRest = ProxyUtils.doGetWithRequestParameters(Constants.SERVICE_NAME_OUT, "weiXin", "obtainAccessToken", obtainAccessTokenRequestParameters);
        ValidateUtils.isTrue(apiRest.isSuccessful(), apiRest.getError());

        WeiXinAccessToken weiXinAccessToken = (WeiXinAccessToken) apiRest.getData();
        return weiXinAccessToken.getAccessToken();
    }

    public static void updateMemberBonus(String tenantId, String code, String cardId, Integer bonus, Integer addBonus, String recordBonus) throws IOException {
        WeiXinPublicAccount weiXinPublicAccount = obtainWeiXinPublicAccount(tenantId);
        Validate.notNull(weiXinPublicAccount, "未配置微信公众号，不能更新会员积分！");

        Map<String, Object> updateUserRequestBody = new HashMap<String, Object>();
        updateUserRequestBody.put("code", code);
        updateUserRequestBody.put("card_id", cardId);
        if (bonus != null) {
            updateUserRequestBody.put("bonus", bonus);
        }
        if (addBonus != null) {
            updateUserRequestBody.put("add_bonus", addBonus);
        }
        if (StringUtils.isNotBlank(recordBonus)) {
            updateUserRequestBody.put("record_bonus", recordBonus);
        }

        String accessToken = obtainAccessToken(weiXinPublicAccount.getAppId(), weiXinPublicAccount.getAppSecret());
        String url = "https://api.weixin.qq.com/card/membercard/updateuser?access_token=" + accessToken;
        WebResponse webResponse = OutUtils.doPostWithRequestBody(url, null, GsonUtils.toJson(updateUserRequestBody, false));
        JSONObject resultJsonObject = JSONObject.fromObject(webResponse.getResult());

        Validate.isTrue(resultJsonObject.getInt("errcode") == 0, resultJsonObject.getString("errmsg"));
    }

    public static void updateMemberBonusSafe(String tenantId, String code, String cardId, Integer bonus, Integer addBonus, String recordBonus) {
        try {
            updateMemberBonus(tenantId, code, cardId, bonus, addBonus, recordBonus);
        } catch (Exception e) {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("tenantId", tenantId);
            parameters.put("code", code);
            parameters.put("cardId", cardId);
            parameters.put("bonus", bonus == null ? null : bonus.toString());
            parameters.put("addBonus", addBonus == null ? null : addBonus.toString());
            parameters.put("recordBonus", recordBonus);
            LogUtils.error("更新微信会员积分失败", WeiXinUtils.class.getName(), "updateMemberBonusSafe", e, parameters);
        }
    }
}
