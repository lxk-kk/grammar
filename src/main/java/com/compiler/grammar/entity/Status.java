package com.compiler.grammar.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author 10652
 */
@Data
public class Status {
    HashMap<String, HashSet<Integer>> nextWord;
    public Status() {
        nextWord = new HashMap<>(1);
    }
}
