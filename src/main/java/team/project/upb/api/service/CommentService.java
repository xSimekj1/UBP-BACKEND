package team.project.upb.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.project.upb.api.model.Comment;
import team.project.upb.api.model.CommentDTO;
import team.project.upb.api.repository.CommentRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    public List<CommentDTO> findByFileMetadataId(Long id) {
        List<CommentDTO> commentDTOList = new ArrayList<>();
        List<Comment> commentList = commentRepository.findAllByFileMetadataId(id);

        for (Comment comment: commentList) {
            CommentDTO commentDTO = new CommentDTO();
            commentDTO.setCommentedBy(comment.getCommentedBy());
            commentDTO.setContent(comment.getContent());
            commentDTOList.add(commentDTO);
        }

        return commentDTOList;
    }
}
