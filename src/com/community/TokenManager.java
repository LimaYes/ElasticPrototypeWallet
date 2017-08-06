package com.community;

import java.util.Arrays;

import static com.community.Constants.MAX_LITERAL_SIZE;
import static com.community.Constants.epl_token;
import static com.community.Primitives.DATA_TYPE.*;
import static com.community.Primitives.EPL_TOKEN_TYPE.*;
import static com.community.Primitives.EXP_TYPE.EXP_EXPRESSION;
import static com.community.Primitives.EXP_TYPE.EXP_FUNCTION;

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
public class TokenManager {

    public Primitives.STATE state;

    public TokenManager(){
        this.state = new Primitives.STATE();
    }

    private boolean add_token(int token_id, String literal, Primitives.DATA_TYPE data_type, int line_num){
        if (token_id >= 0) {
            Primitives.EXP_TOKEN_LIST epl_tok = epl_token[token_id];

            // Determine If '-' Is Binary Or Unary
            if (epl_tok.type == TOKEN_SUB) {
                if (this.state.token_list.size() == 0)
                    token_id++;
                else if (this.state.token_list.get(this.state.token_list.size() - 1).type != TOKEN_CLOSE_PAREN) {
                    if (this.state.token_list.get(this.state.token_list.size() - 1).exp != EXP_EXPRESSION || this.state.token_list.get(this.state.token_list.size() - 1).inputs > 1)
                        token_id++;
                }
            }
            Primitives.SOURCE_TOKEN tok = new Primitives.SOURCE_TOKEN(token_id, epl_tok.type, null, epl_tok.exp, epl_tok.inputs, epl_tok.prec, line_num, epl_tok.data_type);
            state.token_list.add(tok);
        }
        // Literals
        else if (literal != null) {
            Primitives.SOURCE_TOKEN tok = new Primitives.SOURCE_TOKEN(token_id, TOKEN_LITERAL, literal,
                    EXP_EXPRESSION, 0,
                    -1, line_num, data_type);
            state.token_list.add(tok);
        }
        // Error
        else {
            return false;
        }

        return true;
    }

    private Primitives.DATA_TYPE validate_literal(String str) {

        int i, len;
        int max_hex_len = 18;
        int max_bin_len = 66;
        int max_int_len = 21;
        int max_dbl_len = 21;
        boolean string = false;
        boolean found_dot = false;
        String dot_sub_str = "";

        if (str==null || str.length() == 0)
            return DT_NONE; // todo: failure, is it okay to return DT_NONE here

        len = str.length();

        // Validate Hex Numbers
        if (str.startsWith("0x")) {
            if ((len <= 2) || (len > max_hex_len))
                return DT_NONE;

            for (i = 2; i < len; i++) {
                if (!(str.charAt(i) >= '0' && str.charAt(i) <= '9') && !(str.charAt(i) >= 'a' && str.charAt(i) <=
                        'f') && !(str.charAt(i) >= 'A' && str.charAt(i) <= 'F'))
                    return DT_NONE;
            }
            return (string ? DT_STRING : DT_INT);
        }

        // Validate Binary Numbers
        if (str.startsWith("0b")) {
            if ((len <= 2) || (len > max_bin_len))
                return DT_NONE;

            for (i = 2; i < len; i++) {
                if ((str.charAt(i) != '0') && (str.charAt(i) != '1'))
                    return DT_NONE;
            }
            return (string ? DT_STRING : DT_INT);
        }

        // Validate Doubles
        if(str.contains(".")) {
            found_dot = true;
            dot_sub_str = str.substring(str.indexOf(".")); // todo: check if +1 is needed
        }

        if (found_dot) {
            if ((len <= 1) || (len > max_dbl_len))
                return DT_NONE;

            len = str.indexOf(".");
            for (i = 0; i < len; i++) {
                if ((i == 0) && (str.charAt(0) == '-'))
                    continue;

                if (!(str.charAt(i) >= '0' && str.charAt(i) <= '9'))
                    return DT_NONE;
            }

            len = dot_sub_str.length(); // todo: check if -1 must go here
            for (i = 1; i < len; i++) {
                if (!(dot_sub_str.charAt(i) >= '0' && dot_sub_str.charAt(i) <= '9'))
                    return DT_NONE;
            }
            return (string ? DT_STRING : DT_FLOAT);
        }

        // Validate Ints
        if (((str.charAt(0) == '-') && (len > (max_int_len + 1))) || (len > max_int_len))
            return DT_NONE;

        for (i = 0; i < len; i++) {
            if ((i == 0) && (str.charAt(0) == '-'))
                continue;

            if (!(str.charAt(i) >= '0' && str.charAt(i) <= '9'))
                return DT_NONE;
        }
        return (string ? DT_STRING : DT_INT);
    }

    private void validate_tokens() throws Exceptions.SyntaxErrorException {
        int i;
        for (i = 0; i < this.state.token_list.size(); i++) {
            // Validate That If/Repeat/Functions Have '('
            if ((this.state.token_list.get(i).type == TOKEN_IF) ||
                    (this.state.token_list.get(i).type == TOKEN_REPEAT) ||
                    (this.state.token_list.get(i).exp == EXP_FUNCTION) ) {

                if ((i == (this.state.token_list.size()-1)) || (this.state.token_list.get(i+1).type !=
                        TOKEN_OPEN_PAREN)) {
                    throw new Exceptions.SyntaxErrorException("Syntax Error - Missing '('  Line: " + this.state.token_list.get(i).line_num);
                }
            }
        }
    }

