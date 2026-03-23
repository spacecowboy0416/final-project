package com.finalproject.coordi.exception.board;

import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;

//게시글이 존재하지 않을 때 사용하는 예외
public class BoardPostNotFoundException extends BusinessException {
    public BoardPostNotFoundException() {
        super(ErrorCode.POST_NOT_FOUND);
    }
}