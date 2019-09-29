package build.dream.catering.mappers;

import build.dream.catering.beans.PackageDetail;
import build.dream.common.domains.catering.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

@Mapper
public interface GoodsMapper {
    List<PackageDetail> listPackageInfos(@Param("tenantId") BigInteger tenantId,
                                         @Param("branchId") BigInteger branchId,
                                         @Param("packageIds") Collection<BigInteger> packageIds,
                                         @Param("groupType") Integer groupType);

    List<Goods> findAllByIdInList(@Param("tenantId") BigInteger tenantId, @Param("branchId") BigInteger branchId, @Param("goodsIds") List<BigInteger> goodsIds);

    List<Goods> findAllByCategoryId(@Param("tenantId") BigInteger tenantId, @Param("branchId") BigInteger branchId, @Param("categoryId") BigInteger categoryId);

    BigDecimal deductingGoodsStock(@Param("goodsId") BigInteger goodsId,
                                   @Param("goodsSpecificationId") BigInteger goodsSpecificationId,
                                   @Param("quantity") BigDecimal quantity);

    BigDecimal addGoodsStock(@Param("goodsId") BigInteger goodsId,
                             @Param("goodsSpecificationId") BigInteger goodsSpecificationId,
                             @Param("quantity") BigDecimal quantity);

    List<Goods> findAll(@Param("tenantId") BigInteger tenantId, @Param("branchId") BigInteger branchId);
}
