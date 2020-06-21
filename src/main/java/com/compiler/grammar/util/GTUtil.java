package com.compiler.grammar.util;

import com.compiler.grammar.constant.GrammarConstant;
import com.compiler.grammar.constant.Msg;
import com.compiler.grammar.constant.Type;
import com.compiler.grammar.entity.Dfa;
import com.compiler.grammar.entity.DfaVO;
import com.compiler.grammar.entity.Result;

import java.util.*;


/**
 * 左线性文法：
 * U ::= a
 * U ::= Aa
 * <p>
 * 右线性文法：
 * U ::= a
 * U ::= aA
 *
 * @author 10652
 */
public class GTUtil {

    /**
     * 构造产生式
     *
     * @param grammarStr 产生式字符串
     * @return 产生式
     */
    public static Result divideGrammar(String grammarStr) {
        if ((grammarStr = grammarStr.trim()).length() == 0) {
            System.out.println("[divideGrammar]：1");

            return new Result(0, Msg.ERROR_PRODUCTION);
        }
        String[] grammars = grammarStr.split("\n");
        if (grammars.length <= 0) {
            System.out.println("[divideGrammar]：2");

            return new Result(0, Msg.ERROR_PRODUCTION);
        }
        HashMap<String, List<String>> grammarMap = new HashMap<>(grammars.length);

        int count = 0;
        for (String grammar : grammars) {
            String[] letters = grammar.replaceAll(" ", "").split("::=");
            if (letters.length <= 1) {
                System.out.println("ERROR --- [divideGrammar]：3");
                return new Result(0, Msg.ERROR_PRODUCTION);
            }
            count += (letters.length - 1);
            String[] letterArr = letters[1].split("\\|");
            List<String> tempList = new ArrayList<>();
            tempList.addAll(Arrays.asList(letterArr));
            if (grammarMap.containsKey(letters[0])) {
                grammarMap.get(letters[0]).addAll(tempList);
            } else {
                grammarMap.put(letters[0], tempList);
            }
        }
        if (count < 0) {
            System.out.println("[divideGrammar]：4");
            return new Result(0, Msg.LOST_PRODUCTION);
        }
        return new Result(1, Msg.SUCCESS, grammarMap);
    }

    /**
     * 构造终结符
     *
     * @param letterSymbol 终结符字符串
     * @return 终结符集
     */
    public static Result divideTerminalSymbol(String letterSymbol) {
        return divideSymbol(letterSymbol);
    }

    /**
     * 构造非终结符
     *
     * @param letterSymbol 非终结符字符串
     * @return 非终结符集
     */
    public static Result divideNonTerminalSymbol(String letterSymbol) {
        return divideSymbol(letterSymbol);
    }

    /**
     * 分割字符串（以英文逗号分割）
     *
     * @param symbol 字符串
     * @return 字汇表
     */
    private static Result divideSymbol(String symbol) {
        HashSet<String> symbolSet = new HashSet<>();
        if (Objects.isNull(symbol) || (symbol = symbol.trim()).length() == 0) {
            return new Result(0, Msg.ERROR_SYMBOL);
        }
        String[] symbols = symbol.split(",");
        for (String sym : symbols) {
            if (sym.length() > 0 && !symbolSet.contains(sym)) {
                symbolSet.add(sym);
            }
        }
        return new Result(1, Msg.SUCCESS, symbolSet);
    }

