package com.wecom.scrm.service;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.wecom.scrm.config.WxCpServiceManager;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.wecom.scrm.entity.WecomEnterprise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;

@Slf4j
@Service
public class EnterpriseManagementService {

    private final WecomEnterpriseService wecomEnterpriseService;
    private final DynamicRoutingDataSource dynamicRoutingDataSource;
    private final DefaultDataSourceCreator dataSourceCreator;
    private final WxCpServiceManager wxCpServiceManager;
    private final JdbcTemplate jdbcTemplate;
    private final SyncService syncService;

    public EnterpriseManagementService(WecomEnterpriseService wecomEnterpriseService,
                                       DataSource dynamicRoutingDataSource,
                                       DefaultDataSourceCreator dataSourceCreator,
                                       WxCpServiceManager wxCpServiceManager,
                                       JdbcTemplate jdbcTemplate,
                                       SyncService syncService) {
        this.wecomEnterpriseService = wecomEnterpriseService;
        this.dynamicRoutingDataSource = (DynamicRoutingDataSource) dynamicRoutingDataSource;
        this.dataSourceCreator = dataSourceCreator;
        this.wxCpServiceManager = wxCpServiceManager;
        this.jdbcTemplate = jdbcTemplate;
        this.syncService = syncService;
    }

    public WecomEnterprise addEnterprise(WecomEnterprise enterprise) {
        if (!enterprise.getDbUrl().contains("createDatabaseIfNotExist=true")) {
            String separator = enterprise.getDbUrl().contains("?") ? "&" : "?";
            enterprise.setDbUrl(enterprise.getDbUrl() + separator + "createDatabaseIfNotExist=true");
        }

        // Ensure database exists with utf8mb4 encoding
        ensureDatabaseExists(enterprise.getDbUrl());

        wecomEnterpriseService.save(enterprise);

        // 1. Add Datasource dynamically
        DataSourceProperty dsp = new DataSourceProperty();
        dsp.setPoolName(enterprise.getCorpId());
        dsp.setUrl(enterprise.getDbUrl());
        dsp.setUsername(enterprise.getDbUsername());
        dsp.setPassword(enterprise.getDbPassword());
        dsp.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        DataSource newDataSource = dataSourceCreator.createDataSource(dsp);
        dynamicRoutingDataSource.addDataSource(enterprise.getCorpId(), newDataSource);

        // 2. Initialize Database schema
        initDatabaseSchema(newDataSource);

        // 3. Register WxCpConfigStorage via Manager
        wxCpServiceManager.registerWxCpServices(enterprise.getCorpId(),
                enterprise.getAgentId(), enterprise.getAgentSecret(),
                enterprise.getToken(), enterprise.getEncodingAesKey());
        
        // 4. Trigger initial background synchronization
        try {
            // Push context so background threads capture it via MdcTaskDecorator
            DynamicDataSourceContextHolder.push(enterprise.getCorpId());
            WxCpServiceManager.setCurrentCorpId(enterprise.getCorpId());
            
            log.info("Triggering automated initial sync for enterprise: {}", enterprise.getCorpId());
            syncService.initialSync();
        } finally {
            // Clean up calling thread context
            DynamicDataSourceContextHolder.poll();
            WxCpServiceManager.clearCurrentCorpId();
        }

        log.info("Successfully added and initialized enterprise: {}", enterprise.getCorpId());
        return enterprise;
    }

    public WecomEnterprise updateEnterprise(Long id, WecomEnterprise enterprise) {
        WecomEnterprise existing = wecomEnterpriseService.findById(id);
        if (existing == null) {
            throw new RuntimeException("Enterprise not found with id: " + id);
        }

        // Update non-DB fields
        existing.setName(enterprise.getName());
        existing.setAgentId(enterprise.getAgentId());
        
        // Only update secrets if new values are provided
        if (StringUtils.hasText(enterprise.getAgentSecret())) {
            existing.setAgentSecret(enterprise.getAgentSecret());
        }
        if (StringUtils.hasText(enterprise.getToken())) {
            existing.setToken(enterprise.getToken());
        }
        if (StringUtils.hasText(enterprise.getEncodingAesKey())) {
            existing.setEncodingAesKey(enterprise.getEncodingAesKey());
        }
        
        wecomEnterpriseService.save(existing);

        // Update WxCpConfigStorage
        wxCpServiceManager.registerWxCpServices(existing.getCorpId(), existing.getAgentId(),
                existing.getAgentSecret(), existing.getToken(), existing.getEncodingAesKey());

        log.info("Successfully updated enterprise: {}", existing.getCorpId());
        return existing;
    }

    public void deleteEnterprise(Long id) {
        WecomEnterprise existing = wecomEnterpriseService.findById(id);
        if (existing != null) {
            // 1. Remove from master DB
            wecomEnterpriseService.delete(id);
            
            // 2. Cleanup runtime managers
            dynamicRoutingDataSource.removeDataSource(existing.getCorpId());
            wxCpServiceManager.removeEnterprise(existing.getCorpId());
            
            log.info("Successfully hard-deleted enterprise record: {}", existing.getCorpId());
        }
    }

    private void ensureDatabaseExists(String dbUrl) {
        if (StringUtils.hasText(dbUrl)) {
            try {
                // Extract database name from jdbc:mysql://host:port/databaseName?params
                Pattern pattern = Pattern.compile("jdbc:mysql://[^/]+/([^?]+)");
                Matcher matcher = pattern.matcher(dbUrl);
                if (matcher.find()) {
                    String dbName = matcher.group(1);
                    log.info("Ensuring database `{}` exists with utf8mb4 encoding", dbName);
                    String sql = String.format("CREATE DATABASE IF NOT EXISTS `%s` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci", dbName);
                    jdbcTemplate.execute(sql);
                }
            } catch (Exception e) {
                log.warn("Could not explicitly create database with utf8mb4 encoding, falling back to driver auto-creation: {}", e.getMessage());
            }
        }
    }

    private void initDatabaseSchema(DataSource dataSource) {
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("db/schema.sql"));
            populator.setContinueOnError(true); // Don't fail if tables already exist
            populator.execute(dataSource);
            log.info("Initialized database schema for new datasource.");
        } catch (Exception e) {
            log.error("Failed to initialize database schema", e);
        }
    }
}
