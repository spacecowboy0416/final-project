package com.finalproject.coordi.exception.board;

import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;

//본인 글/댓글이 아닌데 수정, 삭제하려고 할 때 사용하는 예외
public class BoardForbiddenException extends BusinessException {
    public BoardForbiddenException() {
        super(ErrorCode.BOARD_FORBIDDEN);
    }
}