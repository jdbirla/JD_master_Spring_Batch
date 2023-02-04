package com.jd.batch.spring.batch.config;

import com.jd.batch.spring.batch.entity.Customer;
import org.springframework.batch.item.ItemProcessor;

/**
 * Created by jd birla on 03-02-2023 at 06:36
 */

public class CustomerProcessor implements ItemProcessor<Customer,Customer> {
    @Override
    public Customer process(Customer customer) throws Exception {
        int age = Integer.parseInt(customer.getAge());
        if(age>18)
        {
            return customer;
        }else
        {
            return null;
        }

    }
}
