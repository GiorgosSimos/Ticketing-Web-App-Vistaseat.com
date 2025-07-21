package com.unipi.gsimos.vistaseat.dto;

// Helper DTO used to map the different event categories in the browse by category in home.html
public record CategoryCardDto(String title,
                              String image,
                              String description,
                              String eventCount,
                              String link) {
}
