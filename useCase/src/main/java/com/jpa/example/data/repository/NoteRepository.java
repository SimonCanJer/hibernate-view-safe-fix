package com.jpa.example.data.repository;

import com.jpa.example.data.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository  extends JpaRepository<Note,Long> {

    @Query("FROM Note where type = ?1")
    List<Note> findByType(String sID);
    @Query("FROM Note where userId = ?1")
    Note find(String userId);

}
