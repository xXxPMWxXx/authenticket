package com.authenticket.authenticket.repository;

import com.authenticket.authenticket.dto.event.EventHomeDto;
import com.authenticket.authenticket.model.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    @Query(nativeQuery = true,
            value = "SELECT " +
                    "A.artist_id," +
                    "A.artist_name, " +
                    "A.artist_image, " +
                    "E.event_id, " +
                    "E.organiser_id, " +
                    "E.venue_id, " +
                    "E.event_name, " +
                    "E.event_description, " +
                    "E.category_id, " +
                    "E.event_date, " +
                    "E.event_location, " +
                    "E.other_event_info, " +
                    "E.total_tickets, " +
                    "E.total_tickets_sold, " +
                    "E.event_image, " +
                    "E.ticket_sale_date, " +
                    "E.type_id " +
                    "FROM " +
                    "dev.Artist AS A " +
                    "JOIN " +
                    "dev.Artist_Event AS AE ON A.artist_id = AE.artist_id " +
                    "JOIN " +
                    "dev.Event AS E ON AE.event_id = E.event_id")
    List<Object[]> getAssignedEvent();

    @Query(nativeQuery = true,
            value = "SELECT " +
                    "A.artist_id, " +
                    "A.artist_name, " +
                    "A.artist_image " +
                    "FROM " +
                    "dev.Artist AS A " +
                    "JOIN " +
                    "dev.Artist_Event AS AE ON A.artist_id = AE.artist_id " +
                    "JOIN " +
                    "dev.Event AS E ON AE.event_id = E.event_id " +
                    "WHERE E.event_id = :eventId")
    List<Object[]> getArtistByEventId(@Param("eventId") Integer eventId);

    //recently added
    List<Event> findTop7ByOrderByCreatedAtDesc();

    //upcoming ticket sales
    List<Event> findTop7ByTicketSaleDateAfterOrderByTicketSaleDateAsc(LocalDateTime currentDate);
}
