package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.BookingDto;
import com.unipi.gsimos.vistaseat.dto.EventDto;
import com.unipi.gsimos.vistaseat.mapper.BookingMapper;
import com.unipi.gsimos.vistaseat.mapper.EventMapper;
import com.unipi.gsimos.vistaseat.model.Booking;
import com.unipi.gsimos.vistaseat.model.BookingStatus;
import com.unipi.gsimos.vistaseat.model.Event;
import com.unipi.gsimos.vistaseat.repository.BookingRepository;
import com.unipi.gsimos.vistaseat.repository.EventRepository;
import com.unipi.gsimos.vistaseat.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final BookingMapper bookingMapper;
    private final BookingService bookingService;

    @GetMapping("/api/payments/{bookingId}")
    public String makePayment(@PathVariable Long bookingId, Model model){

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        BookingDto bookingDto = bookingMapper.toDto(booking);

        Event event = eventRepository.findEventByOccurrenceId(booking.getEventOccurrence().getId());

        EventDto eventDto = eventMapper.toDto(event);

        model.addAttribute("booking", bookingDto);
        model.addAttribute("CONFIRMED", BookingStatus.CONFIRMED);
        model.addAttribute("PENDING", BookingStatus.PENDING);
        model.addAttribute("CANCELLED", BookingStatus.CANCELLED);
        model.addAttribute("event", eventDto);

        return "makePayment";
    }

    @PostMapping("/api/payments/{bookingId}/card-sim")
    public ResponseEntity<Void> simulatePayment(@PathVariable Long bookingId){

        bookingService.confirmBooking(bookingId);
        return ResponseEntity.ok().build();
    }
}
