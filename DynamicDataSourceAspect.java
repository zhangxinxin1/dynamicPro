package com.mes.manufacture.datasource;


import com.mes.common.redis.service.RedisService;
import com.mes.common.security.service.TokenService;
import com.mes.system.api.model.LoginUser;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态数据源切换处理器
 * @author Louis
 * @date Oct 31, 2018
 */
@Aspect
@Order(-1)  // 该切面应当先于 @Transactional 执行
@Component
public class DynamicDataSourceAspect {

    @Autowired
    private RedisService redisService;

    @Autowired
    private TokenService tokenService;
    @Pointcut("execution(public * com.mes.manufacture.controller..*.*(..))")
    public void controllerAspect() {
    }


    /**
     * 切换数据源
     * @param point
     */
    @Before("controllerAspect()")
    public void switchDataSource(JoinPoint point) throws SQLException, UnsupportedEncodingException {
        LoginUser loginUser= tokenService.getLoginUser();
        System.out.println("dataSource+++++++++++++++++++++++++++++uuuuuu+++++++++>");
        String dataSource=loginUser.getCurrentDataSource();
        if (!DynamicDataSourceContextHolder.containDataSourceKey(dataSource)) {
            DynamicDataSource  ynamicDataSource=new DynamicDataSource();
            System.out.println("dataSource++++++++++++++++++++++++++++++++++++++>"+dataSource);
            ynamicDataSource.createNewZuhuDB(dataSource);
            DynamicDataSourceContextHolder.setDataSourceKey(dataSource);
            System.out.println("创建数据源++++++++++++++++++++++++"+dataSource);
        } else {
            System.out.println("切换dataSource+++++++++++++++"+dataSource);
            DynamicDataSourceContextHolder.setDataSourceKey(dataSource);
            System.out.println("<++++++++++++++++++++222222>"+DynamicDataSourceContextHolder.getDataSourceKey());
        }
    }

    /**
     * 重置数据源
     * @param point
     */
    @After("controllerAspect()")
    public void restoreDataSource(JoinPoint point) {
        // 将数据源置为默认数据源
    //    DynamicDataSourceContextHolder.clearDataSourceKey();
        System.out.println("Method [" + point.getSignature() + "] 执行完成，数据源切回默认数据源，默认数据源："+DynamicDataSourceContextHolder.getDataSourceKey());


    }



    public static Map<String, Object> getKeyAndValue(Object obj) {
        Map<String, Object> map = new HashMap<>();
        // 得到类对象
        Class userCla = (Class) obj.getClass();
        /* 得到类中的所有属性集合 */
        Field[] fs = userCla.getDeclaredFields();
        for (int i = 0; i < fs.length; i++) {
            Field f = fs[i];
            f.setAccessible(true); // 设置些属性是可以访问的
            Object val = new Object();
            try {
                val = f.get(obj);
                // 得到此属性的值
                map.put(f.getName(), val);// 设置键值
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return map;
    }


}