package com.jpa.example.data.repository;

import com.jpa.example.data.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

//Declaration of repository for comments
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("FROM Comment where note_id = ?1")
    Collection<Comment> queryComments(long id);

}
