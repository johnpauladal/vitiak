package com.test;

import com.baomidou.mybatisplus.enums.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.DbType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import org.junit.Test;

/**
 * <p>
 * 测试生成代码
 * </p>
 *
 * @author K神
 * @date 2017/12/18
 */
public class GeneratorServiceEntityTest {

    @Test
    public void generateCode() {
        String packageName = "com.kvn";
        String moduleName = "car";
        boolean serviceNameStartWithI = false;//user -> UserService, 设置成true: user -> IUserService
        generateByTables(serviceNameStartWithI, packageName, moduleName,"lock_log");
    }

    private void generateByTables(boolean serviceNameStartWithI, String packageName,String moduleName, String... tableNames) {
        GlobalConfig config = new GlobalConfig();
        String dbUrl = "jdbc:mysql://10.203.13.156:3307/avcp?useUnicode=true&amp;characterEncoding=UTF8";
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setDbType(DbType.MYSQL)
                .setUrl(dbUrl)
                .setUsername("dev")
                .setPassword("dev123456")
                .setDriverName("com.mysql.jdbc.Driver");
        StrategyConfig strategyConfig = new StrategyConfig();
        strategyConfig
                .setCapitalMode(true)
                .setEntityLombokModel(true) // lombok支持
                .setDbColumnUnderline(true)
                .setNaming(NamingStrategy.underline_to_camel)
//                .setTablePrefix("ts_")
                .setInclude(tableNames);//修改替换成你需要的表名，多个表名传数组
        config.setActiveRecord(true)
                .setAuthor("809693")
                .setOutputDir("E:\\output\\java")
                .setIdType(IdType.AUTO)
                .setFileOverride(true);
        if (!serviceNameStartWithI) {
            config.setServiceName("%sService");
        }
        new AutoGenerator().setGlobalConfig(config)
                .setDataSource(dataSourceConfig)
                .setStrategy(strategyConfig)
                .setPackageInfo(
                        new PackageConfig()
                                .setParent(packageName)
                                .setModuleName(moduleName)
                                .setController("controller")
                                .setEntity("entity")

                ).execute();
    }

    private void generateByTables(String packageName,String moduleName, String... tableNames) {
        generateByTables(true, packageName,moduleName, tableNames);
    }
}