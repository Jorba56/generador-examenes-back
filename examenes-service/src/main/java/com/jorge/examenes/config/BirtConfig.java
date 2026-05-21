package com.jorge.examenes.config;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BirtConfig {

    @Bean
    public IReportEngine reportEngine() throws BirtException {
        EngineConfig config = new EngineConfig();

        // Inyectamos las clases de Spring Boot y apagamos el modo Eclipse antiguo
        config.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, BirtConfig.class.getClassLoader());
        System.setProperty("RUN_UNDER_ECLIPSE", "false");
        config.setEngineHome("");

        Platform.startup(config);
        IReportEngineFactory factory = (IReportEngineFactory) Platform
                .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
        return factory.createReportEngine(config);
    }
}