package com.compiler.grammar.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 10652
 */

@Data
public class Sensitive implements Serializable {
    private static final long serialVersionUID = -8424339853971532260L;
    String sensitive;
    String sentence;
}
