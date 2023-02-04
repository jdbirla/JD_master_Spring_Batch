package com.jd.batch.spring.batch.repository;

import com.jd.batch.spring.batch.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by jd birla on 03-02-2023 at 06:12
 */
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}
