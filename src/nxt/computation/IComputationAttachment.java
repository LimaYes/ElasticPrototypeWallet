package nxt.computation;

import nxt.*;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

/******************************************************************************
 * Copyright © 2017 The XEL Core Developers.                                  *
 *                                                                            *
 * See the AUTHORS.txt, DEVELOPER-AGREEMENT.txt and LICENSE.txt files at      *
 * the top-level directory of this distribution for the individual copyright  *
 * holder information and the developer policies on copyright and licensing.  *
 *                                                                            *
 * Unless otherwise agreed in a custom licensing agreement, no part of the    *
 * XEL software, including this file, may be copied, modified, propagated,    *
 * or distributed except according to the terms contained in the LICENSE.txt  *
 * file.                                                                      *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************/

public abstract class IComputationAttachment  {

    abstract String getAppendixName();

    IComputationAttachment(ByteBuffer buffer) {
    }

    IComputationAttachment() {
    }

    public final int getSize() {
        return getMySize();
    }

    abstract int getMySize();
    abstract byte getMyMessageIdentifier();


    @Override
    abstract public String toString();

    public final byte[] getByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(1+getMySize());
        buffer.put(getMyMessageIdentifier()); // add this to byte buffer and retreive before parsing
        putMyBytes(buffer);
        return buffer.array();
    }

    abstract void putMyBytes(ByteBuffer buffer);

    abstract boolean validate(Transaction transaction) throws NxtException.ValidationException;
    abstract void apply(Transaction transaction, Account senderAccount);

}
