package com.reportsystem.restaurant.infrastructure.config;

import com.reportsystem.restaurant.domain.port.outbound.*;
import com.reportsystem.restaurant.domain.service.*;
import com.reportsystem.restaurant.infrastructure.persistence.repository.*;
import com.reportsystem.restaurant.infrastructure.persistence.adapter.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestaurantConfig {

    private final JpaOutletRepository outletRepo;
    private final JpaTableRepository tableRepo;
    private final JpaCategoryRepository categoryRepo;
    private final JpaMenuItemRepository menuItemRepo;
    private final JpaOrderRepository orderRepo;
    private final JpaOrderItemRepository orderItemRepo;
    private final JpaCustomerRepository customerRepo;
    private final JpaReservationRepository reservationRepo;

    public RestaurantConfig(JpaOutletRepository outletRepo, JpaTableRepository tableRepo,
                            JpaCategoryRepository categoryRepo, JpaMenuItemRepository menuItemRepo,
                            JpaOrderRepository orderRepo, JpaOrderItemRepository orderItemRepo,
                            JpaCustomerRepository customerRepo, JpaReservationRepository reservationRepo) {
        this.outletRepo = outletRepo; this.tableRepo = tableRepo;
        this.categoryRepo = categoryRepo; this.menuItemRepo = menuItemRepo;
        this.orderRepo = orderRepo; this.orderItemRepo = orderItemRepo;
        this.customerRepo = customerRepo; this.reservationRepo = reservationRepo;
    }

    @Bean
    public OutletRepository outletRepository() { return new JpaOutletAdapter(outletRepo); }

    @Bean
    public TableRepository tableRepository() { return new JpaTableAdapter(tableRepo); }

    @Bean
    public CategoryRepository categoryRepository() { return new JpaCategoryAdapter(categoryRepo); }

    @Bean
    public MenuItemRepository menuItemRepository() { return new JpaMenuItemAdapter(menuItemRepo); }

    @Bean
    public OrderRepository orderRepository() { return new JpaOrderAdapter(orderRepo); }

    @Bean
    public OrderItemRepository orderItemRepository() { return new JpaOrderItemAdapter(orderItemRepo); }

    @Bean
    public CustomerRepository customerRepository() { return new JpaCustomerAdapter(customerRepo); }

    @Bean
    public ReservationRepository reservationRepository() { return new JpaReservationAdapter(reservationRepo); }

    @Bean
    public OutletServiceImpl outletService(OutletRepository oRepo) {
        return new OutletServiceImpl(oRepo);
    }

    @Bean
    public MenuServiceImpl menuService(CategoryRepository catRepo, MenuItemRepository miRepo) {
        return new MenuServiceImpl(catRepo, miRepo);
    }

    @Bean
    public OrderServiceImpl orderService(OrderRepository oRepo, OrderItemRepository oiRepo) {
        return new OrderServiceImpl(oRepo, oiRepo);
    }

    @Bean
    public CustomerServiceImpl customerService(CustomerRepository cRepo) {
        return new CustomerServiceImpl(cRepo);
    }

    @Bean
    public ReservationServiceImpl reservationService(ReservationRepository rRepo) {
        return new ReservationServiceImpl(rRepo);
    }
}
