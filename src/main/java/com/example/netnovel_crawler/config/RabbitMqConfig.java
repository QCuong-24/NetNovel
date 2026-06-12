package com.example.netnovel_crawler.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public DirectExchange crawlExchange(@Value("${app.crawl.rabbit.exchange:netnovel.crawl}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue crawlNovelRequestQueue(@Value("${app.crawl.rabbit.novel-request-queue:crawl.novel.request}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding crawlNovelRequestBinding(
        Queue crawlNovelRequestQueue,
        DirectExchange crawlExchange,
        @Value("${app.crawl.rabbit.novel-request-routing-key:crawl.novel.request}") String routingKey
    ) {
        return BindingBuilder.bind(crawlNovelRequestQueue).to(crawlExchange).with(routingKey);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
        ConnectionFactory connectionFactory,
        JacksonJsonMessageConverter jacksonJsonMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonJsonMessageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
