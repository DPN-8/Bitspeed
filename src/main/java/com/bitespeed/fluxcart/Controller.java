package com.bitespeed.fluxcart;

import com.bitespeed.fluxcart.Entity.Contact;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequiredArgsConstructor
@RequestMapping
public class Controller {

    private final ControllerService service;

    @PostMapping("/identify")
    public ResponseEntity<?> createContact(@RequestBody Contact contact) {
        return service.createContact(contact);
    }
}
