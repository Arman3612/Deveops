package com.exam.proctor.config;

import com.exam.proctor.dto.AnswerDTO;
import com.exam.proctor.service.SubmissionWorkerService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Profile("!docker")
public class LocalMockConfig {

    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate() {
        final ConcurrentHashMap<String, Object> memStore = new ConcurrentHashMap<>();

        @SuppressWarnings("unchecked")
        final ValueOperations<String, Object> mockOps = (ValueOperations<String, Object>) Proxy.newProxyInstance(
                ValueOperations.class.getClassLoader(),
                new Class<?>[]{ValueOperations.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("set".equals(name) && args != null && args.length >= 2) {
                        memStore.put(String.valueOf(args[0]), args[1]);
                        return null;
                    } else if ("get".equals(name) && args != null && args.length >= 1) {
                        return memStore.get(String.valueOf(args[0]));
                    } else if ("increment".equals(name) && args != null && args.length >= 1) {
                        String key = String.valueOf(args[0]);
                        long delta = (args.length >= 2 && args[1] instanceof Number) ? ((Number) args[1]).longValue() : 1L;
                        Object cur = memStore.get(key);
                        long curVal = cur instanceof Number ? ((Number) cur).longValue() : 0L;
                        long nextVal = curVal + delta;
                        memStore.put(key, nextVal);
                        return nextVal;
                    }
                    return null;
                }
        );

        return new RedisTemplate<String, Object>() {
            @Override
            public void afterPropertiesSet() {
            }

            @Override
            public ValueOperations<String, Object> opsForValue() {
                return mockOps;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(RabbitTemplate.class)
    public RabbitTemplate rabbitTemplate(ObjectProvider<SubmissionWorkerService> workerServiceProvider) {
        return new RabbitTemplate() {
            @Override
            public void afterPropertiesSet() {
            }

            @Override
            public void convertAndSend(String routingKey, Object message) {
                directDispatch(message);
            }

            @Override
            public void convertAndSend(String exchange, String routingKey, Object message) {
                directDispatch(message);
            }

            private void directDispatch(Object message) {
                if (message instanceof AnswerDTO) {
                    SubmissionWorkerService worker = workerServiceProvider.getIfAvailable();
                    if (worker != null) {
                        try {
                            worker.consumeSubmission((AnswerDTO) message);
                        } catch (Exception e) {
                            System.err.println("Local submission worker error: " + e.getMessage());
                        }
                    }
                }
            }
        };
    }
}
