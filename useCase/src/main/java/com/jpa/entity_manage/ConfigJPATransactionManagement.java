package com.jpa.entity_manage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * The class provides to configure transaction manager and involved objects as
 * EntityManagerFactory, DataSources and vendor adapter.
 * The class can be considerd as closed configuration model, which parameters are set up externaly
 *
 * @see #transactionManager()
 * @see #dataSource()
 * @see #vendorAdapter()
 * @see #entityManagerFactoryBean()
 */
@Configuration
@EnableTransactionManagement
public class ConfigJPATransactionManagement extends CommonConfig {
/*

    static class DriverDataSourceWrapper extends DriverManagerDataSource implements ICommonDSSetup {

    }

    @Autowired
    JpaVendorAdapter mJpaVendor;

    @Bean
    public DataSource dataSource() {

        DriverDataSourceWrapper ds = new DriverDataSourceWrapper();
        ds.setDriverClassName(mEnvironment.getProperty(CONFIG_DRIVER_NAME));
        setupDS(ds);
        return ds;
    }

    @Bean
    public JpaVendorAdapter vendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        boolean showSQL = false;
        if ("true".equals(mEnvironment.getProperty(CONFIG_HIBERNATE_SHOW_SQL)))
            showSQL = true;
        adapter.setShowSql(showSQL);
        return adapter;

    }

    private LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(mDataSource);
        factoryBean.setPackagesToScan(repositories(mEnvironment.getProperty(CONFIG_ENTITY_MANAGER_SCAN_PACKAGES)));
        factoryBean.setJpaVendorAdapter(mJpaVendor);
        factoryBean.setJpaProperties(properties());
        factoryBean.afterPropertiesSet();
        return factoryBean;
    }

    public @Bean
    EntityManagerFactory entityManagerFactory() {
        return entityManagerFactoryBean().getObject();
    }

    Properties properties() {
        Properties properties = new Properties();
        properties.setProperty(CONFIG_HIBENATE_DIALECT, mEnvironment.getProperty(CONFIG_HIBENATE_DIALECT));
        properties.setProperty(CONFIG_HIBENATE_DDL, mEnvironment.getProperty(CONFIG_HIBENATE_DDL));
        return properties;
    }

    @Bean
    PlatformTransactionManager transactionManager() {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory());
        return jpaTransactionManager;
    }
*/

}
