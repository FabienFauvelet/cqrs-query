package org.acme.out.postgres.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.acme.domain.model.Customer;
import org.acme.out.postgres.entity.CustomerEntity;
import org.acme.out.mapper.CustomerMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class CustomerRepository implements PanacheRepository<CustomerEntity> {
    @Inject
    CustomerMapper customerMapper;
    @Transactional
    public void createCustomer(Customer customer){
        CustomerEntity customerEntity =  customerMapper.toCustomerEntity(customer);
        persist(customerEntity);
    }
}