    public void build_token_list(String str) throws Exceptions.SyntaxErrorException {
        int i, idx, len, token_id, line_num, token_list_sz, literal_idx;
        char c;
        Primitives.DATA_TYPE data_type;
        boolean literal_str = false;
        char[] literal = new char[MAX_LITERAL_SIZE];
        len = str.length();
        idx = 0;
        line_num = 1;
        literal_idx = 0;

        while(idx < len){
            token_id = -1;
            c = str.charAt(idx);
            if(literal_idx > 0){
                if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_'))
                    literal_str = true;
                else
                    literal_str = false;
            }
            if (!literal_str) {

                // Remove Whitespace
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f') {

                    if (literal_idx > 0) {

                        // Check If '-' Token Needs To Be Moved To Literal
                        if (this.state.token_list.get(this.state.token_list.size()-1).type == TOKEN_NEG) {
                            for (i = MAX_LITERAL_SIZE - 2; i > 0; i--)
                                literal[i] = literal[i - 1];
                            literal[0] = '-';

                            // Remove '-' From Token List
                            this.state.token_list.remove(this.state.token_list.size()-1);
                        }

                        if (this.state.token_list.get(this.state.token_list.size()-1).type == TOKEN_FUNCTION) {
                            data_type = DT_STRING;
                        }
                        // todo: check if -2 can crash at some point (idx out of bounds)
                        else if (this.state.token_list.get(this.state.token_list.size()-2).type ==
                                TOKEN_CALL_FUNCTION) {
                            data_type = DT_STRING;
                        }
                        else {
                            data_type = validate_literal(String.valueOf(literal).trim()); // here, char[] is converted to// String
                            if (data_type == DT_NONE) {
                                throw new Exceptions.SyntaxErrorException("Syntax Error - Invalid Literal: '" +
                                        String.valueOf(literal).trim() + "' Line: " + line_num);
                            }
                        }
                        add_token(-1, String.valueOf(literal).trim(), data_type, line_num);
                        literal_idx = 0;
                        Arrays.fill(literal, (char)0x00); // reset
                    }

                    // Increment Line Number Counter
                    if (c == '\n') {
                        line_num++;
                    }

                    idx++;
                    continue;
                }
                // Check For EPL Token
                for (i = 0; i < epl_token.length; i++) {
                    if(str.substring(idx).startsWith(epl_token[i].str)){ // TODO: make more efficient
                        token_id = i;
                        break;
                    }
                }
            }
            if (token_id >= 0) {
                // Remove Single Comments
                if (epl_token[token_id].type == TOKEN_COMMENT) {
                    int pos = str.indexOf("\n",idx);

                    if (pos == -1){
                        throw new Exceptions.SyntaxErrorException("Syntax Error - Missing new line after single line comment: " + line_num);
                    }

                    idx = pos + 1; // todo: double check if 1 is needed here
                    line_num++;
                    continue;
                }

                // Remove Block Comments
                if (epl_token[token_id].type == TOKEN_BLOCK_COMMENT) {
                    int pos = str.indexOf("*/",idx);


                    if (pos == -1){
                        throw new Exceptions.SyntaxErrorException("Syntax Error - Missing '*/'  Line: " + line_num);
                    }

                    // Count The Number Of Lines Skipped
                    i = pos;
                    while (i >= idx + 2) { // todo: sure this works correctly
                        if (str.charAt(i) == '\n')
                            line_num++;
                        i--;
                    }

                    idx = pos + 2; // todo: +2 corret here?
                    continue;
                }

                // Add Literals To Token List
                if (literal_idx > 0) {

                    // Check If '-' Token Needs To Be Moved To Literal
                    if (this.state.token_list.get(this.state.token_list.size()-1).type == TOKEN_NEG) {
                        for (i = MAX_LITERAL_SIZE - 2; i > 0; i--)
                            literal[i] = literal[i - 1];
                        literal[0] = '-';

                        // Remove '-' From Token List
                        this.state.token_list.remove(this.state.token_list.size()-1);
                    }

                    if (epl_token[token_id].type == TOKEN_CALL_FUNCTION) {
                        data_type = DT_STRING;
                    }
                    else if (this.state.token_list.get(this.state.token_list.size()-1).type == TOKEN_CALL_FUNCTION) {
                        data_type = DT_STRING;
                    }
                    else {
                        data_type = validate_literal(String.valueOf(literal).trim());
                        if (data_type == DT_NONE) {
                            throw new Exceptions.SyntaxErrorException("Syntax Error - Invalid Literal: '" + String.valueOf(literal).trim() + "'  Line: " + line_num);
                        }
                    }

                    add_token( -1, String.valueOf(literal).trim(), data_type, line_num);
                    literal_idx = 0;
                    Arrays.fill(literal, (char)0x00);
                }

                add_token(token_id, null, DT_NONE, line_num);
                idx += epl_token[token_id].len;
            }
            else {
                literal[literal_idx] = c;
                literal_idx++;
                idx++;

                if (literal_idx > MAX_LITERAL_SIZE) {
                    throw new Exceptions.SyntaxErrorException("Syntax Error - Invalid Literal: '" + String.valueOf(literal).trim() + "'  Line: " + line_num);
                }
            }
        }
    }
}
