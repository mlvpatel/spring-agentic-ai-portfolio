package com.portfolio.shared.ai;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
public class PortfolioAiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PortfolioAiClient.class)
    public PortfolioAiClient portfolioAiClient(ObjectProvider<ChatModel> chatModels, Environment environment) {
        return new PortfolioAiClient(chatModels, environment);
    }
}
