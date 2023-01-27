package com.jpa.entity_manage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

public class CommonConfig {
    static protected final String CONFIG_DRIVER_NAME = "spring.datasource.driver-class-name";
    static protected final String CONFIG_URL = "spring.datasource.url";
    static protected final String CONFIG_USERNAME = "spring.datasource.username";
    static protected final String CONFIG_PASSWORD = "spring.datasource.password";
    static protected final String CONFIG_HIBENATE_DDL = "hibernate.hbm2ddl.auto";
    static protected final String CONFIG_HIBENATE_DIALECT = "hibernate.dialect";
    static protected final String CONFIG_HIBERNATE_SHOW_SQL = "hibernate.show_sql";
    static protected final String CONFIG_ENTITY_MANAGER_SCAN_PACKAGES = "entitymanager.packagesToScan";
    static protected final String CONFIG_JDBC_DRIVER_DEFAULT = "org.h2.Driver";
    static protected final String CONFIG_JDBC_URL_DEF = "jdbc:h2:dummy";
    @Autowired
   protected  Environment mEnvironment;

    @Autowired
    protected  DataSource mDataSource;
    protected void setupDS(ICommonDSSetup ds) {

        ds.setUrl(mEnvironment.getProperty(CONFIG_URL));
        ds.setUsername(mEnvironment.getProperty(CONFIG_USERNAME,""));
        ds.setPassword(mEnvironment.getProperty(CONFIG_PASSWORD));
    }
    protected String[] repositories(String packs) {
        String rep = mEnvironment.getProperty(CONFIG_ENTITY_MANAGER_SCAN_PACKAGES);
        if (rep == null) {
            return new String[]{};
        }
        return rep.split("\\,");
    }
}
