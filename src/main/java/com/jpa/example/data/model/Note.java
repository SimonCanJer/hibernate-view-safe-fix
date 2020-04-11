package com.jpa.example.data.model;

import com.google.gson.Gson;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "note",uniqueConstraints = {@UniqueConstraint(columnNames="id"),@UniqueConstraint(columnNames = "userId")})

public class Note {


    public void setId(long id) {
        this.id = id;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(unique=true,nullable = false)
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    String userId;

    String title;

    String type;
    LocalDate deadLine;
    LocalDate when;
    String  description;

    @OneToMany(mappedBy = "about",fetch = FetchType.LAZY)
    public Set<Comment> getComments() {
        return comments;
    }

    Set<Comment> comments;
    public Note()
    {

    }
    public void addComment(Comment comment )
    {
        if(comments==null)
        {
            comments = new HashSet<>();
            comment.setAbout(this);
            comments.add(comment);
        }
    }
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
    @Column(name="type", unique = false,updatable = false)
    public String getType() {
        return type;
    }

    public LocalDate getDeadLine() {
        return deadLine;
    }

    public LocalDate getWhen() {
        return when;
    }

    public String getDescription() {
        return description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDeadLine(LocalDate deadLine) {
        this.deadLine = deadLine;
    }

    public void setWhen(LocalDate when) {
        this.when = when;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    @Override
    public String toString()
    {
        Map<String,String> map = new HashMap<>();
        map.put("user", userId);
        map.put("type",type);
        map.put("description",description);
        map.put("comments",String.valueOf(comments));
        return new Gson().toJson(map);


    }


}
