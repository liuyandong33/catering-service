package build.dream.erp.models.meituan;

import build.dream.common.models.BasicModel;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;

public class PullMeiTuanOrderModel extends BasicModel {
    @NotNull
    private BigInteger tenantId;

    @NotNull
    private BigInteger branchId;

    @NotNull
    private BigInteger meiTuanOrderId;

    public BigInteger getTenantId() {
        return tenantId;
    }

    public void setTenantId(BigInteger tenantId) {
        this.tenantId = tenantId;
    }

    public BigInteger getBranchId() {
        return branchId;
    }

    public void setBranchId(BigInteger branchId) {
        this.branchId = branchId;
    }

    public BigInteger getMeiTuanOrderId() {
        return meiTuanOrderId;
    }

    public void setMeiTuanOrderId(BigInteger meiTuanOrderId) {
        this.meiTuanOrderId = meiTuanOrderId;
    }
}
