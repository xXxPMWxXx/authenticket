package com.authenticket.authenticket.service.impl;

import com.authenticket.authenticket.dto.event.EventDto;
import com.authenticket.authenticket.dto.event.EventDtoMapper;
import com.authenticket.authenticket.model.Event;
import com.authenticket.authenticket.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventDtoMapper eventDTOMapper;


    public List<EventDto> findAllEvent() {
        return eventRepository.findAll()
                .stream()
                .map(eventDTOMapper)
                        .collect(Collectors.toList());
    }

    public Optional<EventDto> findById(Integer event_id) {
        return eventRepository.findById(event_id).map(eventDTOMapper);
    }

    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    public Event updateEvent(Event event) {
        return eventRepository.save(event);
    }


    public String deleteEvent(Integer event_id) {
        Optional<Event> eventOptional = eventRepository.findById(event_id);

        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            event.setDeletedAt(LocalDateTime.now());
            eventRepository.save(event);
            return "event deleted successfully";
        }

        return "error: event deleted unsuccessfully";
    }

    public String removeEvent(Integer event_id){

        Optional<Event> eventOptional = eventRepository.findById(event_id);

        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            eventRepository.deleteById(event_id);
            return "event removed successfully";
        }
        return "error: event does not exist";

    }
}
