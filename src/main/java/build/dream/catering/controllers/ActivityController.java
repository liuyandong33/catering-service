package build.dream.catering.controllers;

import build.dream.catering.models.activity.*;
import build.dream.catering.services.ActivityService;
import build.dream.common.annotations.ApiRestAction;
import build.dream.common.controllers.BasicController;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/activity")
public class ActivityController extends BasicController {
    /**
     * 保存买A赠B活动
     *
     * @return
     */
    @RequestMapping(value = "/saveBuyGiveActivity", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ApiRestAction(modelClass = SaveBuyGiveActivityModel.class, serviceClass = ActivityService.class, serviceMethodName = "saveBuyGiveActivity", error = "保存买A赠B活动失败")
    public String saveBuyGiveActivity() {
        return null;
    }

    /**
     * 保存满减活动
     *
     * @return
     */
    @RequestMapping(value = "/saveFullReductionActivity", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ApiRestAction(modelClass = SaveBuyGiveActivityModel.class, serviceClass = ActivityService.class, serviceMethodName = "saveBuyGiveActivity", error = "保存买A赠B活动失败")
    public String saveFullReductionActivity() {
        return null;
    }

    /**
     * 保存特价商品活动
     *
     * @return
     */
    @RequestMapping(value = "/saveSpecialGoodsActivity", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ApiRestAction(modelClass = SaveSpecialGoodsActivityModel.class, serviceClass = ActivityService.class, serviceMethodName = "saveSpecialGoodsActivity", error = "保存特价商品活动失败")
    public String saveSpecialGoodsActivity() {
        return null;
    }

    /**
     * 查询生效的活动
     *
     * @return
     */
    @RequestMapping(value = "/listEffectiveActivities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ApiRestAction(modelClass = ListEffectiveActivitiesModel.class, serviceClass = ActivityService.class, serviceMethodName = "listEffectiveActivities", error = "查询生效的活动失败")
    public String listEffectiveActivities() {
        return null;
    }

    /**
     * 查询所有生效的整单满减活动
     *
     * @return
     */
    @RequestMapping(value = "/listFullReductionActivities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ApiRestAction(modelClass = ListFullReductionActivitiesModel.class, serviceClass = ActivityService.class, serviceMethodName = "listFullReductionActivities", error = "查询所有生效的整单满减活动失败")
    public String listFullReductionActivities() {
        return null;
    }

    /**
     * 查询所有生效的支付促销活动
     *
     * @return
     */
    @RequestMapping(value = "/listPaymentActivities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ApiRestAction(modelClass = ListPaymentActivitiesModel.class, serviceClass = ActivityService.class, serviceMethodName = "listPaymentActivities", error = "查询所有生效的支付促销活动失败")
    public String listPaymentActivities() {
        return null;
    }
}
