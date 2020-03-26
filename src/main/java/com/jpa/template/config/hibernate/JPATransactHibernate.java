package com.jpa.template.config.hibernate;

import com.jpa.template.config.common.CommonConfig;
import com.jpa.template.config.common.ICommonDSSetup;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.SQLException;

@Configuration
@EnableTransactionManagement
public class JPATransactHibernate extends CommonConfig {
    /*static   class BasicDataSourceBridge extends BasicDataSource implements ICommonDSSetup
    {


    }*/
    @Bean
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(mEnvironment.getProperty(CONFIG_DRIVER_NAME));
        setupDS(new ICommonDSSetup() {
            @Override
            public void setUrl(String s) {
                dataSource.setUrl(s);
            }

            @Override
            public void setUsername(String s) {
                dataSource.setUsername(s);

            }

            @Override
            public void setPassword(String s) {
                dataSource.setPassword(s);

            }
        });
        return dataSource;
    }
    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setPackagesToScan(repositories(mEnvironment.getProperty(CONFIG_ENTITY_MANAGER_SCAN_PACKAGES)));
        return sessionFactory;
    }
    @Bean
    HibernateTransactionManager hibernateTransactionManager()
    {
        HibernateTransactionManager htm= new HibernateTransactionManager();
         htm.setSessionFactory(sessionFactory().getObject());
        htm.afterPropertiesSet();
        return htm;

    }
}
