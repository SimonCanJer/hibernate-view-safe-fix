package data.services.abstraction;


import data.model.Comment;
import data.model.Note;

import java.util.List;

public interface INoteService {

    Note find(Long id);
    Note find(String userId);
    List<Note> all();
    List<Note> queryByType(String t);
    void add(Note note);
    void addComment(Note note, Comment comment);
}
