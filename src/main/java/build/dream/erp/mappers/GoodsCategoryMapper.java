package build.dream.erp.mappers;

import build.dream.common.erp.domains.GoodsCategory;
import build.dream.common.utils.SearchModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GoodsCategoryMapper {
    long insert(GoodsCategory goodsCategory);
    GoodsCategory find(SearchModel searchModel);
    List<GoodsCategory> findAll(SearchModel searchModel);
    List<GoodsCategory> findAllPaged(SearchModel searchModel);
}