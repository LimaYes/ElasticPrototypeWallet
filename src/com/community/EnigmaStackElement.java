package com.community;

import nxt.util.Convert;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.community.EnigmaProgram.MEM_TARGET_STORE.*;

public class EnigmaStackElement {
    private byte[] content;
    private EnigmaProgram.MEM_TARGET_STORE type;

    public byte[] getContent() {
        return content;
    }

    public EnigmaProgram.MEM_TARGET_STORE getType() {
        return type;
    }

    public EnigmaStackElement(byte[] content, EnigmaProgram.MEM_TARGET_STORE type) {
        this.content = content;
        this.type = type;
    }

    public EnigmaStackElement(){
        this.content = null;
        this.type = null;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setType(EnigmaProgram.MEM_TARGET_STORE type) {
        this.type = type;
    }

    @Override
    public String toString(){
        if(type==F)
            return String.format("%s: %f [%s]", type.name(), ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getFloat(), Convert.toHexString(content));
        else if(type==D)
            return String.format("%s: %f [%s]", type.name(), ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getDouble(), Convert.toHexString(content));
        else if(type==I)
            return String.format("%s: %d [%s]", type.name(), ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getInt(), Convert.toHexString(content));
        else if(type==U)
            return String.format("%s: %s [%s]", type.name(), Integer.toUnsignedString(ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getInt()), Convert.toHexString(content));
        else if(type==I)
            return String.format("%s: %d [%s]", type.name(), ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getLong(), Convert.toHexString(content));
        else if(type==UL)
            return String.format("%s: %s [%s]", type.name(), Long.toUnsignedString(ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getLong()), Convert.toHexString(content));
        else
            return String.format("unknown: %s", Convert.toHexString(content));
    }

    public long getLong(){
        switch(this.getType()){
            case L:
            case UL:
                return ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getLong();
            case I:
            case U:
                return (long)ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getInt();
            case D:
                return (long)ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            case F:
                return (long)ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        }
        return 0;
    }

    public int getInt(){
        switch(this.getType()){
            case L:
            case UL:
                return (int)ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getLong();
            case I:
            case U:
                return ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getInt();
            case D:
                return (int)ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            case F:
                return (int)ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        }
        return 0;
    }
    public float getFloat(){
        switch(this.getType()){
            case L:
            case UL:
                return (float)ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getLong();
            case I:
            case U:
                return (float)ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getInt();
            case D:
                return (float)ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            case F:
                return ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        }
        return 0;
    }
    public double getDouble(){
        switch(this.getType()){
            case L:
            case UL:
                return (double)ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getLong();
            case I:
            case U:
                return (double)ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getInt();
            case D:
                return ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            case F:
                return (double)ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        }
        return 0;
    }

    public void setLong(long x){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN).putLong(x);
        content = buffer.array();
    }
    public void setInt(int x){
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN).putLong(x);
        content = buffer.array();
    }
    public void setFloat(float x){
        ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN).putFloat(x);
        content = buffer.array();
    }
    public void setDouble(double x){
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN).putDouble(x);
        content = buffer.array();
    }


    public void convertType(EnigmaProgram.MEM_TARGET_STORE cast) {
        this.type = cast;
    }

    public boolean isNotZero() {
        switch(this.getType()){
            case L:
            case UL:
                return this.getLong()!=0;
            case I:
            case U:
                return this.getInt()!=0;
            case D:
                return this.getDouble()!=0;
            case F:
                return this.getFloat()!=0;
        }
        return false;
    }
}