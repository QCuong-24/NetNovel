package com.example.netnovel_server.audio.event;

import com.example.netnovel_server.audio.dto.ChapterAudioJobMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AudioGenerationMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(AudioGenerationMessagePublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;

    public AudioGenerationMessagePublisher(
        RabbitTemplate rabbitTemplate,
        @Value("${app.audio.rabbit.exchange:netnovel.audio}") String exchangeName,
        @Value("${app.audio.rabbit.generation-routing-key:audio.generation}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAudioGeneration(AudioGenerationRequestedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                exchangeName,
                routingKey,
                ChapterAudioJobMessage.builder().assetId(event.assetId()).build()
            );
            log.info("Published audio generation job. assetId={}", event.assetId());
        } catch (AmqpException exception) {
            log.warn("Could not publish audio generation job. assetId={}", event.assetId(), exception);
        }
    }
}
