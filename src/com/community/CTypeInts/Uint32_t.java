package com.community.CTypeInts;

public class Uint32_t extends IntegerType {
    /**
     * Constructor.
     */
    public Uint32_t() {
        this("0");
    }

    /**
     * Constructor.
     *
     * @param value Value.
     */
    public Uint32_t(String value) {
        super(4, safeLong(value), true);
    }
}
