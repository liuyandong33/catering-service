package build.dream.catering.services;

import build.dream.catering.constants.Constants;
import build.dream.catering.models.dietorder.*;
import build.dream.catering.utils.DietOrderUtils;
import build.dream.catering.utils.ThreadUtils;
import build.dream.common.api.ApiRest;
import build.dream.common.auth.AbstractUserDetails;
import build.dream.common.auth.SystemUserUserDetails;
import build.dream.common.auth.VipUserDetails;
import build.dream.common.beans.KafkaFixedTimeSendResult;
import build.dream.common.catering.domains.*;
import build.dream.common.constants.DietOrderConstants;
import build.dream.common.models.alipay.AlipayTradeAppPayModel;
import build.dream.common.models.alipay.AlipayTradePagePayModel;
import build.dream.common.models.alipay.AlipayTradePayModel;
import build.dream.common.models.alipay.AlipayTradeWapPayModel;
import build.dream.common.models.aliyunpush.PushMessageToAndroidModel;
import build.dream.common.models.weixinpay.MicroPayModel;
import build.dream.common.models.weixinpay.UnifiedOrderModel;
import build.dream.common.utils.*;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DietOrderService {
    /**
     * 获取订单明细
     *
     * @param obtainDietOrderInfoModel
     * @return
     */
    @Transactional(readOnly = true)
    public ApiRest obtainDietOrderInfo(ObtainDietOrderInfoModel obtainDietOrderInfoModel) {
        // 查询出订单信息
        BigInteger tenantId = obtainDietOrderInfoModel.obtainTenantId();
        BigInteger branchId = obtainDietOrderInfoModel.obtainBranchId();
        BigInteger dietOrderId = obtainDietOrderInfoModel.getDietOrderId();
        SearchModel dietOrderSearchModel = new SearchModel(true);
        dietOrderSearchModel.addSearchCondition(DietOrder.ColumnName.ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, dietOrderId);
        dietOrderSearchModel.addSearchCondition(DietOrder.ColumnName.TENANT_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, tenantId);
        dietOrderSearchModel.addSearchCondition(DietOrder.ColumnName.BRANCH_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, branchId);
        DietOrder dietOrder = DatabaseHelper.find(DietOrder.class, dietOrderSearchModel);
        ValidateUtils.notNull(dietOrder, "订单不存在！");

        // 查询出订单组信息
        SearchModel dietOrderGroupSearchModel = new SearchModel(true);
        dietOrderGroupSearchModel.addSearchCondition(DietOrderGroup.ColumnName.DIET_ORDER_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, dietOrderId);
        dietOrderGroupSearchModel.addSearchCondition(DietOrderGroup.ColumnName.TENANT_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, tenantId);
        dietOrderGroupSearchModel.addSearchCondition(DietOrderGroup.ColumnName.BRANCH_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, branchId);
        List<DietOrderGroup> dietOrderGroups = DatabaseHelper.findAll(DietOrderGroup.class, dietOrderGroupSearchModel);

        // 查询出订单详情信息
        SearchModel dietOrderDetailSearchModel = new SearchModel(true);
        dietOrderDetailSearchModel.addSearchCondition(DietOrderDetail.ColumnName.DIET_ORDER_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, dietOrderId);
        dietOrderDetailSearchModel.addSearchCondition(DietOrderDetail.ColumnName.TENANT_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, tenantId);
        dietOrderDetailSearchModel.addSearchCondition(DietOrderDetail.ColumnName.BRANCH_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, branchId);
        List<DietOrderDetail> dietOrderDetails = DatabaseHelper.findAll(DietOrderDetail.class, dietOrderDetailSearchModel);

        // 查询出订单口味信息
        SearchModel dietOrderDetailGoodsAttributeSearchModel = new SearchModel(true);
        dietOrderDetailGoodsAttributeSearchModel.addSearchCondition(DietOrderDetailGoodsAttribute.ColumnName.DIET_ORDER_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, dietOrderId);
        dietOrderDetailGoodsAttributeSearchModel.addSearchCondition(DietOrderDetailGoodsAttribute.ColumnName.TENANT_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, tenantId);
        dietOrderDetailGoodsAttributeSearchModel.addSearchCondition(DietOrderDetailGoodsAttribute.ColumnName.BRANCH_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, branchId);
        List<DietOrderDetailGoodsAttribute> dietOrderDetailGoodsAttributes = DatabaseHelper.findAll(DietOrderDetailGoodsAttribute.class, dietOrderDetailGoodsAttributeSearchModel);

        SearchModel dietOrderActivitySearchModel = new SearchModel(true);
        dietOrderActivitySearchModel.addSearchCondition(DietOrderActivity.ColumnName.DIET_ORDER_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, dietOrderId);
        dietOrderActivitySearchModel.addSearchCondition(DietOrderActivity.ColumnName.TENANT_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, tenantId);
        dietOrderActivitySearchModel.addSearchCondition(DietOrderActivity.ColumnName.BRANCH_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, branchId);
        List<DietOrderActivity> dietOrderActivities = DatabaseHelper.findAll(DietOrderActivity.class, dietOrderActivitySearchModel);

        Map<String, Object> dietOrderInfo = DietOrderUtils.buildDietOrderInfo(dietOrder, dietOrderGroups, dietOrderDetails, dietOrderDetailGoodsAttributes, dietOrderActivities);
        return ApiRest.builder().data(dietOrderInfo).message("获取订单信息成功！").successful(true).build();
    }

    /**
     * 保存订单信息
     *
     * @param saveDietOrderModel
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ApiRest saveDietOrder(SaveDietOrderModel saveDietOrderModel) {
        AbstractUserDetails abstractUserDetails = WebSecurityUtils.obtainUserDetails();
        String clientType = abstractUserDetails.getClientType();
        if (Constants.CLIENT_TYPE_POS.equals(clientType) || Constants.CLIENT_TYPE_APP.equals(clientType) || Constants.CLIENT_TYPE_WEB.equals(clientType)) {
            SystemUserUserDetails systemUserUserDetails = (SystemUserUserDetails) abstractUserDetails;
            saveDietOrderModel.setTenantId(systemUserUserDetails.getTenantId());
            saveDietOrderModel.setTenantCode(systemUserUserDetails.getTenantCode());
            saveDietOrderModel.setUserId(systemUserUserDetails.getUserId());
        } else if (Constants.CLIENT_TYPE_O2O.equals(clientType)) {
            VipUserDetails vipUserDetails = (VipUserDetails) abstractUserDetails;
            saveDietOrderModel.setTenantId(vipUserDetails.getTenantId());
            saveDietOrderModel.setTenantCode(vipUserDetails.getTenantCode());
            saveDietOrderModel.setVipId(vipUserDetails.getVipId());
        }

        DietOrder dietOrder = DietOrderUtils.saveDietOrder(saveDietOrderModel);
        return ApiRest.builder().data(dietOrder).message("保存订单成功！").successful(true).build();
    }

    @Transactional(rollbackFor = Exception.class)
    public ApiRest confirmOrder(ConfirmOrderModel confirmOrderModel) {
        BigInteger tenantId = confirmOrderModel.obtainTenantId();
        BigInteger branchId = confirmOrderModel.obtainBranchId();
        BigInteger orderId = confirmOrderModel.getOrderId();

        SearchModel searchModel = new SearchModel(true);
        searchModel.addSearchCondition(DietOrder.ColumnName.TENANT_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, tenantId);
        searchModel.addSearchCondition(DietOrder.ColumnName.BRANCH_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, branchId);
        searchModel.addSearchCondition(DietOrder.ColumnName.ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, orderId);
        DietOrder dietOrder = DatabaseHelper.find(DietOrder.class, searchModel);
        ValidateUtils.notNull(dietOrder, "订单不存在！");
        ValidateUtils.isTrue(dietOrder.getOrderStatus() == DietOrderConstants.ORDER_STATUS_UNPROCESSED, "只有未处理的订单才能进行接单操作！");
        ValidateUtils.isTrue(new Date().getTime() - dietOrder.getActiveTime().getTime() <= 5 * 60 * 100, "订单已超时！");

        DietOrderUtils.stopOrderInvalidJob(dietOrder.getJobId(), dietOrder.getTriggerId());
        dietOrder.setJobId(Constants.VARCHAR_DEFAULT_VALUE);
        dietOrder.setTriggerId(Constants.VARCHAR_DEFAULT_VALUE);
        dietOrder.setOrderStatus(DietOrderConstants.ORDER_STATUS_VALID);
        DatabaseHelper.update(dietOrder);

        return ApiRest.builder().message("接单成功！").successful(true).build();
    }

    /**
     * 商户拒单取消订单
     *
     * @param cancelOrderModel
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ApiRest cancelOrder(CancelOrderModel cancelOrderModel) {
        BigInteger tenantId = cancelOrderModel.obtainTenantId();
        BigInteger branchId = cancelOrderModel.obtainBranchId();
        BigInteger orderId = cancelOrderModel.getOrderId();
        DietOrderUtils.cancelOrder(tenantId, branchId, orderId, 2);
        return ApiRest.builder().message("取消订单成功").successful(true).build();
    }

    /**
     * @param tenantId: 商户ID
     * @param branchId: 门店ID
     * @param orderId:  订单ID
     * @param type:     类型，1-超时未付款自动取消，3-超时未接单自动取消
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(BigInteger tenantId, BigInteger branchId, BigInteger orderId, int type) {
        DietOrderUtils.cancelOrder(tenantId, branchId, orderId, type);
    }

    @Transactional(readOnly = true)
    public ApiRest doPay(DoPayModel doPayModel) {
        BigInteger tenantId = doPayModel.getTenantId();
        BigInteger branchId = doPayModel.getBranchId();
        BigInteger dietOrderId = doPayModel.getDietOrderId();
        Integer paidScene = doPayModel.getPaidScene();
        String authCode = doPayModel.getAuthCode();
        String openId = doPayModel.getOpenId();
        String subOpenId = doPayModel.getSubOpenId();

        SearchModel searchModel = new SearchModel(true);
        searchModel.addSearchCondition(DietOrder.ColumnName.TENANT_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, tenantId);
        searchModel.addSearchCondition(DietOrder.ColumnName.BRANCH_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, branchId);
        searchModel.addSearchCondition(DietOrder.ColumnName.ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, dietOrderId);
        DietOrder dietOrder = DatabaseHelper.find(DietOrder.class, searchModel);
        ValidateUtils.notNull(dietOrder, "订单不存在！");
        ValidateUtils.isTrue(dietOrder.getOrderStatus() == DietOrderConstants.ORDER_STATUS_PENDING && dietOrder.getOrderStatus() == DietOrderConstants.ORDER_STATUS_PENDING, "订单状态异常！");
        ValidateUtils.isTrue(new Date().getTime() - dietOrder.getCreatedTime().getTime() <= 15 * 60 * 1000, "订单已超时！");

        String orderNumber = dietOrder.getOrderNumber();
        BigDecimal payableAmount = dietOrder.getPayableAmount();
        String partitionCode = ConfigurationUtils.getConfiguration(Constants.PARTITION_CODE);
        String serviceDomain = CommonUtils.getServiceDomain(partitionCode, Constants.SERVICE_NAME_CATERING);

        Object result = null;
        if (paidScene == Constants.PAID_SCENE_WEI_XIN_MICROPAY) {
            MicroPayModel microPayModel = MicroPayModel.builder()
                    .tenantId(tenantId.toString())
                    .branchId(branchId.toString())
                    .signType(Constants.MD5)
                    .body("订单支付")
                    .outTradeNo(orderNumber)
                    .totalFee(payableAmount.multiply(Constants.BIG_DECIMAL_ONE_HUNDRED).intValue())
                    .spbillCreateIp(ApplicationHandler.getRemoteAddress())
                    .authCode(authCode)
                    .build();
            result = WeiXinPayUtils.microPay(microPayModel);
        } else if (paidScene == Constants.PAID_SCENE_WEI_XIN_JSAPI_PUBLIC_ACCOUNT || paidScene == Constants.PAID_SCENE_WEI_XIN_NATIVE || paidScene == Constants.PAID_SCENE_WEI_XIN_APP || paidScene == Constants.PAID_SCENE_WEI_XIN_MWEB || paidScene == Constants.PAID_SCENE_WEI_XIN_JSAPI_MINI_PROGRAM) {
            String tradeType = null;
            if (paidScene == Constants.PAID_SCENE_WEI_XIN_JSAPI_PUBLIC_ACCOUNT) {
                tradeType = Constants.WEI_XIN_PAY_TRADE_TYPE_JSAPI;
            } else if (paidScene == Constants.PAID_SCENE_WEI_XIN_NATIVE) {
                tradeType = Constants.WEI_XIN_PAY_TRADE_TYPE_NATIVE;
            } else if (paidScene == Constants.PAID_SCENE_WEI_XIN_APP) {
                tradeType = Constants.WEI_XIN_PAY_TRADE_TYPE_APP;
            } else if (paidScene == Constants.PAID_SCENE_WEI_XIN_MWEB) {
                tradeType = Constants.WEI_XIN_PAY_TRADE_TYPE_MWEB;
            } else if (paidScene == Constants.PAID_SCENE_WEI_XIN_JSAPI_MINI_PROGRAM) {
                tradeType = Constants.WEI_XIN_PAY_TRADE_TYPE_MINI_PROGRAM;
            }
            UnifiedOrderModel unifiedOrderModel = UnifiedOrderModel.builder()
                    .tenantId(tenantId.toString())
                    .branchId(branchId.toString())
                    .signType(Constants.MD5)
                    .body("订单支付")
                    .outTradeNo(orderNumber)
                    .totalFee(payableAmount.multiply(Constants.BIG_DECIMAL_ONE_HUNDRED).intValue())
                    .spbillCreateIp(ApplicationHandler.getRemoteAddress())
                    .notifyUrl(serviceDomain + "/dietOrder/weiXinPayCallback")
                    .tradeType(tradeType)
                    .openId(openId)
                    .subOpenId(subOpenId)
                    .build();
            result = WeiXinPayUtils.unifiedOrder(unifiedOrderModel);
        } else if (paidScene == Constants.PAID_SCENE_ALIPAY_MOBILE_WEBSITE) {
            String returnUrl = "";

            AlipayTradeWapPayModel alipayTradeWapPayModel = AlipayTradeWapPayModel.builder()
                    .tenantId(tenantId.toString())
                    .branchId(branchId.toString())
                    .returnUrl(returnUrl)
                    .notifyUrl(serviceDomain + "/dietOrder/alipayCallback")
                    .subject("订单支付")
                    .outTradeNo(orderNumber)
                    .totalAmount(payableAmount)
                    .productCode(orderNumber)
                    .build();
            result = AlipayUtils.alipayTradeWapPay(alipayTradeWapPayModel);
        } else if (paidScene == Constants.PAID_SCENE_ALIPAY_PC_WEBSITE) {
            String returnUrl = "";

            AlipayTradePagePayModel alipayTradePagePayModel = AlipayTradePagePayModel.builder()
                    .tenantId(tenantId.toString())
                    .branchId(branchId.toString())
                    .returnUrl(returnUrl)
                    .notifyUrl(serviceDomain + "/dietOrder/alipayCallback")
                    .outTradeNo(orderNumber)
                    .productCode(orderNumber)
                    .totalAmount(payableAmount)
                    .subject("订单支付")
                    .build();
            result = AlipayUtils.alipayTradePagePay(alipayTradePagePayModel);
        } else if (paidScene == Constants.PAID_SCENE_ALIPAY_APP) {
            AlipayTradeAppPayModel alipayTradeAppPayModel = AlipayTradeAppPayModel.builder()
                    .tenantId(tenantId.toString())
                    .branchId(branchId.toString())
                    .notifyUrl(serviceDomain + "/dietOrder/alipayCallback")
                    .outTradeNo(orderNumber)
                    .totalAmount(payableAmount)
                    .subject("订单支付")
                    .build();
            result = AlipayUtils.alipayTradeAppPay(alipayTradeAppPayModel);
        } else if (paidScene == Constants.PAID_SCENE_ALIPAY_FAC_TO_FACE) {
            AlipayTradePayModel alipayTradePayModel = AlipayTradePayModel.builder()
                    .tenantId(tenantId.toString())
                    .branchId(branchId.toString())
                    .notifyUrl(serviceDomain + "/dietOrder/alipayCallback")
                    .outTradeNo(orderNumber)
                    .totalAmount(payableAmount)
                    .scene(Constants.SCENE_BAR_CODE)
                    .authCode(authCode)
                    .subject("订单支付")
                    .build();
            result = AlipayUtils.alipayTradePay(alipayTradePayModel);
        }

        return ApiRest.builder().data(result).message("发起支付成功！").successful(true).build();
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleCallback(Map<String, String> parameters, String paymentCode) throws ParseException {
        String orderNumber = null;
        Date occurrenceTime = null;
        BigDecimal totalAmount = null;
        if (Constants.PAYMENT_CODE_ALIPAY.equals(paymentCode)) {
            orderNumber = parameters.get("out_trade_no");
            occurrenceTime = new SimpleDateFormat(Constants.DEFAULT_DATE_PATTERN).parse(parameters.get("gmt_payment"));
            totalAmount = BigDecimal.valueOf(Double.valueOf(parameters.get("total_amount")));
        } else if (Constants.PAYMENT_CODE_WX.equals(paymentCode)) {
            orderNumber = "";
        }

        SearchModel dietOrderSearchModel = new SearchModel(true);
        dietOrderSearchModel.addSearchCondition(DietOrder.ColumnName.ORDER_NUMBER, Constants.SQL_OPERATION_SYMBOL_EQUAL, orderNumber);
        DietOrder dietOrder = DatabaseHelper.find(DietOrder.class, dietOrderSearchModel);
        ValidateUtils.notNull(dietOrder, "订单不存在！");
        if (dietOrder.getOrderStatus() == DietOrderConstants.PAY_STATUS_PAID) {
            return;
        }

        BigInteger tenantId = dietOrder.getTenantId();
        String tenantCode = dietOrder.getTenantCode();
        BigInteger branchId = dietOrder.getBranchId();

        SearchModel paymentSearchModel = new SearchModel(true);
        paymentSearchModel.addSearchCondition(Payment.ColumnName.TENANT_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, tenantId);
        paymentSearchModel.addSearchCondition(Payment.ColumnName.BRANCH_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, branchId);
        paymentSearchModel.addSearchCondition(Payment.ColumnName.CODE, Constants.SQL_OPERATION_SYMBOL_EQUAL, paymentCode);
        Payment payment = DatabaseHelper.find(Payment.class, paymentSearchModel);

        DietOrderPayment dietOrderPayment = DietOrderPayment.builder()
                .tenantId(tenantId)
                .tenantCode(tenantCode)
                .branchId(branchId)
                .dietOrderId(dietOrder.getId())
                .paymentId(payment.getId())
                .paymentCode(payment.getCode())
                .paymentName(payment.getName())
                .paidAmount(totalAmount)
                .occurrenceTime(occurrenceTime)
                .extraInfo(GsonUtils.toJson(parameters))
                .build();
        DatabaseHelper.insert(dietOrderPayment);

        dietOrder.setPaidAmount(dietOrder.getPaidAmount().add(totalAmount));
        dietOrder.setPayStatus(DietOrderConstants.PAY_STATUS_PAID);
        dietOrder.setOrderStatus(DietOrderConstants.ORDER_STATUS_UNPROCESSED);
        dietOrder.setActiveTime(occurrenceTime);

        BigInteger userId = CommonUtils.getServiceSystemUserId();
        dietOrder.setUpdatedUserId(userId);
        DietOrderUtils.stopOrderInvalidJob(dietOrder.getJobId(), dietOrder.getTriggerId());
        KafkaFixedTimeSendResult kafkaFixedTimeSendResult = DietOrderUtils.startOrderInvalidJob(tenantId, branchId, dietOrder.getId(), 3, DateUtils.addMinutes(occurrenceTime, 5));
        dietOrder.setJobId(kafkaFixedTimeSendResult.getJobId());
        dietOrder.setTriggerId(kafkaFixedTimeSendResult.getTriggerId());
        DatabaseHelper.update(dietOrder);
    }

    /**
     * 获取POS订单
     *
     * @param obtainPosOrderModel
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ApiRest obtainPosOrder(ObtainPosOrderModel obtainPosOrderModel) {
        BigInteger tenantId = obtainPosOrderModel.obtainTenantId();
        BigInteger branchId = obtainPosOrderModel.obtainBranchId();
        String tableCode = obtainPosOrderModel.obtainBranchCode();
        BigInteger vipId = obtainPosOrderModel.getVipId();
        PushMessageToAndroidModel pushMessageToAndroidModel = new PushMessageToAndroidModel();
        pushMessageToAndroidModel.setAppKey("");
        pushMessageToAndroidModel.setTarget(AliyunPushUtils.TAG);
        pushMessageToAndroidModel.setTargetValue("POS" + tenantId + "_" + branchId);
        pushMessageToAndroidModel.setTitle("获取POS订单");

        Map<String, Object> body = new HashMap<String, Object>();
        body.put("code", "");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("tableCode", tableCode);

        String uuid = UUID.randomUUID().toString();
        map.put("uuid", uuid);

        body.put("data", map);

        pushMessageToAndroidModel.setBody(GsonUtils.toJson(body));
        Map<String, Object> result = AliyunPushUtils.pushMessageToAndroid(pushMessageToAndroidModel);

        String dataJson = null;
        int times = 0;
        while (times < 120) {
            times += 1;
            dataJson = CommonRedisUtils.get(uuid);
            if (StringUtils.isNotBlank(dataJson)) {
                break;
            }
            ThreadUtils.sleepSafe(500);
        }

        ValidateUtils.notBlank(dataJson, "POS端未响应");

        Map<String, Object> dataMap = JacksonUtils.readValueAsMap(dataJson, String.class, Object.class);

        /*String platformPrivateKey = ConfigurationUtils.getConfiguration(Constants.PLATFORM_PRIVATE_KEY);
        PrivateKey privateKey = RSAUtils.restorePrivateKey(platformPrivateKey);
        String encryptedData = Base64.encodeBase64String(RSAUtils.encryptByPrivateKey(dataJson.getBytes(Constants.CHARSET_NAME_UTF_8), privateKey, PADDING_MODE_RSA_ECB_PKCS1PADDING));
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("order", dataMap.get("order"));
        data.put("encryptedData", encryptedData);*/

        String order = MapUtils.getString(dataMap, "order");
        String orderGroups = MapUtils.getString(dataMap, "orderGroups");
        String orderDetails = MapUtils.getString(dataMap, "orderDetails");
        String orderDetailGoodsAttributes = MapUtils.getString(dataMap, "orderDetailGoodsAttributes");
        String orderActivities = MapUtils.getString(dataMap, "orderActivities");

        DietOrder dietOrder = JacksonUtils.readValue(order, DietOrder.class);
        List<DietOrderGroup> dietOrderGroups = JacksonUtils.readValueAsList(orderGroups, DietOrderGroup.class);
        List<DietOrderDetail> dietOrderDetails = JacksonUtils.readValueAsList(orderDetails, DietOrderDetail.class);
        DatabaseHelper.insert(dietOrder);

        BigInteger dietOrderId = dietOrder.getId();

        Map<String, DietOrderGroup> dietOrderGroupMap = new HashMap<String, DietOrderGroup>();
        for (DietOrderGroup dietOrderGroup : dietOrderGroups) {
            dietOrderGroupMap.put(dietOrder.getLocalId(), dietOrderGroup);
            dietOrderGroup.setDietOrderId(dietOrderId);
        }

        DatabaseHelper.insertAll(dietOrderGroups);

        Map<String, DietOrderDetail> dietOrderDetailMap = new HashMap<String, DietOrderDetail>();
        for (DietOrderDetail dietOrderDetail : dietOrderDetails) {
            DietOrderGroup dietOrderGroup = dietOrderGroupMap.get(dietOrderDetail.getLocalDietOrderGroupId());
            dietOrderDetail.setDietOrderId(dietOrderId);
            dietOrderDetail.setDietOrderGroupId(dietOrderGroup.getId());
            dietOrderDetailMap.put(dietOrderDetail.getLocalId(), dietOrderDetail);
        }

        DatabaseHelper.insertAll(dietOrderDetails);

        List<DietOrderDetailGoodsAttribute> dietOrderDetailGoodsAttributes = null;
        if (StringUtils.isNotBlank(orderDetailGoodsAttributes)) {
            dietOrderDetailGoodsAttributes = JacksonUtils.readValueAsList(orderDetailGoodsAttributes, DietOrderDetailGoodsAttribute.class);
            for (DietOrderDetailGoodsAttribute dietOrderDetailGoodsAttribute : dietOrderDetailGoodsAttributes) {
                DietOrderGroup dietOrderGroup = dietOrderGroupMap.get(dietOrderDetailGoodsAttribute.getLocalDietOrderGroupId());
                DietOrderDetail dietOrderDetail = dietOrderDetailMap.get(dietOrderDetailGoodsAttribute.getLocalDietOrderDetailId());

                dietOrderDetailGoodsAttribute.setDietOrderId(dietOrderId);
                dietOrderDetailGoodsAttribute.setDietOrderGroupId(dietOrderGroup.getId());
                dietOrderDetailGoodsAttribute.setDietOrderDetailId(dietOrderDetail.getId());
            }
            DatabaseHelper.insertAll(dietOrderDetailGoodsAttributes);
        }

        List<DietOrderActivity> dietOrderActivities = null;
        if (StringUtils.isNotBlank(orderActivities)) {
            dietOrderActivities = JacksonUtils.readValueAsList(orderActivities, DietOrderActivity.class);
            for (DietOrderActivity dietOrderActivity : dietOrderActivities) {
                dietOrderActivity.setDietOrderId(dietOrderId);
            }
            DatabaseHelper.insertAll(dietOrderActivities);
        }

        Map<String, Object> dietOrderInfo = DietOrderUtils.buildDietOrderInfo(dietOrder, dietOrderGroups, dietOrderDetails, dietOrderDetailGoodsAttributes, dietOrderActivities);
        return ApiRest.builder().data(dietOrderInfo).message("获取POS订单成功！").successful(true).build();
    }
}
