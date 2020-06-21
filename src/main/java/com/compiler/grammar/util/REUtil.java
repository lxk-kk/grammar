package com.compiler.grammar.util;

import com.compiler.grammar.constant.GrammarConstant;
import com.compiler.grammar.constant.Msg;
import com.compiler.grammar.entity.Result;

import java.util.*;

/**
 * @author 10652
 */
public abstract class REUtil {
    void subBuildMap(HashMap<String, String> branch, String key, String value) {
        if (branch.containsKey(key)) {
            branch.put(key, branch.get(key) + "+" + value);
        } else {
            branch.put(key, value);
        }
    }

    void subBuilder(StringBuilder builder, String... strings) {
        for (String str : strings) {
            builder.append("(").append(str).append(")");
        }
    }

    void mergeMap(
            HashMap<String, HashMap<String, String>> stiMap,
            HashMap<String, HashMap<String, String>> tempMap) {
        Iterator ite = tempMap.keySet().iterator();

        while (ite.hasNext()) {
            String key = (String) ite.next();
            HashMap<String, String> vnt;
            if (stiMap.containsKey(key)) {
                vnt = stiMap.get(key);
            } else {
                stiMap.put(key, new HashMap<>(1));
                vnt = stiMap.get(key);
            }
            HashMap<String, String> tempVnt = tempMap.get(key);
            Iterator ite1 = tempVnt.keySet().iterator();
            while (ite1.hasNext()) {
                String vn = (String) ite1.next();
                subBuildMap(vnt, vn, tempVnt.get(vn));
            }
        }
    }

