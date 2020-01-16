package com.personal.oyl.event.sample;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

/**
 * @author OuYang Liang
 */
@Configuration
public class DatasourceConfig {
    private static final int DEFAULT_POOL_SIZE = 10;

    @Bean
    @Primary
    public DataSource dataSource() throws PropertyVetoException {
        ComboPooledDataSource rlt = new ComboPooledDataSource();
        rlt.setDriverClass("com.mysql.jdbc.Driver");
        rlt.setUser("root");
        rlt.setPassword("password");
        rlt.setJdbcUrl("jdbc:mysql://localhost:3306/libevent_sample");
        rlt.setInitialPoolSize(DEFAULT_POOL_SIZE);
        rlt.setMaxPoolSize(DEFAULT_POOL_SIZE);
        rlt.setMinPoolSize(DEFAULT_POOL_SIZE);
        rlt.setTestConnectionOnCheckin(true);
        rlt.setPreferredTestQuery("select 1");

        return rlt;
    }


    @Bean
    @Primary
    public SqlSessionFactory sqlSessionFactory(final DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:sqlMap/**/*.xml"));
        return bean.getObject();
    }

}
