package com.compiler.grammar.util;

import java.util.HashMap;

/**
 * @author 10652
 */
public class RREUtil extends REUtil {

    @Override
    void subBuild(StringBuilder builder, String v1, String v2) {
        subBuilder(builder, v2, v1);
    }
    @Override
    String mergeItem(HashMap<String, String> map, String kv1, String kv2) {
        subBuildMap(map, kv2, kv1);
        return kv2;
    }
}
