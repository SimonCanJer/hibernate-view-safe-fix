package com.jpa.example.data.model;

import com.google.gson.Gson;
import com.jpa.example.data.model.Note;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="comment")
@Access(AccessType.FIELD)
public class Comment {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setContent(String content) {
        this.content = content;
    }

    LocalDate date;
    String content;
    @ManyToOne
    @JoinColumn(name="note_id")
    @Fetch(FetchMode.SELECT)
    Note about;

    public long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }

    public Note getAbout() {
        return about;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAbout(Note about) {
        this.about = about;
    }
    @Override
    public String toString()
    {
        Note n= about;
        about=null;
        Gson gs= new Gson();
        try
        {
            return gs.toJson(n);
        }
        finally
        {
            about = n;
        }


    }

}
