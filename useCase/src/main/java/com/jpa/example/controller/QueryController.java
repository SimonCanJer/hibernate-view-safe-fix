package com.jpa.example.controller;

import com.jpa.example.data.model.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.jpa.example.data.services.abstrat.INoteService;
import javax.annotation.PostConstruct;
import com.jpa.template.config.common.ITransactionInterceptionManagement;
@RestController
@RequestMapping(path="/posts")
public class QueryController {
    @PostConstruct
    void init()
    {
        System.out.println("controller");
    }
    @Autowired
    INoteService noteService;
    @Autowired
    ITransactionInterceptionManagement interceptionManagement;
    @RequestMapping(path="/note",method = RequestMethod.GET)
    String  getNote(@RequestParam(name="user") String id)
    {
        String [] conclude=new String[]{"em open"};
        interceptionManagement.activateSessionCloseInterceptor((em)->{conclude[0]="entity manager closed";});
        Note note = noteService.find(id);
        String str=note.toString();
        return str+" "+conclude[0];

    }

}
