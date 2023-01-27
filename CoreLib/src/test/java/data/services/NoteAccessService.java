package data.services;


import data.model.Comment;
import data.model.Note;
import data.repository.CommentRepository;
import data.repository.NoteRepository;
import data.services.abstraction.INoteService;
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
