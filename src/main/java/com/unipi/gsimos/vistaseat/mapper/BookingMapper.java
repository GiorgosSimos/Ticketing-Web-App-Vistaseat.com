package com.unipi.gsimos.vistaseat.mapper;

import com.unipi.gsimos.vistaseat.dto.BookingDto;
import com.unipi.gsimos.vistaseat.model.Booking;
import com.unipi.gsimos.vistaseat.model.EventOccurrence;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.repository.EventOccurrenceRepository;
import com.unipi.gsimos.vistaseat.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final EventOccurrenceRepository eventOccurrenceRepository;
    private final UserRepository userRepository;

    public Booking toEntity(BookingDto  bookingDto) {

        Booking booking = new Booking();

        EventOccurrence occurrence = eventOccurrenceRepository.findById(bookingDto.getOccurrenceId())
                .orElseThrow(() -> new EntityNotFoundException("Event Occurrence not found"));
        booking.setEventOccurrence(occurrence);

        User user = userRepository.findById(bookingDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        booking.setUser(user);
        booking.setFirstName(bookingDto.getFirstName());
        booking.setLastName(bookingDto.getLastName());
        booking.setPhoneNumber(bookingDto.getPhoneNumber());
        booking.setEmail(bookingDto.getEmail());
        booking.setBookingDate(bookingDto.getBookingDate());
        booking.setNumberOfTickets(bookingDto.getNumberOfTickets());
        booking.setTicketsPrice(bookingDto.getTicketsPrice());
        booking.setServiceFee(bookingDto.getServiceFee());
        booking.setTotalAmount(bookingDto.getTotalAmount());
        booking.setStatus(bookingDto.getStatus());
        booking.setExpiresAt(bookingDto.getExpiresAt());

        return booking;
    }

    public static BookingDto toDto(Booking booking) {

        BookingDto bookingDto = new BookingDto();

        bookingDto.setId(booking.getId());
        bookingDto.setOccurrenceId(booking.getEventOccurrence().getId());
        bookingDto.setEventId(booking.getEventOccurrence().getEvent().getId());
        bookingDto.setEventName(booking.getEventOccurrence().getEvent().getName());
        bookingDto.setFirstName(booking.getFirstName());
        bookingDto.setLastName(booking.getLastName());
        bookingDto.setPhoneNumber(booking.getPhoneNumber());
        bookingDto.setEmail(booking.getEmail());

        Long userId = booking.getUser() != null ? booking.getUser().getId() : null;
        bookingDto.setUserId(userId);

        bookingDto.setBookingDate(booking.getBookingDate());
        bookingDto.setNumberOfTickets(booking.getNumberOfTickets());
        bookingDto.setTicketsPrice(booking.getTicketsPrice());
        bookingDto.setServiceFee(booking.getServiceFee());
        bookingDto.setTotalAmount(booking.getTotalAmount());
        bookingDto.setStatus(booking.getStatus());
        bookingDto.setExpiresAt(booking.getExpiresAt());

        return bookingDto;
    }
}
