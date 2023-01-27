package com.jpa.example.starter;


import com.jpa.template.config.common.ITransactionInterceptionManagement;
import com.jpa.example.data.model.Comment;
import com.jpa.example.data.model.Note;
import com.jpa.example.data.services.abstrat.INoteService;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.LocalDate;

@SpringBootApplication()
@PropertySource({"classpath:application.properties"})
@EnableJpaRepositories("${data.repositories}")
//initially application points on to package where
//path of  definitions starts

@EntityScan(basePackages = {"com.jpa.example.data.model"})
@ComponentScan({"com.jpa.template.config.common","com.jpa.example.data.services","com.jpa.entity_manage","com.jpa.example.controller"})
public class JPAConfigStarter {
    static ApplicationContext theContext;


    static public void main(String[] args) {
        SpringApplication.run(JPAConfigStarter.class);
    }


    @Bean
    public ApplicationContextAware onApplicationContext() {
        return new ApplicationContextAware() {
            @Override
            public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
                theContext = applicationContext;
                ITransactionInterceptionManagement im = theContext.getBean(ITransactionInterceptionManagement.class);
                INoteService service = theContext.getBean(INoteService.class);
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
                service.add(note);
                service.addComment(note, comment);
                Object d=service.find("simonCanJer");
                String[] conclude= new String[0];
                im.activateSessionCloseInterceptor((em)->{conclude[0]="entity manager closed";});
                note =service.find("simonCanJer");


            }
        };

    }

}
