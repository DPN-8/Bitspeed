package com.bitespeed.fluxcart;

import com.bitespeed.fluxcart.Entity.Contact;
import com.bitespeed.fluxcart.Entity.ContactResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ControllerService {

    private final ContactRepository contactRepository;

    public ResponseEntity<?> createContact(Contact contact) {

        ContactResponse response = new ContactResponse();
        List<String> emails = new ArrayList<>();
        List<String> phoneNumbers = new ArrayList<>();
        List<Integer> secondaryContactIds = new ArrayList<>();

        if (!contactRepository.existsByEmailOrPhoneNumber(contact.getEmail(), contact.getPhoneNumber())) {
            createPrimaryContact(contact, response, emails, phoneNumbers);
        } else {
            handleExistingContact(contact, response, emails, phoneNumbers, secondaryContactIds);
        }

        return ResponseEntity.ok(response);
    }

    private boolean isInvalidContact(Contact contact) {
        return contact.getEmail() == null || contact.getEmail().isEmpty()
                || contact.getPhoneNumber() == null || contact.getPhoneNumber().isEmpty();
    }

    private void createPrimaryContact(Contact contact, ContactResponse response, List<String> emails, List<String> phoneNumbers) {
        if (isInvalidContact(contact)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Email and phone number should not be null");
        }
        contact.setLinkedPrecedence(Precedence.PRIMARY);
        contactRepository.save(contact);

        emails.add(contact.getEmail());
        phoneNumbers.add(contact.getPhoneNumber());

        Contact primaryContact = contactRepository.findPrimaryContact(contact.getEmail(), contact.getPhoneNumber());
        response.setPrimaryContactId(primaryContact.getId());
        response.setEmail(emails);
        response.setPhoneNumbers(phoneNumbers);
    }

    private void handleExistingContact(Contact contact, ContactResponse response, List<String> emails, List<String> phoneNumbers, List<Integer> secondaryContactIds) {
        Contact existingContact = contactRepository.findByEmailOrPhoneNumber(contact.getEmail(), contact.getPhoneNumber()).get(0);
        if (existingContact.getLinkedPrecedence().equals(Precedence.SECONDARY)) {
            existingContact = contactRepository.findById(existingContact.getLinkedId()).orElseThrow(() -> new ResourceNotFoundException("Primary Contact not found"));
        }
        if (contactRepository.checkForEmailAndPhoneNumber(contact.getEmail(), contact.getPhoneNumber())) {
            linkSecondaryContact(contact);
        } else if (contact.getEmail() != null && contact.getPhoneNumber() != null) {
            contact.setLinkedId(existingContact.getId());
            contact.setLinkedPrecedence(Precedence.SECONDARY);
            contactRepository.save(contact);
        }

        sendResponse(response, existingContact, emails, phoneNumbers, secondaryContactIds, contact);
    }

    private void linkSecondaryContact(Contact contact) {
        Contact emailContact = contactRepository.findByEmail(contact.getEmail());
        Contact phoneContact = contactRepository.findByPhoneNumber(contact.getPhoneNumber());

        phoneContact.setLinkedPrecedence(Precedence.SECONDARY);
        phoneContact.setLinkedId(emailContact.getId());
        contactRepository.save(phoneContact);
    }

    private void sendResponse(ContactResponse response, Contact existingContact, List<String> emails, List<String> phoneNumbers, List<Integer> secondaryContactIds, Contact contact) {
        Contact primaryContact = getPrimaryContact(existingContact);
        assert primaryContact != null;
        emails.add(primaryContact.getEmail());
        phoneNumbers.add(primaryContact.getPhoneNumber());

        List<Contact> secondaryContacts = contactRepository.findByLinkedId(primaryContact.getId());
        for (Contact secondaryContact : secondaryContacts) {
            if (!emails.contains(secondaryContact.getEmail())) emails.add(secondaryContact.getEmail());
            if (!phoneNumbers.contains(secondaryContact.getPhoneNumber())) phoneNumbers.add(secondaryContact.getPhoneNumber());
            secondaryContactIds.add(secondaryContact.getId());
        }

        response.setPrimaryContactId(primaryContact.getId());
        response.setEmail(emails);
        response.setSecondaryId(secondaryContactIds);
        response.setPhoneNumbers(phoneNumbers);
    }

    private Contact getPrimaryContact(Contact existingContact) {
        if (existingContact.getLinkedPrecedence() == Precedence.PRIMARY) {
            return existingContact;
        } else if (existingContact.getLinkedPrecedence() == Precedence.SECONDARY) {
            return contactRepository.findById(existingContact.getLinkedId()).orElseThrow(() -> new IllegalStateException("Primary contact not found"));
        }
        return null;
    }
}
