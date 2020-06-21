package com.compiler.grammar.util;

import com.compiler.grammar.constant.GrammarConstant;
import com.compiler.grammar.constant.Msg;
import com.compiler.grammar.entity.Result;
import com.compiler.grammar.entity.Status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

/**
 * @author 10652
 */
public class SFUtil {
    private static void rebuildStatus(Status status, String nextWord) {
        if (status.getNextWord().containsKey(nextWord)) {
            return;
        }
        HashSet<Integer> set = new HashSet<>();
        set.add(-1);
        status.getNextWord().put(nextWord, set);
    }

    private static void rebuildEndStatus(Status status, String nextWord, Integer wordLength) {
        if (status.getNextWord().containsKey(nextWord)) {
            HashSet<Integer> len = status.getNextWord().get(nextWord);
            len.add(wordLength);
            status.getNextWord().put(nextWord, len);
        } else {
            HashSet<Integer> set = new HashSet<>();
            set.add(wordLength);
            status.getNextWord().put(nextWord, set);
        }
    }


    public static Result enterSensitiveWord(String words) {
        HashMap<String, Status> sensitive = new HashMap<>(1);

        if (Objects.isNull(words) || (words = words.trim()).length() == 0) {
            return new Result(0, Msg.ERROR_SENSITIVE_WORD);
        }
        String[] sws = words.split(" ");
        System.out.println(Arrays.toString(sws));
        for (String word : sws) {
            if (word.isEmpty()) {
                continue;
            }
            String key = String.valueOf(word.charAt(0));
            Status status;
            if (word.length() == 1) {
                if (sensitive.containsKey(key)) {
                    status = sensitive.get(key);
                } else {
                    status = new Status();
                    sensitive.put(key, status);
                }
                rebuildEndStatus(status, GrammarConstant.NON, 1);
            }
            for (int i = 1; i < word.length(); ++i) {
                String vt = String.valueOf(word.charAt(i));

                if (sensitive.containsKey(key)) {
                    status = sensitive.get(key);
                } else {
                    status = new Status();
                    sensitive.put(key, status);
                }
                if (i == word.length() - 1) {
                    rebuildEndStatus(status, vt, i + 1);
                } else {
                    rebuildStatus(status, vt);
                }
                key = vt;
            }
        }
        return new Result(1, Msg.SUCCESS, sensitive);
    }

    public static Result filterSensitive(String wordString, HashMap<String, Status> sensitive) {
        wordString = wordString.replaceAll(" ", "");
        HashSet<String> sensitiveList = new HashSet<>();
        if (Objects.isNull(wordString) || (wordString = wordString.trim()).length() == 0) {
            return new Result(0, Msg.ERROR_WORDS);
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < wordString.length(); ++i) {
            String key = String.valueOf(wordString.charAt(i));
            // 当前词 不匹配敏感词
            if (!sensitive.containsKey(key)) {
                builder.delete(0, builder.length());
                continue;
            }
            builder.append(key);
            Status status = sensitive.get(key);
            // 构成敏感词（单个词）
            if (status.getNextWord().containsKey(GrammarConstant.NON)) {
                sensitiveList.add(builder.toString());
                builder.delete(0, builder.length());
                continue;
            }
            String vt;
            if (i < wordString.length() - 1) {
                vt = String.valueOf(wordString.charAt(i + 1));
            } else {
                // 句子的末尾了，不构成敏感词
                builder.delete(0, builder.length());
                continue;
            }

            // key 不构成敏感词
            if (!status.getNextWord().containsKey(vt)) {
                builder.delete(0, builder.length());
                continue;
            }
            // 说明 vt 可能构成敏感词
            HashSet<Integer> lenSet = status.getNextWord().get(vt);
            // 敏感词匹配，如果匹配成功，则记录，否则需要遍历下一个词继续判断
            if (lenSet.contains(builder.length() + 1)) {
                // 成功匹配敏感词记录
                i++;
                builder.append(vt);
                // 记录敏感词
                sensitiveList.add(builder.toString());
                // 敏感词匹配记录之后，先不清空缓冲，也许此次当前的匹配到的词还能和后面的词组合成其他的敏感词
                builder.delete(0, builder.length());
            }
        }
        return new Result(1, Msg.SUCCESS, sensitiveList);
    }

    public static void main(String[] args) {
        Result result = enterSensitiveWord("装傻小傻子 装傻 小傻子 大小 大傻子 人小鬼大  大大大 大大大傻子");
        if (result.getCode().equals(0)) {
            System.out.println(result.getMsg());

        } else {
            HashMap<String, Status> sensitive = (HashMap<String, Status>) result.getBody();
            System.out.print("SENSITIVE：");
            System.out.println(sensitive);

            String words = "大家都别装。大大大傻子，人小大傻子，都是装傻小傻子";
            System.out.println(filterSensitive(words, sensitive).getBody());
        }
    }
}
