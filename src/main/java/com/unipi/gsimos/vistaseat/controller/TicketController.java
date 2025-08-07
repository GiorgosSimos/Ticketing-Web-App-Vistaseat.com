package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
                                               @RequestParam(defaultValue = "view") String mode){

        byte[] pdfTicket = ticketService.generatePdfTicket(ticketId);

        String dispositionType = mode.equals("view") ? "inline" : "attachment";
        String rawFilename = "ticket-" + ticketId + ".pdf";
        String encodedFilename = URLEncoder.encode(rawFilename, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, dispositionType + "; filename=" + encodedFilename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfTicket);
    }
}
