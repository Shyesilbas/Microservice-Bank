package com.serhat.creditcard.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopics {


    @Bean
    public NewTopic cardCreated(){
        return TopicBuilder
                .name("Card-created")
                .build();
    }
    @Bean
    public NewTopic cardDebtPayed(){
        return TopicBuilder
                .name("CardDebtPayed")
                .build();
    }

}
