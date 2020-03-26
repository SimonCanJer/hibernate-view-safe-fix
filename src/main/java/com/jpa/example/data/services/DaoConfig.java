package com.jpa.example.data.services;

import com.jpa.example.data.services.abstrat.INoteService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@Configuration
public class DaoConfig {
    @Bean
    public INoteService notesDao()
    {
        return new NoteAccessService();
    }
}
