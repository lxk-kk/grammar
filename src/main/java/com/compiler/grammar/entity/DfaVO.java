package com.compiler.grammar.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * @author 10652
 */
@Data
@NoArgsConstructor
public class DfaVO {
    private String k;
    private String vt;
    private List<String> m;
    private String s;
    private String z;

    public DfaVO(Dfa dfa) {
        this.k = "{" + dfa.getK() + "}";
        this.vt = "{" + dfa.getVt() + "}";
        this.m = Arrays.asList(dfa.getM().split("\n"));
        this.s = dfa.getS();
        this.z = "{" + dfa.getZ() + "}";
    }
}
