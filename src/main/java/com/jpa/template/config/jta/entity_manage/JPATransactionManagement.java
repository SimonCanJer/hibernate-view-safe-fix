package com.jpa.template.config.jta.entity_manage;

import com.jpa.template.config.common.CommonConfig;
import com.jpa.template.config.common.ICommonDSSetup;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.AbstractDriverBasedDataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;

/**
 * The class provides to configure transaction manager and involved
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
public class JPATransactionManagement extends CommonConfig {


 static class DriverDataSourceWrapper extends DriverManagerDataSource implements ICommonDSSetup
 {

 }

    @Autowired
    JpaVendorAdapter mJpaVendor;

      @Bean
    public DataSource dataSource() {

        DriverDataSourceWrapper ds = new DriverDataSourceWrapper();
                ///new DriverManagerDataSource();
        ds.setDriverClassName(mEnvironment.getProperty(CONFIG_DRIVER_NAME));
        setupDS(ds);
        return ds;
    }

    @Bean
    public JpaVendorAdapter vendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        String strGenDdl = mEnvironment.getProperty(CONFIG_HIBENATE_DDL);
        boolean ddl = true;
        if (strGenDdl != null) {
            if (strGenDdl.equals("false")) {
                ddl = false;
            }
        }

       //adapter.setGenerateDdl(ddl);
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
    EntityManagerFactory entityManagerFactory()
    {
        return entityManagerFactoryBean().getObject();
    }

    Properties properties() {
        Properties properties = new Properties();
        properties.setProperty(CONFIG_HIBENATE_DIALECT, mEnvironment.getProperty(CONFIG_HIBENATE_DIALECT));
        properties.setProperty(CONFIG_HIBENATE_DDL, mEnvironment.getProperty(CONFIG_HIBENATE_DDL));
        //properties.setProperty(CONFIG_ENTITY_MANAGER_SCAN_PACKAGES,System.getProperty(CONFIG_ENTITY_MANAGER_SCAN_PACKAGES));
        return properties;
    }

    @Bean
    PlatformTransactionManager transactionManager() {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory());
        return jpaTransactionManager;
    }


}
