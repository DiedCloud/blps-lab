package com.example.blps.dao.controller;

import com.example.blps.dao.controller.model.NewCommentDTO;
import com.example.blps.entity.Comment;
import com.example.blps.service.CommentService;
import com.example.blps.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
@AllArgsConstructor
public class CommentController {
    CommentService commentService;
    UserService userService;

    @CrossOrigin
    @PostMapping("/new")
    public ResponseEntity<?> login(@RequestBody final NewCommentDTO request) {
        if (!commentService.validateComment(request.getText()))
            return ResponseEntity.badRequest().body("Comment contains banned pattern");

        Comment res = commentService.createComment(userService.getCurrentUser(), request.getText());
        return ResponseEntity.ok(res.getId());
    }
}
