package com.jpa.example.data.services.abstrat;



import com.jpa.example.data.model.Comment;
import com.jpa.example.data.model.Note;

import java.util.List;

public interface INoteService {

    Note find(Long id);
    Note find(String userId);
    List<Note> all();
    List<Note> queryByType(String t);
    void add(Note note);
    void addComment(Note note, Comment comment);
}
