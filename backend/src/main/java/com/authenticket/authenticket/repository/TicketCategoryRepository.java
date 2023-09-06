package com.authenticket.authenticket.repository;

import com.authenticket.authenticket.model.Event;
import com.authenticket.authenticket.model.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Integer> {
}