    String simplifyExpression(String expression) {
        StringBuilder builder = new StringBuilder();
        LinkedList<String> stack = new LinkedList<>();
        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            if (ch == ')') {
                String str = "";
                while (stack.size() > 0 && !stack.getFirst().equals("(")) {
                    str = stack.removeFirst() + str;
                }
                if (str.contains("+") && !check(str)) {
                    str = "(" + str + ")";
                }
                if (stack.size() > 0 && stack.getFirst().equals("(")) {
                    stack.removeFirst();
                }
                stack.addFirst(str);
            } else if (ch == '*' &&  !checkX(stack.getFirst())) {
                stack.addFirst("(" + stack.removeFirst() + ")*");
            } else {
                stack.addFirst(String.valueOf(expression.charAt(i)));
            }
        }
        while (!stack.isEmpty()) {
            builder.append(stack.removeLast());
        }
        return builder.toString().replace('+', '|');
    }

    private boolean checkX(String string) {
        if (string.charAt(0) != '(' || string.charAt(string.length() - 1) != ')') {
            return false;
        }
        int count = 1;
        for (int i=1;i<string.length();++i) {
            if (string.charAt(i) == '(') {
                count++;
            } else if (string.charAt(i) == ')') {
                count--;
                if (count == 0 && i < string.length() - 1) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean check(String string) {
        if (!string.contains("(") && string.contains("+")) {
            return false;
        }
        if (string.indexOf("(") > string.indexOf("+")
                || string.lastIndexOf(")") < string.lastIndexOf("+")) {
            return false;
        }
        int count = 0;
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (ch == '(') {
                count += 1;
            } else if (ch == ')') {
                count -= 1;
            } else if (ch == '+' && count == 0) {
                return false;
            }
        }
        return true;
    }

    public Result regularExpression(HashMap<String, List<List<String>>> regularGram, String symbol) {
        HashMap<String, String> kvMap = new HashMap<>(1);
        HashMap<String, HashMap<String, String>> stiMap = new HashMap<>(1);
        // 1. Map<Str,Map<Str,List>> 合并同类项，生成 stiMap 和 kvMap
        boolean goOn = mergeSameItem(regularGram, kvMap, stiMap);
        if (!goOn) {
            return new Result(0, Msg.CANNOT_GET_EXPRESSION);
        }
        do {
            // 2. 寻找解：X=Xa+b => X=ba*
            rebuildKvMap(kvMap, stiMap);
            if (kvMap.containsKey(symbol)) {
                break;
            }
            // 3. 替换公因式 B=Aa A=c
            replaceExpression(kvMap, stiMap);
            if (kvMap.containsKey(symbol)) {
                break;
            }
        } while (true);
        String regularExpression = symbol + "->" + simplifyExpression(kvMap.get(symbol));
        return new Result(1, Msg.SUCCESS, regularExpression);
    }

    private boolean mergeSameItem(HashMap<String, List<List<String>>> regularGram, HashMap<String, String> kvMap,
                                  HashMap<String, HashMap<String, String>> stiMap) {
        Iterator iterator = regularGram.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            List<List<String>> values = regularGram.get(key);

            HashMap<String, String> tempMap = new HashMap<>(1);
            StringBuilder builder = new StringBuilder();

            for (List<String> letters : values) {
                if (letters.size() == 1) {
                    builder.append(letters.get(0)).append("+");
                } else if (letters.size() > 1) {
                    String vn = mergeItem(tempMap, letters.get(0), letters.get(1));
                    if (!regularGram.containsKey(vn)) {
                        System.out.println("vn:"+vn);
                        return false;
                    }
                }
            }
            if (builder.length() > 0) {
                String addStr = builder.toString();
                addStr = addStr.substring(0, addStr.length() - 1);
                if (tempMap.size() > 0) {
                    subBuildMap(tempMap, GrammarConstant.NON, addStr);
                } else {
                    // 只有某个 key 的产生式中，左部只包含非终结符，右部只包含终结符时，才将其加入 kvMap
                    // 否则，需要在 stiMap 中记录两种情况
                    kvMap.put(key, addStr);
                }
            }
            if (tempMap.size() > 0) {
                stiMap.put(key, tempMap);
            }
        }
        return true;
    }

    private void replaceExpression(
            HashMap<String, String> kvMap,
            HashMap<String, HashMap<String, String>> stiMap) {
        // 为了能够全部转换，所以使用 judge 循环判断
        boolean judge;
        do {
            judge = false;
            Iterator iterator = stiMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                HashMap<String, String> vnt = stiMap.get(key);
                Iterator ite = vnt.keySet().iterator();

                StringBuilder builder = new StringBuilder();

                List<String> temp = new ArrayList<>();
                while (ite.hasNext()) {
                    String vn = (String) ite.next();
                    if (vn.equals(GrammarConstant.NON)) {
                        continue;
                    }
                    if (kvMap.containsKey(vn)) {
                        subBuild(builder, kvMap.get(vn), vnt.get(vn));
                        temp.add(builder.toString());
                        builder.delete(0, builder.length());
                        ite.remove();
                    }
                }
                if (!temp.isEmpty()) {
                    for (String str : temp) {
                        subBuildMap(vnt, GrammarConstant.NON, str);
                    }
                }
                // X::=a => X=a
                if (vnt.size() == 1 && vnt.containsKey(GrammarConstant.NON)) {
                    kvMap.put(key, vnt.get(GrammarConstant.NON));
                    judge = true;
                    iterator.remove();
                }
            }
        } while (judge);
    }

    void rebuildKvMap(
            HashMap<String, String> kvMap,
            HashMap<String, HashMap<String, String>> stiMap) {
        // Y ::= Xa | b 直接查找 X 对应的 stiMap，如果 X 对应只有两个非终结符，
        // 并且 其中一个是 Y，另一个是 NON，
        // 则将 Y 中的 X 替换掉。
        boolean judge;
        boolean judge1 = false;
        boolean judgeGoOn = false;
        do {
            judge = false;
            Iterator iterator = stiMap.keySet().iterator();
            // 临时的 用来记录需要修改的 stiMap 的 key-value 映射，避免 快速失败
            HashMap<String, HashMap<String, String>> tempMap = new HashMap<>(1);
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                HashMap<String, String> vnt = stiMap.get(key);
                Iterator ite = vnt.keySet().iterator();
                // 临时的 用来记录需要修改的 stiMap 的 subMap 中的 key-value 映射，避免 快速失败
                HashMap<String, String> subTempMap = new HashMap<>(1);
                while (ite.hasNext()) {
                    String vn = (String) ite.next();
                    if (vn.equals(key)) {
                        // 如果 X = Y，则为 Y = Ya+b ，直接的出结果 ba*
                        String bx = tryMergeSameItem(vnt, subTempMap, vn);
                        if (subTempMap.isEmpty()) {
                            kvMap.put(key, bx);
                        } else if (subTempMap.size() == 1 && subTempMap.containsKey(GrammarConstant.NON)) {
                            kvMap.put(key, subTempMap.get(GrammarConstant.NON));
                            subTempMap.remove(GrammarConstant.NON);
                        } else {
                            judge = true;
                        }
                        iterator.remove();
                        break;
                    }

                    // Y ::= Xa | b
                    // X ::= Yc | d
                    // 则使用 Yc+d 替换 X
                    if (stiMap.containsKey(vn) && stiMap.get(vn).containsKey(key)) {
                        rebuildSubTempMap(vnt, stiMap, subTempMap, vn);
                        iterator.remove();
                        judge1 = true;
                        break;
                    } else if (tempMap.containsKey(vn) && tempMap.get(vn).containsKey(key)) {
                        rebuildSubTempMap(vnt, tempMap, subTempMap, vn);
                        judge1 = true;
                        break;
                    }
                }
                if (judge || judge1) {
                    tempMap.put(key, subTempMap);
                    judgeGoOn = true;
                }

                // Y ::= Xa | Dd | b
                // X ::= Yc | Dd | d
                // 则使用 Yc+Dd+d 替换 X 后会出现 Y=Y~ 的情况，因此可以消除 右部分的 Y
                if (judge1) {
                    subTempMap = new HashMap<>(1);
                    HashMap<String, String> vntTemp = tempMap.get(key);
                    String bx = tryMergeSameItem(vntTemp, subTempMap, key);
                    if (subTempMap.isEmpty()) {
                        kvMap.put(key, bx);
                    } else if (subTempMap.size() == 1 && subTempMap.containsKey(GrammarConstant.NON)) {
                        kvMap.put(key, subTempMap.get(GrammarConstant.NON));
                    } else {
                        tempMap.put(key, subTempMap);
                    }
                    judge1 = false;
                }
            }
            if (judgeGoOn) {
                mergeMap(stiMap, tempMap);
                judgeGoOn = false;
            }
        } while (judge);
    }

    private void rebuildSubTempMap(
            HashMap<String, String> vnt,
            HashMap<String, HashMap<String, String>> tempMap,
            HashMap<String, String> subTempMap,
            String vn) {
        HashMap<String, String> vnt2 = tempMap.get(vn);
        StringBuilder builder = new StringBuilder();
        Iterator ite1 = vnt2.keySet().iterator();
        while (ite1.hasNext()) {
            String key1 = (String) ite1.next();
            subBuild(builder, vnt2.get(key1), vnt.get(vn));
            String value1 = builder.toString();
            builder.delete(0, builder.length());
            if (vnt.containsKey(key1)) {
                subBuildMap(subTempMap, key1, vnt.get(key1));
            }
            subBuildMap(subTempMap, key1, value1);
        }
    }

    String tryMergeSameItem(
            HashMap<String, String> vnt,
            HashMap<String, String> subTempMap,
            String vn) {
        StringBuilder builder = new StringBuilder();

        subBuilder(builder, "(" + vnt.get(vn) + ")*");
        String bx = builder.toString();
        builder.delete(0, builder.length());

        Iterator ite1 = vnt.keySet().iterator();
        while (ite1.hasNext()) {
            String vn1 = (String) ite1.next();
            if (!vn1.equals(vn)) {
                subBuild(builder, vnt.get(vn1), bx);
                String value1 = builder.toString();
                subBuildMap(subTempMap, vn1, value1);
                builder.delete(0, builder.length());
            }
        }
        return bx;
    }

    abstract String mergeItem(HashMap<String, String> map, String kv1, String kv2);

    abstract void subBuild(StringBuilder builder, String v1, String v2);
}
