package nxt.computation;

import nxt.*;
import nxt.http.ParameterException;
import nxt.http.ParameterParser;
import nxt.peer.Peers;
import nxt.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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


public class MessageEncoder {

    /*
    the reason that both are present is, that messages can be split up in multiple chunks. Each chunk must be identifies as a NON message, but the last chunk only triggers the message parsing
     warning: both must be of same length */
    static byte[] MAGIC = {(byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef, (byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef};
    static byte[] MAGIC_INTERMEDIATE = {(byte)0xef, (byte)0xbe, (byte)0xad, (byte)0xde, (byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef};


    static boolean useComputationEngine = Nxt.getBooleanProperty("nxt.enableComputationEngine");

    static void processBlockInternal(Block block){
        // Check all TX for relevant stuff
        for(Transaction t : block.getTransactions()){
            Appendix.PrunablePlainMessage m = t.getPrunablePlainMessage();
            if(m==null) continue;
            if(MessageEncoder.checkMessageForPiggyback(m, true, false)){
                try {
                    Appendix.PrunablePlainMessage[] reconstructedChain = MessageEncoder.extractMessages(t);

                    // Allow the decoding of the attachment
                    IComputationAttachment att = MessageEncoder.decodeAttachment(reconstructedChain);
                    if(att == null) continue;
                    att.apply(t);
                } catch (Exception e) {
                    // generous catch, do not allow anything to cripple the blockchain integrity
                    continue;
                }
            }
        }
        block.setLocallyProcessed();
    }

    static {
        Nxt.getBlockchainProcessor().addListener(block -> {
            if (block.getHeight() < ComputationConstants.START_ENCODING_BLOCK || !useComputationEngine) {
                return;
            }
            processBlockInternal(block);
        }, BlockchainProcessor.Event.AFTER_BLOCK_APPLY);
    }

    public static void init(){
        // Here, we need to catch up if there are blocks remaining which have not been "work parsed" and the computation engine is started/active
        int current_height = Nxt.getBlockchain().getHeight();
        int last_processed_height = Nxt.getBlockchain().getLastLocallyProcessedHeight();

        if(last_processed_height == current_height) return; // no need to do anything
        if(last_processed_height > current_height) return; // this should never happen anyways, jetz keep it in here to avoid any chance of an infinite loop

        // Catch up!
        for(int i=last_processed_height + 1; i<=last_processed_height; ++i){

            Block block = Nxt.getBlockchain().getBlockAtHeight(i);
            if(block==null){
                // This is bad!
                throw new RuntimeException("Could not retrieve Block from BlockDB (height: " + i);
            }
            processBlockInternal(block);
            Logger.logInfoMessage("Catching up work related information from past blocks - block " + i + " of " + last_processed_height);
        }
    }


    public static void push(IComputationAttachment work, String secretPhrase) throws NxtException, IOException {
        Appendix.PrunablePlainMessage[] messages = MessageEncoder.encodeAttachment(work);
        JSONStreamAware[] individual_txs = MessageEncoder.encodeTransactions(messages, secretPhrase);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        for(int i=0;i<individual_txs.length;++i){
            individual_txs[i].writeJSONString(pw);
        }
        StringBuffer sb = sw.getBuffer();
        MessageEncoder.pushThemAll(individual_txs);
    }

    public static Appendix.PrunablePlainMessage[] extractMessages(Transaction _t) throws NxtException.ValidationException {

        Transaction t = _t;

        ArrayList<Appendix.PrunablePlainMessage> arl = new ArrayList<>();

        if(t == null) throw new NxtException.NotValidException("This transaction is not a valid work-encoder");
        Appendix.PrunablePlainMessage pm = t.getPrunablePlainMessage();
        if(pm == null) throw new NxtException.NotValidException("This transaction is not a valid work-encoder");

        if(!checkMessageForPiggyback(pm, true, false)){
            throw new NxtException.NotValidException("This transaction is not a valid work-encoder");
        }

        arl.add(pm);

        int counter = 0;

        // now, that we have the original transaction we have to fetch (possible) referenced transactions
        while(t.getReferencedTransactionFullHash() != null){
            t = Nxt.getBlockchain().getTransactionByFullHash(t.getReferencedTransactionFullHash());

            if(t == null) throw new NxtException.NotValidException("This transaction is not a valid work-encoder");
            pm = t.getPrunablePlainMessage();
            if(pm == null) throw new NxtException.NotValidException("This transaction is not a valid work-encoder");

            if(!checkMessageForPiggyback(pm, false, true)){
                throw new NxtException.NotValidException("This transaction is not a valid work-encoder");
            }

            arl.add(0, pm);

            counter = counter + 1;
            if(counter > ComputationConstants.MAX_CHAINED_TX_ACCEPTED)
                throw new NxtException.NotValidException("This transaction references a chain which is too long");
        }

        return arl.toArray(new Appendix.PrunablePlainMessage[arl.size()]);
    }


    public static JSONStreamAware[] encodeTransactions(Appendix.PrunablePlainMessage[] msgs, String passphraseOrPubkey) throws NxtException {
        ArrayList<JSONStreamAware> array_tx = new ArrayList<>(msgs.length);

        // Transactions have to be created from "end to start" to get the "referenced tx hashes" chained up correctly
        String previousHash = "";
        for(int i=msgs.length-1; i>=0; --i){
            Pair<JSONStreamAware, String> t = null;
            if(previousHash.length()==0) {
                t = CustomTransactionBuilder.createTransaction(msgs[i], passphraseOrPubkey);
                previousHash = t.getElement1();
            }
            else
                t = CustomTransactionBuilder.createTransaction(msgs[i], passphraseOrPubkey, previousHash);
            array_tx.add(t.getElement0());
        }

        return array_tx.toArray(new JSONStreamAware[msgs.length]);
    }

    public static void pushThemAll(JSONStreamAware[] aw) throws NxtException.ValidationException, ParameterException {
        List<Transaction> toPush = new ArrayList<>();

        for(int i=0;i<aw.length;++i)
        {
            Transaction.Builder builder = ParameterParser.parseTransaction(aw[i].toString(), null, null);
            Transaction transaction = builder.build();
            transaction.validate(); // safe guard, so it cannot happen that tx1 goes through and tx2 fails validation
            toPush.add(transaction);
        }

        for(Transaction t : toPush){
            Nxt.getTransactionProcessor().broadcast(t);
        }
    }

    public static Appendix.PrunablePlainMessage[] encodeAttachment(IComputationAttachment att){
        try {
            ArrayList<Appendix.PrunablePlainMessage> preparation = new ArrayList<>();
            byte[] to_encode = att.getByteArray();
            int pos_counter=0;

            while(pos_counter<to_encode.length){
                int maximum_read = Math.min(Constants.MAX_PRUNABLE_MESSAGE_LENGTH - MAGIC.length, to_encode.length - pos_counter);
                byte[] msg = new byte[maximum_read + MAGIC.length];
                pos_counter += maximum_read;

                // now, depending on pos_counter decide whether MAGIC or MAGIC_INTERMEDIATE is appended
                if(pos_counter==to_encode.length)
                    System.arraycopy(MessageEncoder.MAGIC, 0, msg, 0, MessageEncoder.MAGIC.length);
                else
                    System.arraycopy(MessageEncoder.MAGIC_INTERMEDIATE, 0, msg, 0, MessageEncoder.MAGIC_INTERMEDIATE.length);

                System.arraycopy(to_encode, pos_counter-maximum_read, msg, MessageEncoder.MAGIC.length, maximum_read);
                Appendix.PrunablePlainMessage pl = new Appendix.PrunablePlainMessage(msg);
                preparation.add(pl);
            }

            return preparation.toArray(new Appendix.PrunablePlainMessage[preparation.size()]);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static IComputationAttachment decodeAttachment(Appendix.PrunablePlainMessage[] m){
        try {

            int total_length = 0;
            for(int i=0;i<m.length;++i) {
                if (!MessageEncoder.checkMessageForPiggyback(m[i])) return null;
                total_length = total_length + (m[i].getMessage().length - MessageEncoder.MAGIC.length);
            }

            byte[] work_package = new byte[total_length];
            int last_pos = 0;

            for(int i=0;i<m.length;++i) {
                byte[] msg = m[i].getMessage();
                System.arraycopy(msg, MessageEncoder.MAGIC.length, work_package, last_pos, msg.length-MessageEncoder.MAGIC.length);
                last_pos += msg.length;
            }

            if (work_package.length == 0) return null; // safe guard

            ByteBuffer wp_bb = ByteBuffer.wrap(work_package);

            byte messageType = wp_bb.get();
            if(messageType == CommandsEnum.CREATE_NEW_WORK.getCode()){
                return new CommandNewWork(wp_bb);
            }
            else if(messageType == CommandsEnum.CANCEL_WORK.getCode()){
                return new CommandCancelWork(wp_bb);
            }
            else if(messageType == CommandsEnum.POWBTY.getCode()){
                return new CommandPowBty(wp_bb);
            }
            else{
                return null;
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static boolean checkMessageForPiggyback(Appendix.PrunablePlainMessage plainMessage){
        return checkMessageForPiggyback(plainMessage, false, false);
    }

    public static boolean checkMessageForPiggyback(Appendix.PrunablePlainMessage plainMessage, boolean onlyFinalMessageOfChain, boolean onlyMidMessage){

        try {
            if (plainMessage.isText())
                return false;

            byte[] msg = plainMessage.getMessage();
            if (msg.length < MAGIC.length) return false;

            boolean returned = true;
            if(!onlyMidMessage) {
                for (int i = 0; i < MAGIC.length; ++i) {
                    if (msg[i] != MAGIC[i]) {
                        returned = false;
                        break;
                    }
                }
            }else{
                returned = false;
            }

            if(!returned && !onlyFinalMessageOfChain)
            {
                returned = true;
                for (int i = 0; i < MAGIC_INTERMEDIATE.length; ++i) {
                    if (msg[i] != MAGIC_INTERMEDIATE[i]){
                        returned = false;
                        break;
                    }
                }
            }
            return returned;
        }catch(Exception e){
            return false;
        }
    }
}
