package data.services;


import data.services.abstraction.INoteService;
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
