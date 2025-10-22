package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.Event;
import com.unipi.gsimos.vistaseat.model.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    boolean existsByNameAndEventTypeAndVenueId(String name, EventType eventType, Long venueId);

    Page<Event> findEventByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Event> findEventByNameContainingIgnoreCaseAndVenueId(String name, Long venueId, Pageable pageable);

    Page<Event> findAllByEventType(EventType eventType, Pageable pageable);

    Page<Event> findEventByNameContainingIgnoreCaseAndEventType(String name, EventType eventType, Pageable pageable);

    List<Event> findAllByVenueId(Long venueId);

    @Query("""
  select e
  from Event e
  join e.occurrences o
  where lower(e.name) like lower(concat('%', :name, '%'))
    and o.eventDate > :now
  group by e
  order by min(o.eventDate) asc
""")
    List<Event> findUpcomingByNameGroupBy(@Param("name") String name,
                                          @Param("now") LocalDateTime now);

    List<Event> findTop5ByNameContainingIgnoreCase(String eventName);

    Page<Event> findAllByVenueId(Long venueId, Pageable pageable);

    @Query("""
       select o.event
       from EventOccurrence o
       where o.id = :occurrenceId
       """)
    Event findEventByOccurrenceId(@Param("occurrenceId") Long occurrenceId);

    Long countByVenueId(Long venueId);

    @Query("""
        select distinct e
        from Event e
        where exists (select 1
                      from EventOccurrence o
                      where o.event = e AND o.eventDate > CURRENT TIMESTAMP
                     )
    """)
    List<Event> findUpcomingWithAtLeastOneOccurrence();

    @Query("""
        select distinct e
        from Event e
        where e.eventType = :type
            and exists (select 1
                        from EventOccurrence o
                        where o.event = e AND o.eventDate > CURRENT TIMESTAMP
                        )
    """)
    List<Event> findUpcomingWithAtLeastOneOccurrenceByType(@Param("type") EventType type);

    @Query("""
        select distinct e
        from Event e
        where e.eventType = :type
            and lower(e.name) like lower(concat('%', :name, '%'))
            and exists (select 1
                        from EventOccurrence o
                        where o.event = e AND o.eventDate > CURRENT TIMESTAMP
                        )
    """)
    List<Event> findUpcomingWithAtLeastOneOccurrenceByTypeAndNameLike(@Param("type") EventType type,
                                                                      @Param("name") String name);

    @Query("""
        select distinct e
        from Event e
        where e.eventType = :type
          and exists (
             select 1 from EventOccurrence o
             where o.event = e AND o.eventDate > CURRENT TIMESTAMP
               and o.eventDate between :from and :to
          )
        """)
    List<Event> findUpcomingWithAtLeastOneOccurrenceByTypeAndDateRange(@Param("type") EventType type,
                                                                       @Param("from") LocalDateTime from,
                                                                       @Param("to")   LocalDateTime to);

    @Query("""
        select distinct e
        from Event e
        where e.eventType = :type
          and lower(e.name) like lower(concat('%', :name, '%'))
          and exists (
             select 1 from EventOccurrence o
             where o.event = e AND o.eventDate > CURRENT TIMESTAMP
               and o.eventDate between :from and :to
          )
        """)
    List<Event> findUpcomingWithAtLeastOneOccurrenceByTypeAndNameLikeAndDateRange(
            @Param("type") EventType type,
            @Param("name") String name,
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);

    @Query("""
        select distinct e
        from Event e
        where e.venue.id = :venueId
          and (:name is null or lower(e.name) like :name)
          and exists(
            select 1 from EventOccurrence o
            where o.event = e AND o.eventDate > CURRENT TIMESTAMP
                and o.eventDate >= coalesce(:from, o.eventDate)
                and o.eventDate <= coalesce(:to,   o.eventDate)
          )
""")
    List<Event> findUpcomingEventsAtVenue(@Param("venueId") Long venueId,
                                          @Param("name") String name,
                                          @Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to
    );
}
