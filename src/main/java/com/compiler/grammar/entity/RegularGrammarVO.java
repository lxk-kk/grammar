package com.compiler.grammar.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * @author 10652
 */
@Data
@NoArgsConstructor
public class RegularGrammarVO implements Serializable {
    private static final long serialVersionUID = -4618498655762594249L;
    Integer type;
    String terminalSym;
    String nonTerminalSym;
    List<String> production;
    String symbol;

    public RegularGrammarVO(Integer type, String terminalSym, String nonTerminalSym, String production, String
            symbol) {
        this.type = type;
        this.terminalSym = "{" + terminalSym + "}";
        this.nonTerminalSym = "{" + nonTerminalSym + "}";
        this.production = Arrays.asList(production.split("\n"));
        this.symbol = symbol;
    }
}
