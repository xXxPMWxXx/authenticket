package com.authenticket.authenticket.service.impl;

import com.authenticket.authenticket.dto.ticket.TicketDisplayDto;
import com.authenticket.authenticket.dto.ticket.TicketDisplayDtoMapper;
import com.authenticket.authenticket.dto.ticket.TicketUpdateDto;
import com.authenticket.authenticket.dto.ticket.TicketUpdateDtoMapper;
import com.authenticket.authenticket.model.Ticket;
import com.authenticket.authenticket.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketDisplayDtoMapper ticketDisplayDtoMapper;

    public List<TicketDisplayDto> findAllTicket() {
        return ticketRepository.findAll()
                .stream()
                .map(ticketDisplayDtoMapper)
                .collect(Collectors.toList());
    }

    public Optional<TicketDisplayDto> findTicketById(Integer ticketId) {
        return ticketRepository.findById(ticketId).map(ticketDisplayDtoMapper);
    }

    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public Ticket updateTicket(TicketUpdateDto ticketUpdateDto) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketUpdateDto.ticketId());

        if (ticketOptional.isPresent()) {
            Ticket existingTicket = ticketOptional.get();
            ticketDisplayDtoMapper.update(ticketUpdateDto, existingTicket);
            ticketRepository.save(existingTicket);
            return existingTicket;
        }

        return null;
    }


//    public String deleteTicket(Integer ticketId) {
//        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
//
//        if (ticketOptional.isPresent()) {
//            Ticket ticket = ticketOptional.get();
//            if(ticket.getDeletedAt()!=null){
//                return "event already deleted";
//            }
//
//            ticket.setDeletedAt(LocalDateTime.now());
//            ticketRepository.save(ticket);
//            return "event deleted successfully";
//        }
//
//        return "error: event deleted unsuccessfully, event might not exist";
//    }

    public Boolean removeTicket(Integer ticketId) {

        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);

        if (ticketOptional.isPresent()) {
            ticketRepository.deleteById(ticketId);
            return true;
        }
        return false;
    }
}
