package com.jd.batch.spring.batch.listner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jd.batch.spring.batch.entity.Customer;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;

/**
 * Created by jd birla on 03-02-2023 at 09:52
 */
public class StepSkipListener implements SkipListener<Customer,Number> {

    Logger logger= LoggerFactory.getLogger(StepSkipListener.class);
    @Override
    public void onSkipInRead(Throwable throwable) {
        logger.info("A failure on read {}",throwable.getMessage());
    }

    @Override
    public void onSkipInWrite(Number item, Throwable throwable) {
        logger.info("A failure on write {} , {} ",throwable.getMessage() , item);


    }

    @SneakyThrows
    @Override
    public void onSkipInProcess(Customer customer, Throwable throwable) {
        logger.info("Item from logger {} was skipped due to the exception {} ",new ObjectMapper().writeValueAsString(customer) ,throwable.getMessage());

    }
}