    /**
     * 判断文法类型
     *
     * @param grammarMap 产生式
     * @return 文法判断
     */
    public static Result isRegularGrammar(HashMap<String, List<String>> grammarMap,
                                          HashSet<String> terminalSym,
                                          HashSet<String> nonTerminalSym,
                                          Integer expectType,
                                          String symbol) {

        Set<String> keySet = grammarMap.keySet();
        HashMap<String, List<List<String>>> productionMap = new HashMap<>(grammarMap.size());

        Boolean typeJudge = false;
        Boolean symJudge = false;

        Iterator iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (symbol.equals(key)) {
                symJudge = true;
            }
            if (!nonTerminalSym.contains(key) || terminalSym.contains(key)) {
                System.out.println("ERROR ----- [isRegularGrammar]：1");
                return new Result(0, Msg.ERROR_PRODUCTION);
            }
            List<String> productions = grammarMap.get(key);
            if (Objects.isNull(productions) || productions.isEmpty()) {
                System.out.println("ERROR ----- [isRegularGrammar]：2");
                return new Result(0, Msg.ERROR_PRODUCTION);
            }
            List<List<String>> symbolList = new ArrayList<>();
            for (String production : productions) {
                StringBuilder builder = new StringBuilder();

                List<String> letters = new ArrayList<>();
                List<Integer> sign = new ArrayList<>();
                for (int i = 0; i < production.length(); i++) {
                    builder.append(production.charAt(i));
                    String letter = builder.toString();
                    if (nonTerminalSym.contains(letter)) {
                        sign.add(GrammarConstant.VN);
                        letters.add(letter);
                        builder.delete(0, builder.length());
                    } else if (terminalSym.contains(letter)) {
                        sign.add(GrammarConstant.VT);
                        letters.add(letter);
                        builder.delete(0, builder.length());
                    }
                }
                if (builder.length() > 0 || sign.size() >= 3 || sign.size() <= 0) {
                    System.out.println("ERROR ----- [isRegularGrammar]：3");
                    return new Result(0, Msg.ERROR_REGULAR_GRAMMAR);
                }
                Integer tempType = judgeType(sign);
                if (tempType.equals(Type.ERROR_TYPE)) {
                    System.out.println("ERROR ----- [isRegularGrammar]：4");
                    return new Result(0, Msg.ERROR_REGULAR_GRAMMAR);
                }
                if (!tempType.equals(Type.REGULAR_TYPE) && !tempType.equals(expectType)) {
                    System.out.println("ERROR ----- [isRegularGrammar]：5");
                    return returnType(expectType);
                } else {
                    typeJudge = true;
                }
                symbolList.add(letters);
            }
            productionMap.put(key, symbolList);
        }
        if (!typeJudge) {
            System.out.println("ERROR ----- [isRegularGrammar]：6");
            return returnType(expectType);
        }
        if (!symJudge) {
            System.out.println("ERROR ----- [isRegularGrammar]：7");
            return new Result(0, Msg.ERROR_REGULAR_GRAMMAR);
        }
        return new Result(1, Msg.SUCCESS, productionMap);
    }

    private static Result returnType(Integer expectType) {
        String msg = expectType.equals(Type.GL_TYPE) ? Msg.LEFT_REGULAR_GRAMMAR : Msg.RIGHT_REGULAR_GRAMMAR;
        return new Result(0, msg);
    }

    private static Integer judgeType(List<Integer> typeList) {
        if (typeList.size() == 1) {
            if (typeList.get(0).equals(GrammarConstant.VN)) {
                return Type.ERROR_TYPE;
            } else {
                return Type.REGULAR_TYPE;
            }
        }
        if (typeList.get(0).equals(typeList.get(1))) {
            return Type.ERROR_TYPE;
        }
        return typeList.get(0).equals(GrammarConstant.VN) ? Type.GL_TYPE : Type.GR_TYPE;
    }

    /**
     * 根据右线性文法构建语法树：
     * 右线性文法：
     * symbol : 初始态(必须在产生式中给出)
     * U ::= a（Z） U - a -> Z
     * U ::= aA     U - a -> A
     *
     * @param symbol         标识符
     * @param productionMap  产生式 映射
     * @param terminalSym    终结符 表
     * @param nonTerminalSym 非终结符表
     * @return 语法树
     */
    public static Result buildSyntaxTreeByRight(String symbol,
                                                HashMap<String, List<List<String>>> productionMap,
                                                HashSet<String> terminalSym,
                                                HashSet<String> nonTerminalSym) {

        HashMap<String, HashMap<String, List<String>>> syntaxTree = new HashMap<>(productionMap.size());
        List<List<String>> production = productionMap.get(symbol);
        if (Objects.isNull(production)) {
            System.out.println("ERROR ----- [buildSyntaxTreeByRight]：1");
            return new Result(0, Msg.ERROR_REGULAR_GRAMMAR);
        }
        LinkedList<String> store = new LinkedList<>();
        store.addFirst(symbol);
        while (store.size() > 0) {
            String key = store.removeFirst();
            List<List<String>> values = productionMap.get(key);
            // 校验 key-value
            if (Objects.isNull(values) || values.isEmpty()) {
                System.out.println("ERROR ----- [buildSyntaxTreeByRight]：2");
                return new Result(0, Msg.ERROR_REGULAR_GRAMMAR);
            }
            HashMap<String, List<String>> branch = new HashMap<>(values.size());
            for (List<String> letters : values) {
                if (Objects.isNull(letters) || letters.size() == 0) {
                    System.out.println("ERROR ----- [buildSyntaxTreeByRight]：3");
                    return new Result(0, Msg.ERROR_REGULAR_GRAMMAR);
                }
                if (!terminalSym.contains(letters.get(0))) {
                    System.out.println("ERROR ----- [buildSyntaxTreeByRight]：4");
                    return new Result(0, Msg.ERROR_REGULAR_GRAMMAR);
                }
                if (letters.size() > 1) {
                    // S ::= aA
                    if (!nonTerminalSym.contains(letters.get(1))) {
                        System.out.println("ERROR ----- [buildSyntaxTreeByRight]：5");
                        return new Result(0, Msg.ERROR_REGULAR_GRAMMAR);
                    }

                    // A - a -> B,C
                    subBuildTree(branch, letters.get(0), letters.get(1));
                    // 避免死循环遍历
                    if (!syntaxTree.containsKey(letters.get(1))) {
                        store.addLast(letters.get(1));
                    }
                } else {
                    // S ::= a
                    subBuildTree(branch, letters.get(0), GrammarConstant.FINAL_SYMBOL);
                }
            }
            syntaxTree.put(key, branch);
        }
        return new Result(1, Msg.SUCCESS, syntaxTree);
    }


    /**
     * 根据左线性文法构建语法树：
     * 左线性文法：
     * symbol : 终态(必须在产生式中给出)
     * U ::= (S)a   S - a -> U
     * U ::= Aa     A - a -> U
     *
     * @param symbol         标识符
     * @param productionMap  产生式 映射
     * @param terminalSym    终结符 表
     * @param nonTerminalSym 非终结符表
     * @return 语法树
     */
    public static Result buildSyntaxTreeByLeft(String symbol,
                                               HashMap<String, List<List<String>>> productionMap,
                                               HashSet<String> terminalSym,
                                               HashSet<String> nonTerminalSym) {

        HashMap<String, HashMap<String, List<String>>> syntaxTree = new HashMap<>(productionMap.size());
        List<List<String>> production = productionMap.get(symbol);
        if (Objects.isNull(production)) {
            System.out.println("ERROR ----- [buildSyntaxTreeByLeft]：1");
            return new Result(0, Msg.ERROR_REGULAR_GRAMMAR);
        }
        LinkedList<String> store = new LinkedList<>();
        HashSet<String> nonTerminalSet = new HashSet<>();
        store.addFirst(symbol);
        while (store.size() > 0) {
            String key = store.removeFirst();
            nonTerminalSet.add(key);
            List<List<String>> values = productionMap.get(key);
            // 校验 key-value
            if (Objects.isNull(values) || values.isEmpty()) {
                System.out.println("ERROR ----- [buildSyntaxTreeByLeft]：2");
                return new Result(0, Msg.ERROR_REGULAR_GRAMMAR);
            }
            for (List<String> letters : values) {
                if (Objects.isNull(letters) || letters.size() == 0) {
                    System.out.println("ERROR ----- [buildSyntaxTreeByLeft]：3");
                    return new Result(0, Msg.ERROR_REGULAR_GRAMMAR);
                }
                if (letters.size() == 1) {
                    // Z ::= a
                    if (!terminalSym.contains(letters.get(0))) {
                        System.out.println("ERROR ----- [buildSyntaxTreeByLeft]：4");
                        return new Result(0, Msg.ERROR_REGULAR_GRAMMAR);
                    }
                    if (!syntaxTree.containsKey(GrammarConstant.INIT_SYMBOL)) {
                        HashMap<String, List<String>> node = new HashMap<>(1);
                        subBuildTree(node, letters.get(0), key);
                        syntaxTree.put(GrammarConstant.INIT_SYMBOL, node);
                    } else {
                        HashMap<String, List<String>> node = syntaxTree.get(GrammarConstant.INIT_SYMBOL);
                        subBuildTree(node, letters.get(0), key);
                    }
                } else {
                    //  Z ::= Ua
                    if (!nonTerminalSym.contains(letters.get(0)) || !terminalSym.contains(letters.get(1))) {
                        System.out.println("ERROR ----- [buildSyntaxTreeByLeft]：5");
                        return new Result(0, Msg.ERROR_REGULAR_GRAMMAR);
                    }
                    if (!syntaxTree.containsKey(letters.get(0))) {
                        HashMap<String, List<String>> node = new HashMap<>(1);

                        subBuildTree(node, letters.get(1), key);

                        syntaxTree.put(letters.get(0), node);
                    } else {
                        HashMap<String, List<String>> node = syntaxTree.get(letters.get(0));
                        subBuildTree(node, letters.get(1), key);
                    }
                    // 通过 set 避免死循环遍历
                    if (!nonTerminalSet.contains(letters.get(0))) {
                        store.addLast(letters.get(0));
                        nonTerminalSet.add(letters.get(0));
                    }
                }
            }
        }
        return new Result(1, Msg.SUCCESS, syntaxTree);
    }

    /**
     * 根据 语法树 构建右线性文法
     * 右线性文法：
     * symbol : 初始态(必须在产生式中给出)
     * U ::= a（Z） U - a -> Z
     * U ::= aA     U - a -> A
     *
     * @param syntaxTree 语法树
     * @param symbol     标识符
     * @return 右线性文法
     */
    public static Result buildRightBySyntaxTree(HashMap<String, HashMap<String, List<String>>> syntaxTree, String
            symbol) {
        HashMap<String, List<String>> productionMap = new HashMap<>(syntaxTree.size());
        Set<String> keySet = syntaxTree.keySet();
        Iterator iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            HashMap<String, List<String>> values = syntaxTree.get(key);
            List<String> productions = new ArrayList<>();
            Iterator ite = values.keySet().iterator();
            while (ite.hasNext()) {
                String vt = (String) ite.next();
                List<String> vnList = values.get(vt);
                for (String vn : vnList) {
                    String production;
                    if (vn.equals(symbol)) {
                        production = vt;
                    } else if (vn.equals(GrammarConstant.INIT_SYMBOL)) {
                        production = vt + symbol;
                    } else {
                        production = vt + vn;
                    }
                    productions.add(production);
                }
            }
            if (!productions.isEmpty()) {
                String productionKey;
                if (key.equals(symbol)) {
                    productionKey = GrammarConstant.FINAL_SYMBOL;
                } else if (key.equals(GrammarConstant.INIT_SYMBOL)) {
                    productionKey = symbol;
                } else {
                    productionKey = key;
                }
                if (productionMap.containsKey(productionKey)) {
                    productionMap.get(productionKey).addAll(productions);
                } else {
                    productionMap.put(productionKey, productions);
                }
            }
        }
        return new Result(1, Msg.SUCCESS, productionMap);
    }

    /**
     * 根据 语法树 构建 左线性文法
     * 左线性文法：
     * symbol : 终态(必须在产生式中给出)
     * U ::= (S)a   S - a -> U
     * U ::= Aa     A - a -> U
     *
     * @param syntaxTree 语法树
     * @param symbol     标识符
     * @return 左线性文法
     */
    public static Result buildLeftBySyntaxTree(HashMap<String, HashMap<String, List<String>>> syntaxTree, String
            symbol) {
        HashMap<String, List<String>> productionMap = new HashMap<>(syntaxTree.size());
        Set<String> keySet = syntaxTree.keySet();
        Iterator iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            HashMap<String, List<String>> values = syntaxTree.get(key);
            Iterator ite = values.keySet().iterator();

            if (key.equals(symbol)) {
                key = "";
            } else if (key.equals(GrammarConstant.FINAL_SYMBOL)) {
                key = symbol;
            }

            while (ite.hasNext()) {
                String vt = (String) ite.next();
                List<String> vnList = values.get(vt);
                for (String vn : vnList) {
                    String production = key + vt;
                    String productionKey = vn;

                    // 初始态
                    if (vn.equals(symbol)) {
                        productionKey = GrammarConstant.INIT_SYMBOL;
                    } else if (vn.equals(GrammarConstant.FINAL_SYMBOL)) {
                        productionKey = symbol;
                    }

                    if (productionMap.containsKey(productionKey)) {
                        productionMap.get(productionKey).add(production);
                    } else {
                        List<String> productions = new ArrayList<>();
                        productions.add(production);
                        productionMap.put(productionKey, productions);
                    }
                }
            }
        }
        return new Result(1, Msg.SUCCESS, productionMap);
    }

    public static String toProductionString(HashMap<String, List<String>> productionMap) {
        StringBuilder builder = new StringBuilder();
        Iterator iterator = productionMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            builder.append(key).append("::=");
            List<String> productions = productionMap.get(key);
            for (int i = 0; i < productions.size(); i++) {
                builder.append(productions.get(i));
                if (i < productions.size() - 1) {
                    builder.append("|");
                } else {
                    builder.append("\n");
                }
            }
        }
        return builder.toString();
    }

    public static Result buildDFA(HashMap<String, HashMap<String, List<String>>> syntaxTree,
                                  HashSet<String> terminalSet,
                                  String symbol,
                                  Integer type) {
        Dfa dfa = new Dfa();
        HashSet<String> kSet = new HashSet<>();
        HashSet<String> zSet = new HashSet<>();

        String s;
        String m;
        // s、zSet
        if (type.equals(Type.GR_TYPE)) {
            s = symbol;
            zSet.add(GrammarConstant.FINAL_SYMBOL);
        } else {
            s = GrammarConstant.INIT_SYMBOL;
            zSet.add(symbol);
        }
        StringBuilder builder = new StringBuilder();
        Iterator iterator = syntaxTree.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            HashMap<String, List<String>> valueMap = syntaxTree.get(key);
            // kSet
            kSet.add(key);

            if (!Objects.isNull(valueMap)) {
                Iterator iteratorVt = valueMap.keySet().iterator();
                while (iteratorVt.hasNext()) {
                    String vt = (String) iteratorVt.next();
                    // zSet
                    if (vt.equals(GrammarConstant.NON)) {
                        // z = non 的情况
                        zSet.add(s);
                    }
                    List<String> vnList = valueMap.get(vt);
                    // kSet
                    kSet.addAll(vnList);
                    for (String vn : vnList) {
                        builder.append("M( ")
                                .append(key)
                                .append(" , ")
                                .append(vt)
                                .append(" )= ")
                                .append(vn).append("\n");
                    }
                }
            }
        }
        m = builder.toString();
        String terminal = buildSet(terminalSet);
        String k = buildSet(kSet);
        String z = buildSet(zSet);
        dfa.setterK(k).setterM(m).setterS(s).setterVt(terminal).setterZ(z);
        return new Result(1, Msg.SUCCESS, new DfaVO(dfa));
    }

    private static String buildSet(HashSet<String> set) {
        StringBuilder builder = new StringBuilder();
        for (String str : set) {
            builder.append(str).append(",");
        }
        String result = builder.toString();
        return result.substring(0, result.length() - 1);
    }

    private static void subBuildTree(HashMap<String, List<String>> branch, String key, String value) {
        if (branch.containsKey(key)) {
            branch.get(key).add(value);
        } else {
            List<String> list = new ArrayList<>(1);
            list.add(value);
            branch.put(key, list);
        }
    }

}
