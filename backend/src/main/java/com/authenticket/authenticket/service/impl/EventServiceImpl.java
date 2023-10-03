package com.authenticket.authenticket.service.impl;

import com.authenticket.authenticket.dto.artist.ArtistDisplayDto;
import com.authenticket.authenticket.dto.artist.ArtistDtoMapper;
import com.authenticket.authenticket.dto.event.*;
import com.authenticket.authenticket.dto.section.SectionDtoMapper;
import com.authenticket.authenticket.dto.section.SectionTicketDetailsDto;
import com.authenticket.authenticket.exception.AlreadyExistsException;
import com.authenticket.authenticket.exception.NonExistentException;
import com.authenticket.authenticket.model.*;
import com.authenticket.authenticket.repository.*;
import com.authenticket.authenticket.service.AmazonS3Service;
import com.authenticket.authenticket.service.EmailService;
import com.authenticket.authenticket.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    private final ArtistRepository artistRepository;

    private final FeaturedEventRepository featuredEventRepository;

    private final TicketCategoryRepository ticketCategoryRepository;

    private final TicketPricingRepository ticketPricingRepository;

    private final EventDtoMapper eventDTOMapper;

    private final SectionDtoMapper sectionDtoMapper;

    private final ArtistDtoMapper artistDtoMapper;

    private final AmazonS3Service amazonS3Service;

    private final TicketRepository ticketRepository;

    private final EmailService emailService;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository,
                            ArtistRepository artistRepository,
                            FeaturedEventRepository featuredEventRepository,
                            TicketCategoryRepository ticketCategoryRepository,
                            TicketPricingRepository ticketPricingRepository,
                            EventDtoMapper eventDTOMapper,
                            ArtistDtoMapper artistDtoMapper,
                            AmazonS3Service amazonS3Service,
                            EmailService emailService,
                            TicketRepository ticketRepository,
                            SectionDtoMapper sectionDtoMapper) {
        this.eventRepository = eventRepository;
        this.artistRepository = artistRepository;
        this.featuredEventRepository = featuredEventRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.ticketPricingRepository = ticketPricingRepository;
        this.eventDTOMapper = eventDTOMapper;
        this.artistDtoMapper = artistDtoMapper;
        this.amazonS3Service = amazonS3Service;
        this.emailService = emailService;
        this.ticketRepository = ticketRepository;
        this.sectionDtoMapper = sectionDtoMapper;
    }

    //get all events for home page
    @Override
    public List<EventHomeDto> findAllPublicEvent(Pageable pageable) {
        return eventDTOMapper.mapEventHomeDto(eventRepository.findAllByReviewStatusAndDeletedAtIsNull(Event.ReviewStatus.APPROVED.getStatusValue(),pageable).getContent());
    }


    //find all events for admin
    @Override
    public List<EventAdminDisplayDto> findAllEvent() {
        return eventDTOMapper.mapEventAdminDisplayDto(eventRepository.findAllByOrderByEventIdAsc());
    }

    @Override
    public OverallEventDto findEventById(Integer eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            OverallEventDto overallEventDto = eventDTOMapper.applyOverallEventDto(event);
            return overallEventDto;
        }
        return null;
    }
    //find recently added events by created at date for home
    @Override
    public List<EventHomeDto> findRecentlyAddedEvents(Pageable pageable) {
        return eventDTOMapper.mapEventHomeDto(eventRepository.findAllByReviewStatusAndDeletedAtIsNullOrderByCreatedAtDesc(Event.ReviewStatus.APPROVED.getStatusValue(), pageable).getContent());

    }

    @Override
    public List<FeaturedEventDto> findFeaturedEvents(Pageable pageable) {
        Page<FeaturedEvent> featuredEvents = featuredEventRepository.findAllFeaturedEventsByStartDateBeforeAndEndDateAfter(LocalDateTime.now(),LocalDateTime.now(),pageable);
        return eventDTOMapper.mapFeaturedEventDto(featuredEvents.getContent());
    }

    @Override
    public List<EventHomeDto> findBestSellerEvents() {
        return eventDTOMapper.mapEventHomeDtoForObj(eventRepository.findBestSellerEvents());
    }

    @Override
    public List<EventHomeDto> findUpcomingEventsByTicketSalesDate(Pageable pageable) {
        LocalDateTime currentDate = LocalDateTime.now();
        return eventDTOMapper.mapEventHomeDto(eventRepository.findAllByReviewStatusAndTicketSaleDateAfterAndDeletedAtIsNullOrderByTicketSaleDateAsc(Event.ReviewStatus.APPROVED.getStatusValue(), currentDate,pageable).getContent());
    }

    @Override
    public List<EventHomeDto> findCurrentEventsByEventDate(Pageable pageable) {
        LocalDateTime currentDate = LocalDateTime.now();
        return eventDTOMapper.mapEventHomeDto(eventRepository.findAllByReviewStatusAndEventDateAfterAndDeletedAtIsNullOrderByEventDateAsc(Event.ReviewStatus.APPROVED.getStatusValue(), currentDate,pageable).getContent());
    }

    @Override
    public List<EventHomeDto> findPastEventsByEventDate(Pageable pageable) {
        LocalDateTime currentDate = LocalDateTime.now();
        return eventDTOMapper.mapEventHomeDto(eventRepository.findAllByReviewStatusAndEventDateBeforeAndDeletedAtIsNullOrderByEventDateDesc(Event.ReviewStatus.APPROVED.getStatusValue(), currentDate,pageable).getContent());
    }

    @Override
    public List<EventDisplayDto> findEventsByReviewStatus(String reviewStatus) {
        LocalDateTime currentDate = LocalDateTime.now();
        return eventDTOMapper.map(eventRepository.findAllByReviewStatusAndDeletedAtIsNullOrderByCreatedAtAsc(reviewStatus));
    }
    public List<EventHomeDto> findEventsByVenue(String reviewStatus, Integer venueId, Pageable pageable) {
        return eventDTOMapper.mapEventHomeDto(eventRepository.findAllByReviewStatusAndVenueVenueIdAndDeletedAtIsNullOrderByEventDateDesc(reviewStatus, venueId, pageable).getContent());
    }

    @Override
    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public FeaturedEventDto saveFeaturedEvent(FeaturedEvent featuredEvent) {

        return eventDTOMapper.applyFeaturedEventDto(featuredEventRepository.save(featuredEvent));
    }

    @Override
    public Event updateEvent(EventUpdateDto eventUpdateDto) {
        Optional<Event> eventOptional = eventRepository.findById(eventUpdateDto.eventId());

        if (eventOptional.isPresent()) {
            Event existingEvent = eventOptional.get();
            eventDTOMapper.update(eventUpdateDto, existingEvent);
            existingEvent.setUpdatedAt(LocalDateTime.now());
            // Send email
            String reviewStatus = eventUpdateDto.reviewStatus();
            if (reviewStatus != null) {
                if(reviewStatus.equals(Event.ReviewStatus.APPROVED.getStatusValue()) || reviewStatus.equals(Event.ReviewStatus.REJECTED.getStatusValue())) {
                    EventOrganiser eventOrganiser = existingEvent.getOrganiser();
                    // Send email to organiser
                    emailService.send(eventOrganiser.getEmail(), EmailServiceImpl.buildEventReviewEmail(existingEvent), "Event Review");
                }
            }

            eventRepository.save(existingEvent);
            return existingEvent;
        }

        throw new NonExistentException("Event", eventUpdateDto.eventId());
    }

    @Override
    public String deleteEvent(Integer eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);

        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            if (event.getDeletedAt() != null) {
                return String.format("Event %d is already deleted.", eventId);
            } else{
                event.setDeletedAt(LocalDateTime.now());
                eventRepository.save(event);
                return String.format("Event %d is successfully deleted.", eventId);
            }
        } else {
            throw new NonExistentException("Event", eventId);
        }

    }

    @Override
    public EventDisplayDto addArtistToEvent(Integer artistId, Integer eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        Optional<Artist> artistOptional = artistRepository.findById(artistId);

        if (artistOptional.isPresent() && eventOptional.isPresent()) {

            Artist artist = artistOptional.get();
            Event event = eventOptional.get();
            Set<Artist> artistSet = event.getArtists();
            if (artistSet == null) {
                artistSet = new HashSet<>();
            }
            if (!artistSet.contains(artist)) {

                artistSet.add(artist);
                event.setArtists(artistSet);

                eventRepository.save(event);
                return eventDTOMapper.apply(event);
            } else {
                throw new AlreadyExistsException("Artist already linked to stated event");
            }
        } else {
            if (artistOptional.isEmpty()) {
                throw new NonExistentException("Artist does not exists");
            } else {
                throw new NonExistentException("Event does not exists");
            }
        }
    }

