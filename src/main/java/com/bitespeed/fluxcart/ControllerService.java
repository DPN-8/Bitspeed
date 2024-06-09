package com.bitespeed.fluxcart;

import com.bitespeed.fluxcart.Entity.Contact;
import com.bitespeed.fluxcart.Entity.ContactResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ControllerService {
    private final ContactRepository contactRepository;
    public ResponseEntity<?> createContact(Contact contact) {
        ContactResponse response = new ContactResponse();
        ArrayList<String> email = new ArrayList<>();
        ArrayList<String> phoneNumber = new ArrayList<>();
        ArrayList<Integer> secondaryContactIds = new ArrayList<>();
        if (!contactRepository.existsByEmailOrPhoneNumber(contact.getEmail(), contact.getPhoneNumber())) {
            if (contact.getEmail().isEmpty() || contact.getPhoneNumber().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Email and phone number should not be null");
            }
            contact.setLinkedPrecedence(Precedence.PRIMARY);
            contactRepository.save(contact);
            email.add(contact.getEmail());
            Contact primaryContact = contactRepository.findPrimaryContact(contact.getEmail(), contact.getPhoneNumber());
            response.setPrimaryContactId(primaryContact.getId());
            response.setEmail(email);
            phoneNumber.add(contact.getPhoneNumber());
            response.setPhoneNumbers(phoneNumber);
        } else {
            Contact reqContact = contactRepository.findByEmailOrPhoneNumber(contact.getEmail(), contact.getPhoneNumber()).get(0);
            if (contactRepository.checkForEmailAndPhoneNumber(contact.getEmail(), contact.getPhoneNumber())) {
                Contact emailContact = contactRepository.findByEmail(contact.getEmail());
                Contact phoneContact = contactRepository.findByPhoneNumber(contact.getPhoneNumber());
                phoneContact.setLinkedPrecedence(Precedence.SECONDARY);
                phoneContact.setLinkedId(emailContact.getId());
                contactRepository.save(phoneContact);
            }
            if (contact.getEmail() != null && contact.getPhoneNumber() != null) {
                contact.setLinkedId(reqContact.getId());
                contact.setLinkedPrecedence(Precedence.SECONDARY);
                contactRepository.save(contact);
            }
            sendResponse(response, reqContact, email, phoneNumber, secondaryContactIds, contact);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private void sendResponse(ContactResponse response, Contact reqContact, ArrayList<String> email, ArrayList<String> phoneNumber, ArrayList<Integer> secondaryContactIds, Contact contact) {
        Contact primaryContact = new Contact();
        if(reqContact.getLinkedPrecedence() == Precedence.PRIMARY) {
            primaryContact = reqContact;
        } else if (reqContact.getLinkedPrecedence() == Precedence.SECONDARY) {
            primaryContact = contactRepository.findById(reqContact.getLinkedId()).get();
        }
        List<Contact> secondaryContact = new ArrayList<>(contactRepository.findByLinkedId(primaryContact.getId()));
        email.add(primaryContact.getEmail());
        phoneNumber.add(primaryContact.getPhoneNumber());
        for (Contact con : secondaryContact) {
            if(!email.contains(con.getEmail())) email.add(con.getEmail());
            if(!phoneNumber.contains(con.getPhoneNumber())) phoneNumber.add(con.getPhoneNumber());
            secondaryContactIds.add(con.getId());
        }
        response.setPrimaryContactId(primaryContact.getId());
        response.setEmail(email);
        response.setSecondaryId(secondaryContactIds);
        response.setPhoneNumbers(phoneNumber);
    }
}
