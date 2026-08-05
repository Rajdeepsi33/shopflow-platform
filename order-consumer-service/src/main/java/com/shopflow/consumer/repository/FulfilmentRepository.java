package com.shopflow.consumer.repository;

import com.shopflow.consumer.entity.Fulfilment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FulfilmentRepository extends JpaRepository<Fulfilment, Long> {

    Optional<Fulfilment> findByOrderRef(String orderRef);

    List<Fulfilment> findByWarehouseCode(String warehouseCode);

    boolean existsByOrderRef(String orderRef);
}