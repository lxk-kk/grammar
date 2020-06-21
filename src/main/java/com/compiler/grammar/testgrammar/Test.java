package com.compiler.grammar.testgrammar;

import com.compiler.grammar.constant.Type;
import com.compiler.grammar.entity.Result;
import com.compiler.grammar.util.GTUtil;
import com.compiler.grammar.util.LREUtil;
import com.compiler.grammar.util.REUtil;
import com.compiler.grammar.util.RREUtil;

import java.util.*;

/**
 * @author 10652
 */
public class Test {
    public static void main(String[] args) {
/*        String nonTerminal = "A,B,S";
        String terminal = "0,1";
        String symbol = "S";
        String grammar = "S ::= 0A|1A |0B |1B\nA ::= 0\nB : : =1";
        Integer type = Type.GR_TYPE;*/

/*        String nonTerminal = "<标识符>";
        String terminal = "字母,数字";
        String symbol = "<标识符>";
        String grammar = "<标识符> ::= 字母|<标识符>字母 |<标识符>数字";
        Integer type = Type.GL_TYPE;*/

/*        String nonTerminal = "C,B,S";
        String terminal = "a,b";
        String symbol = "S";
        String grammar = "S ::= aS|aB\nB ::= bC\nC : : =aC|a";
        Integer type = Type.GR_TYPE;*/

/*        String nonTerminal = "A,B,S";
        String terminal = "0,1";
        String symbol = "S";
        String grammar = "A::=0|1\n" +
                "B::=0|1\n" +
                "S::=A0|B1";
        Integer type = Type.GL_TYPE;*/

        String nonTerminal = "A,B,C,S";
        String terminal = "0,1,2";
        String symbol = "S";
        String grammar = "A::=B2|0|1|C1\n" +
                "B::=A0|1|2|C1\n" +
                "S::=A0|A1|B0|B2|C1|C2\n" +
                "C::=A2|B1|0|2";
        Integer type = Type.GL_TYPE;

/*        String nonTerminal = "1,2,3";
        String terminal = "a,b";
        String symbol = "3";
        String grammar =
                "1::=a" +"\n"+
                "2::=b" +"\n"+
                        "2::=1b" +"\n"+
                        "1::=2a" +"\n"+
                        "3::=1a" +"\n"+
                        "3::=2b" +"\n"+
                        "3::=3a" +"\n"+
                        "3::=3b";
        Integer type = Type.GL_TYPE;*/

/*        String nonTerminal = "A,B,S";
        String terminal = "0,1";
        String symbol = "S";
        String grammar = "S ::= 1|0|0A|1B\nA ::= 1|0B\nB ::= 0|1A";
        Integer type = Type.GR_TYPE;*/
/*        String nonTerminal = "A,B,C,S";
        String terminal = "0,1,2";
        String symbol = "S";
        String grammar = "S::=0A|1A|1B|2B|2C|0C\n" +
                "A::=1|0|0B|2C\n" +
                "B::=2|0|2A|1C\n" +
                "C::=1|2|1A|1B";
        Integer type = Type.GR_TYPE;*/

        // ----------------------------------------------------------
        Result nonResult = GTUtil.divideNonTerminalSymbol(nonTerminal);
        Result terResult = GTUtil.divideTerminalSymbol(terminal);
        Result graResult = GTUtil.divideGrammar(grammar);
        if (nonResult.getCode() == 0 || terResult.getCode() == 0 || graResult.getCode() == 0) {
            System.out.print("nonTerminal:" + nonResult.getMsg() + "：");
            System.out.println(nonResult.getBody());
            System.out.print("terminal:" + terResult.getMsg() + "：");
            System.out.println(terResult.getBody());
            System.out.print("grammar:" + graResult.getMsg() + "：");
            System.out.println(graResult.getBody());
            return;
        }
        HashSet<String> nonSet = null;
        HashSet<String> terSet = null;
        HashMap<String, List<String>> graMap = null;
        try {
            nonSet = (HashSet<String>) nonResult.getBody();
            terSet = (HashSet<String>) terResult.getBody();
            graMap = (HashMap<String, List<String>>) graResult.getBody();

            System.out.print("nonTerminal：");
            System.out.println(nonSet);

            System.out.print("terminal：");
            System.out.println(terSet);

            System.out.print("grammar：");
            System.out.println(graMap);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("nonTerminal：" + nonResult.getBody());
            System.out.println("terminal：" + terResult.getBody());
            System.out.println("grammar：" + graResult.getBody());
            return;
        }


        // ----------------------------------------------------------
        Result regularGra = GTUtil.isRegularGrammar(graMap, terSet, nonSet, type, symbol);
        if (regularGra.getCode() == 0) {
            System.out.print("正规文法检测：");
            System.out.println(regularGra.getMsg());
            return;
        }
        HashMap<String, List<List<String>>> regularMap = null;
        try {
            regularMap = (HashMap<String, List<List<String>>>) regularGra.getBody();

            System.out.print("【正规文法】：");
            System.out.println(regularMap);

        } catch (Exception e) {
            System.out.print("【错误】正规文法转换：");
            System.out.println(regularGra.getBody());
            return;
        }

        // ----------------------------------------------------------
        Result syntaxTreeResult = null;
        if (type.equals(Type.GL_TYPE)) {
            syntaxTreeResult = GTUtil.buildSyntaxTreeByLeft(symbol, regularMap, terSet, nonSet);
        } else if (type.equals(Type.GR_TYPE)) {
            syntaxTreeResult = GTUtil.buildSyntaxTreeByRight(symbol, regularMap, terSet, nonSet);
        }
        if (syntaxTreeResult.getCode() == 0) {
            System.out.print("【语法树构造】：");
            System.out.println(syntaxTreeResult.getMsg());
            return;
        }
        HashMap<String, HashMap<String, List<String>>> syntaxTree = null;
        try {
            syntaxTree = (HashMap<String, HashMap<String, List<String>>>) syntaxTreeResult.getBody();

            System.out.print("【语法树】：");
            System.out.println(syntaxTree);

        } catch (Exception e) {
            System.out.print("【错误】语法树转换：");
            System.out.println(syntaxTreeResult.getBody());
            return;
        }

        // ----------------------------------------------------------
        Result grammarResult = null;
        if (type.equals(Type.GL_TYPE)) {
            // 左转右
            grammarResult = GTUtil.buildRightBySyntaxTree(syntaxTree, symbol);

        } else if (type.equals(Type.GR_TYPE)) {
            // 右转左
            grammarResult = GTUtil.buildLeftBySyntaxTree(syntaxTree, symbol);
        }
        if (grammarResult.getCode() == 0) {
            System.out.print("【语法树构建文法】：");
            System.out.println(grammarResult.getMsg());
            return;
        }
        HashMap<String, List<String>> regularGrammar = null;
        try {
            regularGrammar = (HashMap<String, List<String>>) grammarResult.getBody();
            System.out.print("【左/右线性文法】：");
            System.out.println(regularGrammar);
        } catch (Exception e) {
            System.out.print("【错误】语法树构建左/右线性文法：");
            System.out.println(grammarResult.getBody());
            return;
        }

        // ----------------------------------------------------------
        String result = GTUtil.toProductionString(regularGrammar);
        if (type.equals(Type.GL_TYPE)) {
            System.out.print("最终结果：左->右线性文法：");
        } else if (type.equals(Type.GR_TYPE)) {
            System.out.print("最终结果：右->左线性文法：");
        }
        System.out.println(result);


        // ----------------------------------------------------------
        Result resultDfa = GTUtil.buildDFA(syntaxTree, terSet, symbol, type);
        System.out.print("DFA：");
        System.out.println(resultDfa);

        // ----------------------------------------------------------

        if (type.equals(Type.GL_TYPE)) {
            REUtil reUtil = new LREUtil();
            Result regularExp = reUtil.regularExpression(regularMap, symbol);
            System.out.print("左线性文法正规式：");
            System.out.println(regularExp.getBody());
        }else if (type.equals(Type.GR_TYPE)) {
            REUtil reUtil = new RREUtil();
            Result regularExp = reUtil.regularExpression(regularMap, symbol);
            System.out.print("右线性文法正规式：");
            System.out.println(regularExp.getBody());
        }
        // ----------------------------------------------------------
        String words = "";
    }
}
