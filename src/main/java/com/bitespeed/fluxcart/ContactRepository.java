package com.bitespeed.fluxcart;

import com.bitespeed.fluxcart.Entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer> {

    List<Contact> findByEmailOrPhoneNumber(String email, String phoneNumber);


    boolean existsByEmailOrPhoneNumber(String email, String phoneNumber);

    @Query("SELECT c FROM Contact c WHERE (c.email = ?1 OR c.phoneNumber = ?2) AND c.linkedPrecedence = 'PRIMARY'")
    Contact findPrimaryContact(String email, String phoneNumber);

    List<Contact> findByLinkedId(Integer linkedId);

    @Query("SELECT (COUNT(c) > 0) " +
            "FROM Contact c " +
            "WHERE c.email = ?1 AND c.linkedPrecedence = 'PRIMARY' " +
            "AND EXISTS (" +
            "   SELECT 1 FROM Contact c2 " +
            "   WHERE c2.phoneNumber = ?2 AND c2.linkedPrecedence = 'PRIMARY'" +
            ")")
    boolean checkForEmailAndPhoneNumber(String email, String phoneNumber);

    Contact findByPhoneNumber(String phoneNumber);

    Contact findByEmail(String email);
}
