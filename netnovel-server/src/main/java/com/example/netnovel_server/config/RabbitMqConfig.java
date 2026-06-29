package com.example.netnovel_server.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    public DirectExchange audioExchange(@Value("${app.audio.rabbit.exchange:netnovel.audio}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue audioGenerationQueue(@Value("${app.audio.rabbit.generation-queue:audio.generation}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding audioGenerationBinding(
        Queue audioGenerationQueue,
        DirectExchange audioExchange,
        @Value("${app.audio.rabbit.generation-routing-key:audio.generation}") String routingKey
    ) {
        return BindingBuilder.bind(audioGenerationQueue).to(audioExchange).with(routingKey);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
        ConnectionFactory connectionFactory,
        JacksonJsonMessageConverter jacksonJsonMessageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jacksonJsonMessageConverter);
        return rabbitTemplate;
    }
}
