package com.finalproject.coordi.exception.board;

import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;

//댓글이 존재하지 않을 때 사용하는 예외
public class BoardCommentNotFoundException extends BusinessException {
    public BoardCommentNotFoundException() {
        super(ErrorCode.COMMENT_NOT_FOUND);
    }
}