//     public void removeAllArtistFromEvent(Integer eventId){
//         eventRepository.deleteAllArtistByEventId(eventId);
//         };
  
    @Override
    public EventDisplayDto addTicketCategory(Integer catId, Integer eventId, Double price) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        Optional<TicketCategory> categoryOptional = ticketCategoryRepository.findById(catId);
        if (categoryOptional.isPresent() && eventOptional.isPresent()) {
            TicketCategory ticketCategory = categoryOptional.get();
            Event event = eventOptional.get();

            Optional<TicketPricing> eventTicketCategoryOptional = ticketPricingRepository.findById(new EventTicketCategoryId(ticketCategory, event));
            if (eventTicketCategoryOptional.isPresent()) {
                throw new AlreadyExistsException("Ticket Category already linked to stated event");
            }

            event.addTicketPricing(ticketCategory, price);
            //adding to total tickets
//            Integer currentTotalTickets = event.getTotalTickets();
//            currentTotalTickets += totalTicketsPerCat;
//            event.setTotalTickets(currentTotalTickets);

            //adding to total tickets sold
//            Integer currentTotalTicketsSold = event.getTotalTicketsSold();
//            currentTotalTicketsSold += (totalTicketsPerCat - availableTickets);
//            event.setTotalTicketsSold(currentTotalTicketsSold);

            eventRepository.save(event);
            return eventDTOMapper.apply(event);
//            Set<EventTicketCategory> eventTicketCategorySet = event.getEventTicketCategorySet();

//            if(eventTicketCategoryOptional.isEmpty()){
//                EventTicketCategory eventTicketCategory = new EventTicketCategory(ticketCategory, event, price, availableTickets, totalTicketsPerCat);
//                eventTicketCategoryRepository.save(eventTicketCategory);
//
//                eventTicketCategorySet.add(new EventTicketCategory(ticketCategory, event, price, availableTickets, totalTicketsPerCat));
//                System.out.println(eventTicketCategorySet.size());
//                event.setEventTicketCategorySet(eventTicketCategorySet);
//
//                eventRepository.save(event);
//                return eventDTOMapper.apply(event);
//            } else {
//                throw new AlreadyExistsException("Ticket Category already linked to stated event");
//            }
        } else {
            if (categoryOptional.isEmpty()) {
                throw new NonExistentException("Category does not exist");
            } else {
                throw new NonExistentException("Event does not exist");
            }
        }
    }

    @Override
    public void updateTicketPricing(Integer catId, Integer eventId, Double price) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        Optional<TicketCategory> categoryOptional = ticketCategoryRepository.findById(catId);
        if (categoryOptional.isPresent() && eventOptional.isPresent()) {
            TicketCategory ticketCategory = categoryOptional.get();
            Event event = eventOptional.get();

            Optional<TicketPricing> ticketPricingOptional = ticketPricingRepository.findById(new EventTicketCategoryId(ticketCategory, event));
            if (ticketPricingOptional.isEmpty()) {
                throw new NonExistentException("Ticket Category " + ticketCategory.getCategoryName() + " is not linked to Event '" + event.getEventName() + "'");
            }
            TicketPricing ticketPricing = ticketPricingOptional.get();

            //adding to total tickets
//            Integer currentTotalTickets = event.getTotalTickets();
//            currentTotalTickets += totalTicketsPerCat;
//            event.setTotalTickets(currentTotalTickets);

            //adding to total tickets sold
//            Integer currentTotalTicketsSold = event.getTotalTicketsSold();
//            currentTotalTicketsSold += (totalTicketsPerCat - availableTickets);
//            event.setTotalTicketsSold(currentTotalTicketsSold);

            event.updateTicketPricing(ticketPricing, price);
            eventRepository.save(event);
        } else {
            if (categoryOptional.isEmpty()) {
                throw new NonExistentException("Category does not exist");
            } else {
                throw new NonExistentException("Event does not exist");
            }
        }
    }

    @Override
    public EventDisplayDto removeTicketCategory(Integer catId, Integer eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        Optional<TicketCategory> categoryOptional = ticketCategoryRepository.findById(catId);
        if (categoryOptional.isPresent() && eventOptional.isPresent()) {
            TicketCategory ticketCategory = categoryOptional.get();
            Event event = eventOptional.get();

            Optional<TicketPricing> eventTicketCategoryOptional = ticketPricingRepository.findById(new EventTicketCategoryId(ticketCategory, event));
            if (eventTicketCategoryOptional.isEmpty()) {
                throw new NonExistentException("Ticket Category not linked to stated event");
            }

            event.removeTicketCategory(ticketCategory);
            eventRepository.save(event);
            return eventDTOMapper.apply(event);
        } else {
            if (categoryOptional.isEmpty()) {
                throw new NonExistentException("Category does not exist");
            } else {
                throw new NonExistentException("Event does not exist");
            }
        }
    }

    @Override
    //return artist for a specific event
    public Set<ArtistDisplayDto> findArtistForEvent(Integer eventId) throws NonExistentException {

        if (eventRepository.findById(eventId).isEmpty()) {
            throw new NonExistentException("Event does not exist");
        }
        List<Object[]> artistObject = eventRepository.getArtistByEventId(eventId);
        Set<ArtistDisplayDto> artistDisplayDtoList = artistDtoMapper.mapArtistDisplayDto(artistObject);
        return artistDisplayDtoList;
    }

    @Override
    public List<SectionTicketDetailsDto> findAllSectionDetailsForEvent(Event event){
        return sectionDtoMapper.mapSectionTicketDetailsDto(ticketRepository.findAllTicketDetailsBySectionForEvent(event.getEventId()));
    };
}
