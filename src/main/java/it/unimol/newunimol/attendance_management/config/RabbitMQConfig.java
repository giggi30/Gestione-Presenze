package it.unimol.newunimol.attendance_management.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.attendance}")
    private String attendanceExchange;

    @Value("${rabbitmq.exchange.microservices}")
    private String microservicesExchange;

    @Value("${rabbitmq.queue.course.scheduled}")
    private String courseScheduledQueue;

    @Value("${rabbitmq.queue.course.updated}")
    private String courseUpdatedQueue;

    @Value("${rabbitmq.queue.report.requested}")
    private String reportRequestedQueue;

    @Value("${rabbitmq.queue.attendance.deleted}")
    private String attendanceDeletedQueue;

    @Value("${rabbitmq.queue.attendance.created}")
    private String attendanceCreatedQueue;

    @Value("${rabbitmq.queue.attendance.updated}")
    private String attendanceUpdatedQueue;

    @Value("${rabbitmq.queue.attendance.stats}")
    private String attendanceStatsQueue;

    // Exchanges
    @Bean
    public TopicExchange attendanceExchange() {
        return new TopicExchange(attendanceExchange);
    }

    @Bean
    public TopicExchange microservicesExchange() {
        return new TopicExchange(microservicesExchange);
    }

    // Code per eventi consumati
    @Bean
    public Queue courseScheduledQueue() {
        return QueueBuilder.durable(courseScheduledQueue).build();
    }

    @Bean
    public Queue courseUpdatedQueue() {
        return QueueBuilder.durable(courseUpdatedQueue).build();
    }

    @Bean
    public Queue reportRequestedQueue() {
        return QueueBuilder.durable(reportRequestedQueue).build();
    }

    @Bean
    public Queue attendanceDeletedQueue() {
        return QueueBuilder.durable(attendanceDeletedQueue).build();
    }

    @Bean
    public Queue attendanceCreatedQueue() {
        return QueueBuilder.durable(attendanceCreatedQueue).build();
    }

    @Bean
    public Queue attendanceUpdatedQueue() {
        return QueueBuilder.durable(attendanceUpdatedQueue).build();
    }

    @Bean
    public Queue attendanceStatsQueue() {
        return QueueBuilder.durable(attendanceStatsQueue).build();
    }

    // Bindings per eventi consumati
    @Bean
    public Binding courseScheduledBinding() {
        return BindingBuilder
                .bind(courseScheduledQueue())
                .to(microservicesExchange())
                .with("course.scheduled");
    }

    @Bean
    public Binding courseUpdatedBinding() {
        return BindingBuilder
                .bind(courseUpdatedQueue())
                .to(microservicesExchange())
                .with("course.updated");
    }

    @Bean
    public Binding reportRequestedBinding() {
        return BindingBuilder
                .bind(reportRequestedQueue())
                .to(microservicesExchange())
                .with("report.requested");
    }

    @Bean
    public Binding attendanceDeletedBinding() {
        return BindingBuilder
                .bind(attendanceDeletedQueue())
                .to(attendanceExchange())
                .with("attendance.deleted");
    }

    @Bean
    public Binding attendanceCreatedBinding() {
        return BindingBuilder
                .bind(attendanceCreatedQueue())
                .to(attendanceExchange())
                .with("attendance.created");
    }

    @Bean
    public Binding attendanceUpdatedBinding() {
        return BindingBuilder
                .bind(attendanceUpdatedQueue())
                .to(attendanceExchange())
                .with("attendance.updated");
    }

    @Bean
    public Binding attendanceStatsBinding() {
        return BindingBuilder
                .bind(attendanceStatsQueue())
                .to(attendanceExchange())
                .with("attendance.stats.generated");
    }

    // Configurazione JSON converter
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
}