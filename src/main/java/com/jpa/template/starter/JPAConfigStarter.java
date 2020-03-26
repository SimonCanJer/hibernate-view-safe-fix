package com.jpa.template.starter;


import com.google.gson.Gson;
import com.jpa.template.config.common.ITransactionInterceptionManagement;
import com.jpa.example.data.model.Comment;
import com.jpa.example.data.model.Note;
import com.jpa.example.data.services.abstrat.INoteService;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.LocalDate;
import java.util.Set;

@SpringBootApplication()
@PropertySource({"classpath:application.properties"})
@EnableJpaRepositories
//initially application points on to package where
//path of  definitions starts
@ComponentScan({"com.jpa.template.config.common","com.jpa.example.controller"})
public class JPAConfigStarter {
    static ApplicationContext theContext;


    static public void main(String[] args) {
        //BasicDataSource bs= new BasicDataSource();

        //if (additional()) return;
        //new SpringApplicationBuilder(JPAConfigStarter.class).web(WebApplicationType.NONE).run(args);

        SpringApplication.run(JPAConfigStarter.class);

    }


    @Bean
    public ApplicationContextAware onApplicationContext() {
        return new ApplicationContextAware() {
            @Override
            public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
                theContext = applicationContext;
                ITransactionInterceptionManagement im=theContext.getBean(ITransactionInterceptionManagement.class);
                INoteService service = theContext.getBean(INoteService.class);
                LocalDate ld= LocalDate.now();
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
                service.addComment(note,comment);



             //   im.activateInterception((em)->{System.out.println("closed");});
              //  note=service.find("simonCanJer");
             //   Set<Comment> comments=note.getComments();
              //  im.finalizeInterceptedTransaction();

            }
        };

    }

}
