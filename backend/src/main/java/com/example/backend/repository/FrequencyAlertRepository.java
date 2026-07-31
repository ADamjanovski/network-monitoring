package com.example.backend.repository;

import com.example.backend.model.FrequencyAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FrequencyAlertRepository extends JpaRepository<FrequencyAlert, String> {

    List<FrequencyAlert> findAllByTimestampBetween(Long start, Long end);
}
