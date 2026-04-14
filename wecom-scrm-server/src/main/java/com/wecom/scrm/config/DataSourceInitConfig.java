package com.wecom.scrm.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.wecom.scrm.entity.WecomEnterprise;
import com.wecom.scrm.service.WecomEnterpriseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.util.List;

@Slf4j
@Configuration
public class DataSourceInitConfig {

    private final WecomEnterpriseService wecomEnterpriseService;
    private final DynamicRoutingDataSource dynamicRoutingDataSource;
    private final DefaultDataSourceCreator dataSourceCreator;
    private final WxCpServiceManager wxCpServiceManager;

    public DataSourceInitConfig(WecomEnterpriseService wecomEnterpriseService,
                                DataSource dynamicRoutingDataSource,
                                DefaultDataSourceCreator dataSourceCreator,
                                WxCpServiceManager wxCpServiceManager) {
        this.wecomEnterpriseService = wecomEnterpriseService;
        this.dynamicRoutingDataSource = (DynamicRoutingDataSource) dynamicRoutingDataSource;
        this.dataSourceCreator = dataSourceCreator;
        this.wxCpServiceManager = wxCpServiceManager;
    }

    @Bean
    @Order(1) // Run early
    public CommandLineRunner initDynamicDataSources() {
        return args -> {
            log.info("Initializing dynamic datasources from master database...");
            try {
                List<WecomEnterprise> enterprises = wecomEnterpriseService.findAll();
                for (WecomEnterprise enterprise : enterprises) {
                    DataSourceProperty dsp = new DataSourceProperty();
                    dsp.setPoolName(enterprise.getCorpId());
                    dsp.setUrl(enterprise.getDbUrl());
                    dsp.setUsername(enterprise.getDbUsername());
                    dsp.setPassword(enterprise.getDbPassword());
                    dsp.setDriverClassName("com.mysql.cj.jdbc.Driver");
                    
                    DataSource newDataSource = dataSourceCreator.createDataSource(dsp);
                    dynamicRoutingDataSource.addDataSource(enterprise.getCorpId(), newDataSource);

                    // Reconstruct WxCpService config via Manager
                    wxCpServiceManager.addEnterprise(enterprise.getCorpId(), 
                            enterprise.getAgentId(), enterprise.getAgentSecret(),
                            enterprise.getToken(), enterprise.getEncodingAesKey());

                    // Initialize/Migrate database schema
                    initDatabaseSchema(newDataSource);

                    log.info("Loaded and initialized enterprise: {}", enterprise.getCorpId());
                }
            } catch (Exception e) {
                log.warn("Failed to load enterprises from db, perhaps it is not initialized yet? ({})", e.getMessage());
            }
        };
    }

    private void initDatabaseSchema(DataSource dataSource) {
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("db/schema.sql"));
            populator.setContinueOnError(true); // Don't fail if tables already exist
            populator.execute(dataSource);
            log.info("Check/Initialize database schema completed.");
        } catch (Exception e) {
            log.error("Failed to initialize database schema", e);
        }
    }
}
