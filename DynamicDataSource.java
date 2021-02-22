package com.mes.manufacture.datasource;

import com.mes.common.core.utils.SpringUtils;
import com.mes.common.security.constant.AppConstant;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

/**
 * 动态数据源实现类
 */
public class DynamicDataSource extends AbstractRoutingDataSource {
    Connection conn = null;
    Statement stmt = null;

    public static Map<Object, Object> dataSourcesCache = null;

    /**
     * 如果不希望数据源在启动配置时就加载好，可以定制这个方法，从任何你希望的地方读取并返回数据源
     * 比如从数据库、文件、外部接口等读取数据源信息，并最终返回一个DataSource实现类对象即可
     */
    @Override
    protected DataSource determineTargetDataSource() {
        return super.determineTargetDataSource();
    }

    /**
     * 如果希望所有数据源在启动配置时就加载好，这里通过设置数据源Key值来切换数据，定制这个方法
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceContextHolder.getDataSourceKey();
    }

    /**
     * 设置默认数据源
     * @param defaultDataSource
     */
    public void setDefaultDataSource(Object defaultDataSource) {
        super.setDefaultTargetDataSource(defaultDataSource);
    }

    /**
     * 设置数据源
     * @param dataSources
     */
    public void setDataSources(Map<Object, Object> dataSources) {
        super.setTargetDataSources(dataSources);
        dataSourcesCache = dataSources;
        // 将数据源的 key 放到数据源上下文的 key 集合中，用于切换时判断数据源是否有效
        DynamicDataSourceContextHolder.addDataSourceKeys(dataSources.keySet());
    }

    public void addDataSource(String zuhuId,String url,String user,String password) {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(AppConstant.MYSQL_DRIVER);
        dataSourceBuilder.url(url);
        dataSourceBuilder.username(user);
        dataSourceBuilder.password(password);
        DataSource dataSource = dataSourceBuilder.build();


        dataSourcesCache.put(zuhuId,dataSource);
        super.setTargetDataSources(dataSourcesCache);
        super.afterPropertiesSet();
        // 将数据源的 key 放到数据源上下文的 key 集合中，用于切换时判断数据源是否有效
        DynamicDataSourceContextHolder.addDataSourceKey(zuhuId);

    }








    public String createNewZuhuDB(String zuhuId) {
        String ipport = "127.0.0.1:3306"; //配到配置文件中


        //先用默认sql创建新库，用java执行sh脚本，shell脚本中连接mysql并写入创建database，table的语句
        //TODO 1

        Properties properties = readPropertiesFile("application.properties");
        String password = properties.getProperty("spring.datasource.master.password");
        String userName = properties.getProperty("spring.datasource.master.username");
        System.out.println("++++++++++++++++++++++++++++pas>"+password+"<>"+userName);

        //添加动态数据源
        String  mysqlUrl = "jdbc:mysql://"+ipport+"/"+zuhuId+"?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8";
        String  mysqlUser = userName;
       // String  mysqlPwd = "ruanmeng@123!";
       String  mysqlPwd = password;

        DynamicDataSource dds = SpringUtils.getBean("dynamicDataSource");
        dds.addNewDataSource(zuhuId,mysqlUrl,mysqlUser,mysqlPwd);

        //TODO 2 主库中写租户记录

        return "ok";
    }



    public void addNewDataSource(String zuhuId,String url,String user,String password) {
        System.out.println("==addNewDataSource=============111================"+zuhuId);
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(AppConstant.MYSQL_DRIVER);
        dataSourceBuilder.url(url);
        dataSourceBuilder.username(user);
        dataSourceBuilder.password(password);
        DataSource dataSource = dataSourceBuilder.build();
        try {
            System.out.println("==addNewDataSource=============222================"+zuhuId+dataSource.getConnection().toString());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


        dataSourcesCache.put(zuhuId,dataSource);
        super.setTargetDataSources(dataSourcesCache);
        super.afterPropertiesSet();
        // 将数据源的 key 放到数据源上下文的 key 集合中，用于切换时判断数据源是否有效
        DynamicDataSourceContextHolder.addDataSourceKey(zuhuId);
    }


    /**
     * 通过配置文件名读取内容
     * @param fileName
     * @return
     */
    public static Properties readPropertiesFile(String fileName) {
        try {
            Resource resource = new ClassPathResource(fileName);
            Properties props = PropertiesLoaderUtils.loadProperties(resource);
            return props;
        } catch (Exception e) {
            System.out.println("————读取配置文件：" + fileName + "出现异常，读取失败————");
            e.printStackTrace();
        }
        return null;
    }



}
