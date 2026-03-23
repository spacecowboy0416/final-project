package com.finalproject.coordi.exception.board;

import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;

//이미 삭제된 게시글을 다시 삭제하려고 할 때 사용하는 예외
public class BoardPostAlreadyDeletedException extends BusinessException {
    public BoardPostAlreadyDeletedException() {
        super(ErrorCode.POST_ALREADY_DELETED);
    }
}