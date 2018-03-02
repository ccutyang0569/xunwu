package com.founder.xunwu.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * @program: xunwu
 * @description: JPA配置类
 * @author: YangMing
 * @create: 2018-01-28 12:01
 **/
@Configuration
@EnableJpaRepositories(basePackages = "com.founder.xunwu.repository")
@EnableTransactionManagement
public class JPAConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource  dataSource(){
      return DataSourceBuilder.create().build();

    }
    @Bean
    public LocalContainerEntityManagerFactoryBean  entityManagerFactory(){
        HibernateJpaVendorAdapter   jpaVendorAdapter=new HibernateJpaVendorAdapter();
        //自动生成sql  取消
        jpaVendorAdapter.setGenerateDdl(false);
        LocalContainerEntityManagerFactoryBean  entityManagerFactory=new LocalContainerEntityManagerFactoryBean();
          entityManagerFactory.setDataSource(dataSource());
          entityManagerFactory.setJpaVendorAdapter(jpaVendorAdapter);
          entityManagerFactory.setPackagesToScan("com.founder.xunwu.entity");
          return  entityManagerFactory;
    }


    /**
     * method_name: transactionManager
     * param: [entityManagerFactory]
     * return: org.springframework.transaction.PlatformTransactionManager
     * describe: TODO(Hibenate JPA 事务)
     * create_user: YangMing
     * create_date: 2018/1/28 12:23
     **/

    @Bean
    public PlatformTransactionManager  transactionManager(EntityManagerFactory entityManagerFactory){
        JpaTransactionManager   transactionManager=new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;


    }
}
