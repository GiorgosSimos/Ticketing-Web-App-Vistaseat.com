package com.unipi.gsimos.vistaseat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VenueDto {

    private Long id;
    private String name;
    private String street;
    private Integer number;
    private Integer zipcode;
    private String city;
    private Integer capacity;
    private Long adminId;

}
