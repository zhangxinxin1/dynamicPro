package com.rm.app.dyp.core.content.service.impl;

import com.rm.app.dyp.core.base.DomainHelper;
import com.rm.app.dyp.core.content.ContentErrorCode;
import com.rm.app.dyp.core.content.model.dto.OpenMesCallParmsDTO;
import com.rm.app.dyp.core.content.model.vo.openvo.TokenVo;
import com.rm.app.dyp.core.content.service.MesCallBackManager;
import com.rm.app.dyp.core.order.service.impl.OrderSyncManagerImpl;
import com.rm.app.dyp.core.order.vo.open.DypOrderPartCraftProgressResultVo;
import com.rm.app.dyp.core.order.vo.open.DypOrderPartProgressResultVo;
import com.rm.app.dyp.core.order.vo.open.DypOrderProgressResultVo;
import com.rm.app.dyp.core.system.model.vo.EnterpriseinfosAddParam;
import com.rm.app.dyp.core.system.model.vo.EnterpriseinfosEditParam;
import com.rm.app.dyp.framework.exception.ServiceException;
import com.rm.app.dyp.framework.util.HttpUtils;
import com.rm.app.dyp.framework.util.StringUtil;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.json.JsonOutboundMessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MesCallBackManagerImpl implements MesCallBackManager {

    private Logger logger = LoggerFactory.getLogger(MesCallBackManager.class);

    @Autowired
    private DomainHelper domainHelper;


    // 默认超时时间， 单位秒
    public static final int DEFAULT_TIMEOUT = 30;

    public static String ContentType = "application/x-www-form-urlencoded";


    /*
    * 开通云mes
    * */
    @Override
    public Map<String,Object> openMes(EnterpriseinfosAddParam parms) {
        Map<String,Object>  map=new HashMap<>();
        JSONObject json = JSONObject.fromObject(parms);
         String retrunData = null;
        System.out.println("<>"+json);
        System.out.println("domainHelper.getMenMes+>"+domainHelper.getMesDomain());
            try {
                retrunData = HttpUtils.sendPostJson(domainHelper.getMesDomain()+"enterprise/dataCreate/openMes",   json.toString());
               // retrunData = HttpUtils.sendPostJson("http://127.0.0.1:8080/"+"enterprise/dataCreate/openMes",   json.toString());

                System.out.println("retrunData++=>"+retrunData);
              //  根据返参判断是否成功
                if(retrunData.contains("\"code\":200")){
                    map.put("success",true);
                    map.put("messsage","创建mes成功");

                }else if(retrunData.contains("企业账号已存在")){
                    map.put("success",false);
                    map.put("messsage","企业账号已存在");
                }else if(retrunData.contains("企业名称已存在")){
                    map.put("success",false);
                    map.put("messsage","企业名称已存在");
                } else if(retrunData.contains("组织机构代码已存在")){
                    map.put("success",false);
                    map.put("messsage","组织机构代码已存在");
                }
                return  map;

        }catch (Exception e){
                e.getMessage();
                System.out.println("debugex++++++++++++"+ e.getMessage());
            e.printStackTrace();
        }
        return  map;
    }


    @Override
    public String getToken(TokenVo parms) {
        Map<String,Object>  map=new HashMap<>();

        JSONObject json = JSONObject.fromObject(parms);
        String retrunData = null;
        try {
            retrunData = HttpUtils.sendPostJson(domainHelper.getMesDomain()+"auth/dyp-token",json.toString());
            //  根据返参判断是否成功
            return  retrunData;

        }catch (Exception e){
            e.getMessage();
            System.out.println("debugex++++++++++++"+ e.getMessage());
            e.printStackTrace();
        }
        return  "";
    }

    @Override
    @Transactional(value = "tradeTransactionManager",propagation = Propagation.REQUIRED,rollbackFor=Exception.class)
    public Map<String, Object> addMesOrder(String strParam, Map<String, String> mapParam) {
        Map<String,Object>  map=new HashMap<>();
        String retrunData = null;
        try {
            retrunData = HttpUtils.sendPostJsonWithHeader(domainHelper.getMesDomain()+"manufacture/open/order/create",strParam,mapParam);
            if(retrunData.contains("\"code\":200")){
                map.put("success",true);
                map.put("messsage","创建mes成功");

            }else if(retrunData.contains("订单已存在")){
                map.put("success",false);
                map.put("messsage","订单已存在，无须重复");
                throw new ServiceException(ContentErrorCode.E9903.code(), "订单已存在，无须重复!");
            }
        return map;

        }catch (Exception e){
            e.getMessage();
            e.printStackTrace();
            throw new ServiceException(ContentErrorCode.E9903.code(), "添加云mes订单异常,请联系管理员!");

        }

    }

    @Override
    public Map<String, Object> updateOpenMes(EnterpriseinfosEditParam parms) {
        Map<String,Object>  map=new HashMap<>();
        JSONObject json = JSONObject.fromObject(parms);
        String retrunData = null;
        System.out.println("<>"+json);
        System.out.println("domainHelper.getMenMes+>"+domainHelper.getMesDomain());
        try {
            retrunData = HttpUtils.sendPostJson(domainHelper.getMesDomain()+"enterprise/enterpriseinfos/status",   json.toString());
            // retrunData = HttpUtils.sendPostJson("http://127.0.0.1:8080/"+"enterprise/enterpriseinfos/status",   json.toString());

            System.out.println("retrunData++=>"+retrunData);
            //  根据返参判断是否成功
            if(retrunData.contains("\"code\":200")){
                map.put("success",true);
                map.put("messsage","操作成功");

            }else if(retrunData.contains("操作失败")){
                map.put("success",false);
                map.put("messsage","操作失败");
            }
            return  map;

        }catch (Exception e){
            e.getMessage();
            System.out.println("debugex++++++++++++"+ e.getMessage());
            e.printStackTrace();
        }
        return  map;
    }

    @Override
    public DypOrderProgressResultVo getMesOrder(String orderParam, Map<String, String> mapParam) {
        Map<String,Object>  map=new HashMap<>();
        DypOrderProgressResultVo result= null;
        String retrunData = null;
        try {
            retrunData = HttpUtils.sendPostJsonWithHeader(domainHelper.getMesDomain()+"manufacture/open/order/order-progress",orderParam,mapParam);
            logger.info("调用获取mes订单返回值："+retrunData);
             if(StringUtil.isEmpty(retrunData)){
                 logger.info("获取mes订单返回值空,请联系管理员!");
                 return result;
             }
            JSONObject jsonObject=JSONObject.fromObject(retrunData);
            String code=  jsonObject.get("code").toString();

            System.out.println("retrunData++++++++++++++++++++++==>"+retrunData);
            System.out.println("jsonObject+++++++++++++++++++>"+jsonObject);
            //调用mes订单返回值非空判断+++++++++++++++++++++++++
            if(!retrunData.contains("data")){
                logger.info("获取mes订单信息为空"+result+"<返回值没有包括data>"+retrunData);
                return result;
            }
            String data=  jsonObject.get("data").toString();
            if(code.equals("500")) {
                logger.error("获取mes订单信息为空异常,请联系管理员!");
                return result;
            }
            DypOrderProgressResultVo info=(DypOrderProgressResultVo)JSONObject.toBean(JSONObject.fromObject(data), DypOrderProgressResultVo.class);
             if (info!=null){

                 result=info;// 订单信息
                 result.setPartProgressList(new ArrayList<>());
                 // todo 获取零件信息
                String partList=     JSONObject.fromObject( jsonObject.get("data")).get("partProgressList").toString();
                 if(partList!=null){
                     // 零件
                     JSONArray  partArry=  JSONArray.fromObject(partList);
                      if (partArry !=null &&  partArry.size()>0) {
                          for (Object x : partArry)

                              if (JSONObject.fromObject(x).get("craftProgressist") != null) {
                                  DypOrderPartProgressResultVo part = (DypOrderPartProgressResultVo) JSONObject.toBean(JSONObject.fromObject(x), DypOrderPartProgressResultVo.class);
                                  //工艺流程
                                  JSONArray craftArry = JSONArray.fromObject(JSONObject.fromObject(x).get("craftProgressist"));
                                  List<DypOrderPartCraftProgressResultVo> craftList = (List) JSONArray.toCollection(craftArry, DypOrderPartCraftProgressResultVo.class);
                                  if (craftList != null) {
                                      part.setCraftProgressist(craftList);
                                  }
                                  result.getPartProgressList().add(part);
                              }
                      }
                 }
             }
            logger.info("result+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++>"+result);

        }catch (Exception e){
            e.getMessage();
            e.printStackTrace();
            logger.error( "获取云mes订单异常,请联系管理员!");
        }finally {
            return  result;
        }

    }


}
