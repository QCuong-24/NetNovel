package com.example.netnovel_server.event;

import com.example.netnovel_server.dto.CrawlNovelRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CrawlTaskMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskMessagePublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String crawlExchangeName;
    private final String crawlNovelRequestRoutingKey;

    public CrawlTaskMessagePublisher(
        RabbitTemplate rabbitTemplate,
        @Value("${app.crawl.rabbit.exchange:netnovel.crawl}") String crawlExchangeName,
        @Value("${app.crawl.rabbit.novel-request-routing-key:crawl.novel.request}") String crawlNovelRequestRoutingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.crawlExchangeName = crawlExchangeName;
        this.crawlNovelRequestRoutingKey = crawlNovelRequestRoutingKey;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishCrawlTask(CrawlTaskCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                crawlExchangeName,
                crawlNovelRequestRoutingKey,
                CrawlNovelRequestMessage.builder()
                    .taskId(event.taskId())
                    .url(event.url())
                    .requestedByUserId(event.requestedByUserId())
                    .build()
            );
            log.info("Published crawl task message after database commit. taskId={}", event.taskId());
        } catch (AmqpException exception) {
            log.warn(
                "Could not publish crawl task message after commit. The pending recovery scheduler will retry it. taskId={}",
                event.taskId(),
                exception
            );
        }
    }
}
