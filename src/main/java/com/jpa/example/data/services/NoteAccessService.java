package com.jpa.example.data.services;

import com.jpa.example.data.model.Comment;
import com.jpa.example.data.model.Note;
import com.jpa.example.data.repository.CommentRepository;
import com.jpa.example.data.repository.NoteRepository;
import com.jpa.example.data.services.abstrat.INoteService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class NoteAccessService implements INoteService {
    @Autowired
    NoteRepository repository;

    @Autowired
    CommentRepository comments;


    @Override
    public Note find(Long id) {
        return repository.findById(id).get();
    }

    @Override
    public Note find(String userId) {
        return repository.find(userId);
    }

    @Override
    public List<Note> all() {
        return repository.findAll();
    }

    @Override
    public List<Note> queryByType(String t) {
        return repository.findByType(t);
    }

    @Override
    public void add(Note note) {
        repository.save(note);
    }

    @Override
    public void addComment(Note note, Comment comment) {
        comment.setAbout(note);
        comments.save(comment);

    }
}
