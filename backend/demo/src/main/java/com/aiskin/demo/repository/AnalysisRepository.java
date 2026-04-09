package com.aiskin.demo.repository;

import com.aiskin.demo.model.AnalysisRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisRepository extends JpaRepository<AnalysisRecord, Long> {

}