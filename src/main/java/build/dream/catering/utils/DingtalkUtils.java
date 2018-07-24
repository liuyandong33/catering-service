package build.dream.catering.utils;

import build.dream.catering.constants.Constants;
import build.dream.common.api.ApiRest;
import build.dream.common.beans.WebResponse;
import build.dream.common.utils.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DingtalkUtils {
    private static final String DINGTALK_UTILS_SIMPLE_NAME = "DingtalkUtils";
    private static final Map<String, String> HEADERS = new HashMap<String, String>();

    static {
        HEADERS.put("Content-Type", "application/json;charset=UTF-8");
    }

    public static String obtainAccessToken() throws IOException {
        String accessToken = null;
        String tokenJson = CacheUtils.get(Constants.KEY_DINGTALK_TOKEN);
        boolean isRetrieveAccessToken = false;
        if (StringUtils.isNotBlank(tokenJson)) {
            JSONObject tokenJsonObject = JSONObject.fromObject(tokenJson);
            long fetchTime = tokenJsonObject.getLong("fetch_time");
            long expiresIn = tokenJsonObject.getLong("expires_in");
            if ((System.currentTimeMillis() - fetchTime) / 1000 >= expiresIn) {
                isRetrieveAccessToken = true;
            } else {
                accessToken = tokenJsonObject.getString("access_token");
            }
        } else {
            isRetrieveAccessToken = true;
        }
        if (isRetrieveAccessToken) {
            String corpId = ConfigurationUtils.getConfiguration(Constants.DINGTALK_CORP_ID);
            String corpSecret = ConfigurationUtils.getConfiguration(Constants.DINGTALK_CORP_SECRET);
            String url = ConfigurationUtils.getConfiguration(Constants.DINGTALK_SERVICE_URL) + Constants.DINGTALK_GET_TOKEN_URI + "?corpid=" + corpId + "&corpsecret=" + corpSecret;
            WebResponse webResponse = OutUtils.doGetWithRequestParameters(url, null, null);
            JSONObject resultJsonObject = JSONObject.fromObject(webResponse.getResult());
            int errcode = resultJsonObject.getInt("errcode");
            Validate.isTrue(errcode == 0, resultJsonObject.optString("errmsg"));

            Map<String, Object> tokenMap = new HashMap<String, Object>();
            accessToken = resultJsonObject.getString("access_token");
            tokenMap.put("access_token", accessToken);
            tokenMap.put("expires_in", resultJsonObject.getLong("expires_in"));
            tokenMap.put("fetch_time", System.currentTimeMillis());
            CacheUtils.set(Constants.KEY_DINGTALK_TOKEN, GsonUtils.toJson(tokenMap));
        }
        return accessToken;
    }

    public static ApiRest send(String sender, String chatId, String content) throws IOException {
        Map<String, Object> sendRequestBody = new HashMap<String, Object>();
        sendRequestBody.put("sender", sender);
        sendRequestBody.put("chatId", chatId);
        sendRequestBody.put("msgtype", "text");
        Map<String, Object> textMap = new HashMap<String, Object>();
        textMap.put("content", content);
        sendRequestBody.put("text", textMap);
        String url = ConfigurationUtils.getConfiguration(Constants.DINGTALK_SERVICE_URL) + Constants.DINGTALK_CHAT_SEND_URI + "?access_token=" + obtainAccessToken();
        WebResponse webResponse = OutUtils.doPostWithRequestBody(url, GsonUtils.toJson(sendRequestBody), HEADERS);
        JSONObject resultJsonObject = JSONObject.fromObject(webResponse.getResult());
        int errcode = resultJsonObject.getInt("errcode");
        Validate.isTrue(errcode == 0, resultJsonObject.optString("errmsg"));

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("messageId", resultJsonObject.getString("messageId"));

        return new ApiRest(data, "发送群消息成功！");
    }

    public static void send(String content) {
        try {
            String sender = ConfigurationUtils.getConfiguration(Constants.DINGTALK_SENDER);
            String chatId = ConfigurationUtils.getConfiguration(Constants.DINGTALK_CHAT_ID);
            send(sender, chatId, content);
        } catch (Exception e) {
            LogUtils.error("发送钉钉消息失败", DINGTALK_UTILS_SIMPLE_NAME, "send", e);
        }
    }
}
