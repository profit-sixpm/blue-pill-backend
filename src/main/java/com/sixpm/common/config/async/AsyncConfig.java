package com.sixpm.common.config.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public AsyncTaskExecutor taskExecutor() {
        // Virtual Thread를 사용하는 Executor 생성
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}
