package com.community.CTypeInts;

public class Int64_t extends IntegerType {
    /**
     * Constructor.
     */
    public Int64_t() {
        this("0");
    }

    /**
     * Constructor.
     *
     * @param value Value.
     */
    public Int64_t(String value) {
        super(8, safeLong(value), false);
    }
    public Int64_t(long value) {
        super(8, value, false);
    }
}
