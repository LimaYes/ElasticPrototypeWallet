package com.community.CTypeInts;

public abstract class IntegerType {
    private int bytes;
    private long content;
    private boolean unsigned;

    public IntegerType(int bytes, long content, boolean unsigned) {
        this.bytes = bytes;
        this.unsigned = unsigned;
        _set(content);
    }

    public long longValue(){
        return content;
    }
    public void _set(long content) {
        this.content = content;
        switch (this.bytes) {
            case 4:
                this.content = this.content & 0xFFFFFFFF;
                break;
            case 8:
                this.content = content;
                break;
            default: // default is int32
                this.content = this.content & 0xFFFFFFFF;
        }
    }

    public long half(){ // primarily used for testing
        if(bytes==4){
            if(unsigned)
                this.content = Integer.divideUnsigned((int)this.content, 2);
            else
                this.content = (int)this.content / 2;
        }else{
            if(unsigned)
                this.content = Long.divideUnsigned(this.content, 2);
            else
                this.content = this.content / 2;
        }
        return this.content;
    }

    public long shr(int val){ // primarily used for testing
        if(bytes==4){
            this.content = (long)((int)this.content >>> val);
        }else{
            this.content = this.content >>> val;
        }
        return this.content;
    }

    public long shl(int val){ // primarily used for testing
        if(bytes==4){
            this.content = (long)((int)this.content << val);
        }else{
            this.content = this.content << val;
        }
        return this.content;
    }

    public long rotl(int val){ // primarily used for testing
        if(bytes==4){
            this.content = Integer.rotateRight((int)content, val);
        }else{
            this.content = Long.rotateRight(content, val);
        }
        return this.content;
    }

    public long rotr(int val){ // primarily used for testing
        if(bytes==4){
            this.content = Integer.rotateLeft((int)content, val);
        }else{
            this.content = Long.rotateLeft(content, val);
        }
        return this.content;
    }

    public String stringValue(){ // also, still only used in testing
        if(unsigned) return Long.toUnsignedString(content);
        else return Long.toString(content);
    }

    static long safeLong(String long_){
        try {
            if (long_.startsWith("-"))
                return Long.parseLong(long_);
            else
                return Long.parseUnsignedLong(long_);
        }catch
                (Exception e){
            return 0L;
        }
    }
}