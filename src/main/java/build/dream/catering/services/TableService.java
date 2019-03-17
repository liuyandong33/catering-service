package build.dream.catering.services;

import build.dream.catering.constants.Constants;
import build.dream.catering.models.table.ListBranchTablesModel;
import build.dream.catering.models.table.SaveBranchTableModel;
import build.dream.catering.models.table.SaveTableAreaModel;
import build.dream.common.api.ApiRest;
import build.dream.common.catering.domains.BranchTable;
import build.dream.common.catering.domains.TableArea;
import build.dream.common.utils.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TableService {
    /**
     * 保存桌台区域
     *
     * @param saveTableAreaModel
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ApiRest saveTableArea(SaveTableAreaModel saveTableAreaModel) {
        BigInteger tenantId = saveTableAreaModel.obtainTenantId();
        String tenantCode = saveTableAreaModel.obtainTenantCode();
        BigInteger branchId = saveTableAreaModel.obtainBranchId();
        BigInteger id = saveTableAreaModel.getId();
        String name = saveTableAreaModel.getName();
        BigInteger userId = saveTableAreaModel.obtainUserId();

        TableArea tableArea = null;
        if (id == null) {
            tableArea = TableArea.builder()
                    .tenantId(tenantId)
                    .tenantCode(tenantCode)
                    .branchId(branchId)
                    .name(name)
                    .createdUserId(userId)
                    .updatedUserId(userId)
                    .updatedRemark("新增桌台区域！")
                    .build();
            DatabaseHelper.insert(tableArea);
        } else {
            SearchModel searchModel = new SearchModel(true);
            searchModel.addSearchCondition(TableArea.ColumnName.TENANT_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, tenantId);
            searchModel.addSearchCondition(TableArea.ColumnName.BRANCH_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, branchId);
            searchModel.addSearchCondition(TableArea.ColumnName.ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, id);
            tableArea = DatabaseHelper.find(TableArea.class, searchModel);
            ValidateUtils.notNull(tableArea, "桌台区域不存在！");

            tableArea.setName(name);
            tableArea.setUpdatedUserId(userId);
            tableArea.setUpdatedRemark("修改桌台区域！");
            DatabaseHelper.update(tableArea);
        }
        return ApiRest.builder().data(tableArea).message("保存桌台区域成功！").successful(true).build();
    }

    /**
     * 保存桌台
     *
     * @param saveBranchTableModel
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ApiRest saveBranchTable(SaveBranchTableModel saveBranchTableModel) {
        BigInteger tenantId = saveBranchTableModel.obtainTenantId();
        String tenantCode = saveBranchTableModel.obtainTenantCode();
        BigInteger branchId = saveBranchTableModel.obtainBranchId();
        BigInteger id = saveBranchTableModel.getId();
        BigInteger tableAreaId = saveBranchTableModel.getTableAreaId();
        String code = saveBranchTableModel.getCode();
        String name = saveBranchTableModel.getName();
        Integer status = saveBranchTableModel.getStatus();
        Integer dinnersNumber = saveBranchTableModel.getDinnersNumber();
        BigInteger userId = saveBranchTableModel.obtainUserId();

        BranchTable branchTable = null;
        if (id == null) {
            branchTable = BranchTable.builder()
                    .tenantId(tenantId)
                    .tenantCode(tenantCode)
                    .branchId(branchId)
                    .tableAreaId(tableAreaId)
                    .code(code)
                    .name(name)
                    .status(status)
                    .dinnersNumber(dinnersNumber)
                    .createdUserId(userId)
                    .updatedUserId(userId)
                    .updatedRemark("新增桌台！")
                    .build();
            DatabaseHelper.insert(branchTable);
        } else {
            SearchModel searchModel = new SearchModel(true);
            searchModel.addSearchCondition(BranchTable.ColumnName.TENANT_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, tenantId);
            searchModel.addSearchCondition(BranchTable.ColumnName.BRANCH_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, branchId);
            searchModel.addSearchCondition(BranchTable.ColumnName.ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, id);
            branchTable = DatabaseHelper.find(BranchTable.class, searchModel);
            ValidateUtils.notNull(branchTable, "桌台不存在！");

            branchTable.setCode(code);
            branchTable.setName(name);
            branchTable.setStatus(status);
            branchTable.setDinnersNumber(dinnersNumber);
            branchTable.setUpdatedUserId(userId);
            branchTable.setUpdatedRemark("修改桌台信息！");
            DatabaseHelper.update(branchTable);
        }
        return ApiRest.builder().data(branchTable).message("保存桌台信息成功！").successful(true).build();
    }

    /**
     * 分页查询桌台信息
     *
     * @param listBranchTablesModel
     * @return
     */
    @Transactional(readOnly = true)
    public ApiRest listBranchTables(ListBranchTablesModel listBranchTablesModel) {
        BigInteger tenantId = listBranchTablesModel.obtainTenantId();
        BigInteger branchId = listBranchTablesModel.obtainBranchId();
        BigInteger tableAreaId = listBranchTablesModel.getTableAreaId();
        int page = listBranchTablesModel.getPage();
        int rows = listBranchTablesModel.getRows();

        List<SearchCondition> searchConditions = new ArrayList<SearchCondition>();
        searchConditions.add(new SearchCondition(BranchTable.ColumnName.DELETED, Constants.SQL_OPERATION_SYMBOL_EQUAL, 0));
        searchConditions.add(new SearchCondition(BranchTable.ColumnName.TENANT_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, tenantId));
        searchConditions.add(new SearchCondition(BranchTable.ColumnName.BRANCH_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, branchId));
        searchConditions.add(new SearchCondition(BranchTable.ColumnName.TABLE_AREA_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, tableAreaId));

        SearchModel searchModel = new SearchModel();
        searchModel.setSearchConditions(searchConditions);
        long count = DatabaseHelper.count(BranchTable.class, searchModel);

        List<BranchTable> branchTables = null;
        if (count > 0) {
            PagedSearchModel pagedSearchModel = new PagedSearchModel();
            pagedSearchModel.setSearchConditions(searchConditions);
            pagedSearchModel.setPage(page);
            pagedSearchModel.setRows(rows);
            branchTables = DatabaseHelper.findAllPaged(BranchTable.class, pagedSearchModel);
        } else {
            branchTables = new ArrayList<BranchTable>();
        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("total", count);
        data.put("rows", branchTables);
        return ApiRest.builder().data(data).message("查询桌台信息成功！").successful(true).build();
    }
}
