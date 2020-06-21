package com.compiler.grammar.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author 10652
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result implements Serializable {
    private static final long serialVersionUID = -6012743301475968480L;
    Integer code;
    String msg;
    Object body;

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
        this.body = null;
    }
}
