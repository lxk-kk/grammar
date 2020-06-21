package com.compiler.grammar.util;

import java.util.HashMap;

/**
 * @author 10652
 */
public class LREUtil extends REUtil {
    @Override
    String mergeItem(HashMap<String, String> map, String kv1, String kv2) {
        subBuildMap(map, kv1, kv2);
        return kv1;
    }

    @Override
    void subBuild(StringBuilder builder, String v1, String v2) {
        subBuilder(builder, v1, v2);
    }
}
