package com.mes.manufacture.controller;

import com.google.gson.Gson;
import com.mes.manufacture.domain.DypSpareIndication;
import io.swagger.annotations.*;
import java.util.List;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mes.common.log.annotation.Log;
import com.mes.common.log.enums.BusinessType;
import com.mes.common.security.annotation.PreAuthorize;
import com.mes.manufacture.domain.DypMachiningInfo;
import com.mes.manufacture.service.IDypMachiningInfoService;
import com.mes.common.core.web.controller.BaseController;
import com.mes.common.core.web.domain.AjaxResult;
import com.mes.common.core.utils.poi.ExcelUtil;
import com.mes.common.core.web.page.TableDataInfo;

/**
 * 工艺信息Controller
 * 
 * @author mes
 * @date 2020-11-27
 */
@Api(tags = "工艺信息接口")
@RestController
@RequestMapping("/machingInfo")
public class DypMachiningInfoController extends BaseController
{
    @Autowired
    private IDypMachiningInfoService dypMachiningInfoService;

    /**
     * 查询工艺信息列表
     */
    @ApiOperation("查询工艺信息列表")
    @ApiResponses({
            @ApiResponse(code = 200,message = "OK",response = DypMachiningInfo.class),
    })
    @ApiImplicitParam(name = "info", value = "工艺信息实体", dataType = "DypMachiningInfo")
    //@PreAuthorize(hasPermi = "manufacture:info:list")
    @GetMapping("/list")
    public TableDataInfo list(DypMachiningInfo dypMachiningInfo)
    {
        startPage();
        List<DypMachiningInfo> list = dypMachiningInfoService.selectDypMachiningInfoList(dypMachiningInfo);
        return getDataTable(list);
    }

    /**
     * 导出工艺信息列表
     */
    @ApiOperation("导出工艺信息列表")
    @ApiImplicitParam(name = "spareMachiningIds", value = "工艺信息spareMachiningIds", dataType = "Long")
    @PreAuthorize(hasPermi = "manufacture:info:export")
    @Log(title = "工艺信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DypMachiningInfo dypMachiningInfo) throws IOException
    {
        List<DypMachiningInfo> list = dypMachiningInfoService.selectDypMachiningInfoList(dypMachiningInfo);
        ExcelUtil<DypMachiningInfo> util = new ExcelUtil<DypMachiningInfo>(DypMachiningInfo.class);
        util.exportExcel(response, list, "info");
    }

    /**
     * 获取工艺信息详细信息
     */
    @ApiOperation("获取工艺信息详细信息")
    @ApiResponses({
            @ApiResponse(code = 200,message = "OK",response = DypMachiningInfo.class),
    })
    @ApiImplicitParam(name = "id", value = "工艺信息id", dataType = "long")
    @PreAuthorize(hasPermi = "manufacture:info:query")
    @GetMapping(value = "/{spareMachiningId}")
    public AjaxResult getInfo(@PathVariable("spareMachiningId") Long spareMachiningId)
    {
        return AjaxResult.success(dypMachiningInfoService.selectDypMachiningInfoById(spareMachiningId));
    }

    /**
     * 新增工艺信息
     */
    @ApiOperation("新增工艺信息")
    @ApiImplicitParam(name = "dypMachiningInfo", value = "工艺信息实体", dataType = "DypMachiningInfo")
   // @PreAuthorize(hasPermi = "manufacture:info:add")
    @Log(title = "工艺信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(String params)
    {
        Gson gson= new Gson();
        DypMachiningInfo infoObj = new DypMachiningInfo();
        infoObj=gson.fromJson(params,DypMachiningInfo.class);
        return toAjax(dypMachiningInfoService.insertDypMachiningInfo(infoObj));
    }

    /**
     * 修改工艺信息
     */
    @ApiOperation("修改工艺信息")
    @ApiImplicitParam(name = "dypMachiningInfo", value = "工艺信息实体", dataType = "DypMachiningInfo")
    //@PreAuthorize(hasPermi = "manufacture:info:edit")
    @Log(title = "工艺信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(String params)
    {
        Gson gson= new Gson();
        DypMachiningInfo infoObj = new DypMachiningInfo();
        infoObj=gson.fromJson(params,DypMachiningInfo.class);
        return toAjax(dypMachiningInfoService.updateDypMachiningInfo(infoObj));
    }

    /**
     * 删除工艺信息
     */
    @ApiOperation("删除工艺信息")
    @ApiImplicitParam(name = "spareMachiningIds", value = "工艺信息spareMachiningIds", dataType = "Long")
    @PreAuthorize(hasPermi = "manufacture:info:remove")
    @Log(title = "工艺信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{spareMachiningIds}")
    public AjaxResult remove(@PathVariable Long[] spareMachiningIds)
    {
        return toAjax(dypMachiningInfoService.deleteDypMachiningInfoByIds(spareMachiningIds));
    }
}
