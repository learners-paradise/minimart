package com.minimart.users;

import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer findOrCreate(String name, String email) {
        return customerRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    Customer customer = new Customer();
                    customer.setName(name);
                    customer.setEmail(email);
                    return customerRepository.save(customer);
                });
    }
}
