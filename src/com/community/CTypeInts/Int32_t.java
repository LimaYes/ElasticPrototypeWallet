package com.community.CTypeInts;

public class Int32_t extends IntegerType {
    /**
     * Constructor.
     */
    public Int32_t() {
        this("0");
    }

    /**
     * Constructor.
     *
     * @param value Value.
     */
    public Int32_t(String value) {
        super(4, safeLong(value), false);
    }
    public Int32_t(long value) {
        super(4, value, false);
    }
}
