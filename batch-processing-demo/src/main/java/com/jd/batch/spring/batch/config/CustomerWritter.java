package com.jd.batch.spring.batch.config;

import com.jd.batch.spring.batch.entity.Customer;
import com.jd.batch.spring.batch.repository.CustomerRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by jd birla on 03-02-2023 at 08:46
 */
@Component
public class CustomerWritter implements ItemWriter<Customer> {
    @Autowired
    private CustomerRepository customerRepository;


    @Override
    public void write(List<? extends Customer> list) throws Exception {
        System.out.println("Thread Name : "+Thread.currentThread().getName());
        customerRepository.saveAll(list);

    }
}
