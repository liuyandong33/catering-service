package build.dream.catering.utils;

import build.dream.catering.constants.ConfigurationKeys;
import build.dream.catering.constants.Constants;
import build.dream.catering.constants.RedisKeys;
import build.dream.common.api.ApiRest;
import build.dream.common.utils.*;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AnubisUtils {
    public static String generateSignature(String appId, String data, int salt, String accessToken) throws UnsupportedEncodingException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("app_id=" + appId);
        stringBuilder.append("access_token=" + accessToken);
        stringBuilder.append("data=" + URLEncoder.encode(data, Constants.CHARSET_NAME_UTF_8));
        stringBuilder.append("salt=" + salt);
        return DigestUtils.md5Hex(stringBuilder.toString());
    }

    public static Map<String, Object> obtainAccessToken(String url, String appId, String appSecret) throws IOException {
        int salt = RandomUtils.nextInt(1000, 9999);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("app_id=" + appId);
        stringBuilder.append("&salt=" + salt);
        stringBuilder.append("&secret_key=" + appSecret);
        String signature = DigestUtils.md5Hex(URLEncoder.encode(stringBuilder.toString(), Constants.CHARSET_NAME_UTF_8));
        Map<String, String> obtainAccessTokenRequestParameters = new HashMap<String, String>();
        obtainAccessTokenRequestParameters.put("url", url);
        obtainAccessTokenRequestParameters.put("appId", appId);
        obtainAccessTokenRequestParameters.put("salt", String.valueOf(salt));
        obtainAccessTokenRequestParameters.put("signature", signature);
        ApiRest obtainAccessTokenApiRest = ProxyUtils.doGetWithRequestParameters(Constants.SERVICE_NAME_PLATFORM, "anubis", "obtainAccessToken", obtainAccessTokenRequestParameters);
        ValidateUtils.isTrue(obtainAccessTokenApiRest.isSuccessful(), obtainAccessTokenApiRest.getError());
        return (Map<String, Object>) obtainAccessTokenApiRest.getData();
    }

    public static boolean verifySignature(String appId, String data, int salt, String signature) throws IOException {
        return signature.equals(generateSignature(appId, data, salt, getAccessToken()));
    }

    public static Map<String, Object> obtainAccessToken() throws IOException {
        String appId = ConfigurationUtils.getConfiguration(ConfigurationKeys.ANUBIS_APP_ID);
        String appSecret = ConfigurationUtils.getConfiguration(ConfigurationKeys.ANUBIS_APP_SECRET);
        String url = ConfigurationUtils.getConfiguration(ConfigurationKeys.ANUBIS_SERVICE_URL) + Constants.ANUBIS_GET_ACCESS_TOKEN_URI;
        return obtainAccessToken(url, appId, appSecret);
    }

    public static String getAccessToken() throws IOException {
        String accessToken = null;
        String accessTokenJson = CommonRedisUtils.get(RedisKeys.KEY_ANUBIS_TOKEN);
        boolean isRetrieveAccessToken = false;
        if (StringUtils.isNotBlank(accessTokenJson)) {
            JSONObject accessTokenJsonObject = JSONObject.fromObject(accessTokenJson);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(accessTokenJsonObject.getLong("expire_time"));

            Date currentTime = new Date();
            if (currentTime.before(calendar.getTime())) {
                accessToken = accessTokenJsonObject.getString("access_token");
            } else {
                isRetrieveAccessToken = true;
            }
        } else {
            isRetrieveAccessToken = true;
        }
        if (isRetrieveAccessToken) {
            Map<String, Object> accessTokenMap = obtainAccessToken();
            CommonRedisUtils.set(RedisKeys.KEY_ANUBIS_TOKEN, GsonUtils.toJson(accessTokenMap));
            accessToken = MapUtils.getString(accessTokenMap, "access_token");
        }
        return accessToken;
    }

    public static ApiRest callAnubisSystem(String url, String appId, Map<String, Object> data) throws IOException {
        String accessToken = getAccessToken();
        int salt = RandomUtils.nextInt(1000, 9999);
        String signature = generateSignature(appId, GsonUtils.toJson(data), salt, accessToken);

        Map<String, Object> requestBody = new HashMap<String, Object>();
        requestBody.put("app_id", appId);
        requestBody.put("data", data);
        requestBody.put("salt", salt);
        requestBody.put("signature", signature);

        Map<String, String> callAnubisSystemRequestParameters = new HashMap<String, String>();
        callAnubisSystemRequestParameters.put("url", url);
        callAnubisSystemRequestParameters.put("requestBody", GsonUtils.toJson(requestBody));
        ApiRest apiRest = ProxyUtils.doPostWithRequestParameters(Constants.SERVICE_NAME_PLATFORM, "anubis", "callAnubisSystem", callAnubisSystemRequestParameters);
        return apiRest;
    }
}