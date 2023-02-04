package com.jd.batch.spring.batch.config;

import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;

/**
 * Created by jd birla on 03-02-2023 at 09:41
 */
public class CustomExceptionSkipPolicy implements SkipPolicy {
    @Override
    public boolean shouldSkip(Throwable throwable, int i) throws SkipLimitExceededException {
        return throwable instanceof NumberFormatException;
    }
}
