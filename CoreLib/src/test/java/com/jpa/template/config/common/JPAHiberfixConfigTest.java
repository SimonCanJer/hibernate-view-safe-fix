package com.jpa.template.config.common;

import data.ConfigTest;
import data.model.Note;
import data.services.abstraction.INoteService;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import static org.junit.jupiter.api.Assertions.assertNotNull;
///import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = {JPAHiberfixConfig.class, ConfigTest.class})
class JPAHiberfixConfigTest {
    @Autowired
    INoteService noteService;

    @Autowired
    EntityManagerFactory factory;


    @Autowired
    ITransactionInterceptionManagement interception;

    /**
     * This is a test for preventing LazyInitializationException by means of
     * decorating entity manager and posponing its close upon operations are executed
     */
    @Test
    public void test(){

        EntityManager[] consumed= new EntityManager[1];
        interception.activateSessionCloseInterceptor((em->{consumed[0]=em;}));
        Note note= noteService.find("simonCanJer");
        note.getComments().size();
        interception.finalizeInterceptedPostponedOperations();
        assertNotNull(consumed[0]);

    }


    @Test
    public void testNotPosponed(){

        Assertions.assertThrows(LazyInitializationException.class,()-> {
            Note note = noteService.find("simonCanJer");
            note.getComments().size();
        });
    }
    @Test
    public void testTransactions(){

        interception.activateSessionCloseInterceptor((em->{}));
        Note note= noteService.find("simonCanJer");
        note.getComments().size();
        interception.finalizeInterceptedPostponedOperations();
        note.setDescription("changed description");
        EntityTransaction[] consumed= new EntityTransaction[1];
        interception.setTransactionEndHandler((et)->{consumed[0]=et;},(et,e)->{});
        noteService.add(note);
    }
}