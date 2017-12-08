package com.community.CTypeInts;

public class Uint64_t extends IntegerType {
    /**
     * Constructor.
     */
    public Uint64_t() {
        this("0");
    }

    /**
     * Constructor.
     *
     * @param value Value.
     */
    public Uint64_t(String value) {
        super(8, safeLong(value), true);
    }
    public Uint64_t(long value) {
        super(8, value, true);
    }
}
