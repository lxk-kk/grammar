package com.compiler.grammar.service;

import com.compiler.grammar.constant.GrammarConstant;
import com.compiler.grammar.constant.Msg;
import com.compiler.grammar.constant.Type;
import com.compiler.grammar.entity.*;
import com.compiler.grammar.util.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author 10652
 */
@Service
public class GrammarService {

    public Result transformGrammar(RegularGrammar regularGrammar) {
        // 0. 获取参数
        String nonTerminal = regularGrammar.getNonTerminalSym();
        String terminal = regularGrammar.getTerminalSym();
        String symbol = regularGrammar.getSymbol();
        String grammar = regularGrammar.getProduction();
        Integer type = regularGrammar.getType();

        // 1. 切分字符串：创建 终结符、非终结符、产生式
        Result nonResult = GTUtil.divideNonTerminalSymbol(nonTerminal);
        if (nonResult.getCode() == 0) {
            return nonResult;
        }
        Result terResult = GTUtil.divideTerminalSymbol(terminal);
        if (terResult.getCode() == 0) {
            return terResult;
        }
        Result graResult = GTUtil.divideGrammar(grammar);
        if (graResult.getCode() == 0) {
            return graResult;
        }
        HashSet<String> nonSet = null;
        HashSet<String> terSet = null;
        HashMap<String, List<String>> graMap = null;
        try {
            nonSet = (HashSet<String>) nonResult.getBody();
            terSet = (HashSet<String>) terResult.getBody();
            graMap = (HashMap<String, List<String>>) graResult.getBody();
        } catch (Exception e) {
            return new Result(0, Msg.ERROR_REGULAR_GRAMMAR);
        }

        // 2. 判断是否是正规文法
        Result regularGra = GTUtil.isRegularGrammar(graMap, terSet, nonSet, type, symbol);
        if (regularGra.getCode() == 0) {
            return regularGra;
        }
        HashMap<String, List<List<String>>> regularMap = null;
        try {
            regularMap = (HashMap<String, List<List<String>>>) regularGra.getBody();
        } catch (Exception e) {
            return new Result(0, Msg.ERROR_REGULAR_GRAMMAR);
        }

        // 3. 构造语法树
        Result syntaxTreeResult = null;
        if (type.equals(Type.GL_TYPE)) {
            syntaxTreeResult = GTUtil.buildSyntaxTreeByLeft(symbol, regularMap, terSet, nonSet);
        } else if (type.equals(Type.GR_TYPE)) {
            syntaxTreeResult = GTUtil.buildSyntaxTreeByRight(symbol, regularMap, terSet, nonSet);
        }
        if (syntaxTreeResult.getCode() == 0) {
            return syntaxTreeResult;
        }
        HashMap<String, HashMap<String, List<String>>> syntaxTree = null;
        try {
            syntaxTree = (HashMap<String, HashMap<String, List<String>>>) syntaxTreeResult.getBody();
        } catch (Exception e) {
            return new Result(0, Msg.ERROR_REGULAR_GRAMMAR);
        }

        // 4. 根据语法树转换文法
        Result grammarResult = null;
        if (type.equals(Type.GL_TYPE)) {
            // 左转右
            grammarResult = GTUtil.buildRightBySyntaxTree(syntaxTree, symbol);
        } else if (type.equals(Type.GR_TYPE)) {
            // 右转左
            grammarResult = GTUtil.buildLeftBySyntaxTree(syntaxTree, symbol);
        }
        if (grammarResult.getCode() == 0) {
            return grammarResult;
        }
        HashMap<String, List<String>> rg = null;
        try {
            rg = (HashMap<String, List<String>>) grammarResult.getBody();
        } catch (Exception e) {
            return new Result(0, Msg.ERROR_REGULAR_GRAMMAR);
        }

        // 5. 得到结果
        String tProduction = GTUtil.toProductionString(rg);
        if (rg.containsKey(GrammarConstant.FINAL_SYMBOL)) {
            nonSet.add(GrammarConstant.FINAL_SYMBOL);
        }
        if (rg.containsKey(GrammarConstant.INIT_SYMBOL)) {
            nonSet.add(GrammarConstant.INIT_SYMBOL);
        }
        String tNonTerminal = String.join(",", nonSet);
        String tTerminal = terminal;
        Integer tType = type.equals(Type.GR_TYPE) ? Type.GL_TYPE : Type.GR_TYPE;

        // 6. 生成 正规文法
        RegularGrammarVO tRegularGrammar = new RegularGrammarVO(tType, tTerminal, tNonTerminal, tProduction, symbol);
        // 7. 生成 DFA
        Result dfa = GTUtil.buildDFA(syntaxTree, terSet, symbol, type);
        // 8. 生成表达式
        Result expression;
        if (type.equals(Type.GR_TYPE)) {
            REUtil reUtil = new RREUtil();
            expression = reUtil.regularExpression(regularMap, symbol);
        } else {
            REUtil reUtil = new LREUtil();
            expression = reUtil.regularExpression(regularMap, symbol);
        }
        HashMap<String, Object> result = new HashMap<>(2);
        result.put("regularGrammar", new Result(1, Msg.SUCCESS, tRegularGrammar));
        result.put("dfa", dfa);
        result.put("expression", expression);
        return new Result(1, Msg.SUCCESS, result);
    }

    public Result filterSensitiveWord(Sensitive sensitive) {
        Result result = SFUtil.enterSensitiveWord(sensitive.getSensitive());
        if (result.getCode().equals(0)) {
            return result;
        }
        HashMap<String, Status> sensitiveMap = (HashMap<String, Status>) result.getBody();
        return SFUtil.filterSensitive(sensitive.getSentence(), sensitiveMap);
    }

}
