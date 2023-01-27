package data;

import com.jpa.template.config.common.ITransactionInterceptionManagement;
import data.model.Comment;
import data.model.Note;
import data.services.abstraction.INoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.LocalDate;

@Configuration
@ComponentScan("data.services")
@EnableJpaRepositories
@EntityScan(basePackages= {"data.model","data.repository"})
public class ConfigTest {

    @Bean
    ApplicationContextAware onContextReady(INoteService noteService){

       return new ApplicationContextAware() {
           @Override
           public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
               LocalDate ld = LocalDate.now();
               Comment comment = new Comment();
               comment.setDate(ld);
               comment.setContent("content");
               Note note = new Note();
               note.setUserId("simonCanJer");
               note.setTitle("any");
               note.setType("urgent");
               note.setDescription("do");
               note.addComment(comment);
               noteService.add(note);
               noteService.addComment(note, comment);
           }
       } ;
    }




}
