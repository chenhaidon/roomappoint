package com.example.web.enums;

import java.util.HashMap;

public enum FeedbackStatusEnum {
    待处理(1),
    已处理(2),
    已关闭(3);

    private final int index;

    FeedbackStatusEnum(int index) {
        this.index = index;
    }

    public int index() {
        return index;
    }

    private static final HashMap<Integer, FeedbackStatusEnum> MY_MAP = new HashMap<>();

    static {
        for (FeedbackStatusEnum myEnum : values()) {
            MY_MAP.put(myEnum.index(), myEnum);
        }
    }

    public static FeedbackStatusEnum GetEnum(Integer v) {
        if (v == null) {
            return MY_MAP.values().stream().findFirst().get();
        }
        return MY_MAP.get(v);
    }
}
