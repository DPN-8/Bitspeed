package com.bitespeed.fluxcart.Entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactResponse {
    private int primaryContactId;
    private List<String> email;
    private List<String> phoneNumbers;
    private List<Integer> secondaryId;

}
