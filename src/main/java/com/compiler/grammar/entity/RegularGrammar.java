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
public class RegularGrammar implements Serializable {
    private static final long serialVersionUID = 4704783097799748646L;
    Integer type;
    String terminalSym;
    String nonTerminalSym;
    String production;
    String symbol;
}
