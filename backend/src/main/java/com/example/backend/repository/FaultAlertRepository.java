package com.example.backend.repository;

import com.example.backend.model.FaultAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaultAlertRepository extends JpaRepository<FaultAlert,String> {
    List<FaultAlert> findAllByTimestampBetween(Long start, Long end);
}
