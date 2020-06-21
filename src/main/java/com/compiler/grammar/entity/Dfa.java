package com.compiler.grammar.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 10652
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Dfa {
    private String k;
    private String vt;
    private String m;
    private String s;
    private String z;

    public Dfa setterK(String k) {
        this.k = k;
        return this;
    }

    public Dfa setterVt(String vt) {
        this.vt = vt;
        return this;
    }

    public Dfa setterM(String m) {
        this.m = m;
        return this;
    }

    public Dfa setterS(String s) {
        this.s = s;
        return this;
    }

    public Dfa setterZ(String z) {
        this.z = z;
        return this;
    }
}
