package com.mes.manufacture.datasource;


import com.mes.common.redis.service.RedisService;
import com.mes.common.security.constant.AppConstant;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Mybatis配置
 * @author Louis
 * @date Oct 31, 2018
 */
@Configuration
@MapperScan(basePackages = {"com.mes.manufacture.mapper"}) // 扫描DAO
public class MybatisConfig {



    @Autowired
    private RedisService redisService;


    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource master() {
        return DataSourceBuilder.create().build();
    }

    @Bean("dynamicDataSource")
    public DataSource dynamicDataSource() {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>(2);
        DataSource masterDS = master();
        dataSourceMap.put(AppConstant.MASTER_ZUHU_ID, masterDS);
        System.out.println("debugdataSourceMap+++++++++"+ dataSourceMap.get(AppConstant.MASTER_ZUHU_ID));
//        dataSourceMap.put("slave", slave());
        // 将 master 数据源作为默认指定的数据源
        dynamicDataSource.setDefaultDataSource(masterDS);
        // 将 master 和 slave 数据源作为指定的数据源
        dynamicDataSource.setDataSources(dataSourceMap);
        System.out.println("debugdynamicDataSource+++++++++"+ dynamicDataSource.getResolvedDefaultDataSource());
        System.out.println("dynamicDataSource create completed ,master db create over ");
        return dynamicDataSource;
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactoryBean() throws Exception {
        System.out.println("sessionFactory start init");
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        // 配置数据源，此处配置为关键配置，如果没有将 dynamicDataSource作为数据源则不能实现切换
        sessionFactory.setDataSource(dynamicDataSource());
        sessionFactory.setTypeAliasesPackage("com.mes.manufacture.domain");    // 扫描Model
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(resolver.getResources("classpath*:/mapper/manufacture/*.xml"));    // 扫描映射文件
        return sessionFactory;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        // 配置事务管理, 使用事务时在方法头部添加@Transactional注解即可
        return new DataSourceTransactionManager(dynamicDataSource());
    }

//
//    @Bean
//    public String loadDS(){
//        System.out.println("debugBeantest++++++++++++++++");
//        DynamicDataSource dds1 = SpringUtils.getBean("dynamicDataSource");
//        String dsKey="mes00001,mes00002,mes00003,ry-cloud";
//        String[] dss = dsKey.split(",");
//        for(String ds : dss){
//            dds1.createNewZuhuDB(ds);
//            System.out.println("debugBean+++++++++++++++++++++++++++++++++++++++++++++++++++"+ds);
//        }
//        return "";
//
//    }


}

