package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.exception.ResourceNotFoundException;
import com.unipi.gsimos.vistaseat.model.TicketRenderMode;
import com.unipi.gsimos.vistaseat.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/api/tickets/{ticketId}/pdf")
    public ResponseEntity<byte[]> getPdfTicket(@PathVariable Long ticketId,
                                               @RequestParam(defaultValue = "VIEW") TicketRenderMode mode){

        try {
            byte[] pdfTicket = ticketService.generatePdfTicket(ticketId);

            String rawFilename = "ticket-" + ticketId + ".pdf";
            String encodedFilename = URLEncoder.encode(rawFilename, StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, String.format("%s; filename=\"%s\"; filename*=UTF-8''%s",
                                    mode.getContentDispositionValue(), rawFilename, encodedFilename))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfTicket);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/booking/{bookingId}/ticketsPdf")
    public ResponseEntity<byte[]> getAllPDFTickets(@PathVariable Long bookingId){

        TicketRenderMode mode = TicketRenderMode.DOWNLOAD;

        try {
            byte[] pdfTicket = ticketService.generatePdfTicketsForBooking(bookingId);

            String rawFilename = "booking-" + bookingId + "tickets.pdf";
            String encodedFilename = URLEncoder.encode(rawFilename, StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, String.format("%s; filename=\"%s\"; filename*=UTF-8''%s",
                            mode.getContentDispositionValue(), rawFilename, encodedFilename))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfTicket);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
}
