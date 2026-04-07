package com.flash.film.common.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Exchange names ──────────────────────────────────────────────────────
    public static final String FILM_EXCHANGE = "film.exchange";
    public static final String FILM_DL_EXCHANGE = "film.dl.exchange";

    // ── Queue names ─────────────────────────────────────────────────────────
    public static final String FILM_QUEUE = "film.queue";
    public static final String FILM_DL_QUEUE = "film.dl.queue";

    // ── Routing keys ─────────────────────────────────────────────────────────
    public static final String FILM_ROUTING_KEY = "film.key";
    public static final String FILM_DL_ROUTING_KEY = "film.dl.key";

    // ── Exchanges ────────────────────────────────────────────────────────────

    @Bean
    public DirectExchange filmExchange() {
        return new DirectExchange(FILM_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange filmDeadLetterExchange() {
        return new DirectExchange(FILM_DL_EXCHANGE, true, false);
    }

    // ── Queues ───────────────────────────────────────────────────────────────

    @Bean
    public Queue filmQueue() {
        return QueueBuilder.durable(FILM_QUEUE)
                .withArgument("x-dead-letter-exchange", FILM_DL_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", FILM_DL_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue filmDeadLetterQueue() {
        return QueueBuilder.durable(FILM_DL_QUEUE).build();
    }

    // ── Bindings ──────────────────────────────────────────────────────────────

    @Bean
    public Binding filmBinding() {
        return BindingBuilder.bind(filmQueue()).to(filmExchange()).with(FILM_ROUTING_KEY);
    }

    @Bean
    public Binding filmDlBinding() {
        return BindingBuilder.bind(filmDeadLetterQueue()).to(filmDeadLetterExchange()).with(FILM_DL_ROUTING_KEY);
    }

    // ── Message converter ────────────────────────────────────────────────────

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setDefaultRequeueRejected(false); // nack → DLQ, không requeue
        return factory;
    }
}
