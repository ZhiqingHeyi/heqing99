package com.hotel.repository;

import com.hotel.entity.CheckInRecord;
import com.hotel.entity.Visitor;
import com.hotel.entity.Visitor.IdType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    List<Visitor> findByCheckInRecord(CheckInRecord checkInRecord);
    
    List<Visitor> findByIdTypeAndIdNumber(IdType idType, String idNumber);
    
    List<Visitor> findByName(String name);
    
    List<Visitor> findByPhone(String phone);
    
    boolean existsByIdTypeAndIdNumber(IdType idType, String idNumber);
}