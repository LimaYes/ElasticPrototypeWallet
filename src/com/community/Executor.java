package com.community;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import static com.community.Constants.MAX_SOURCE_SIZE;

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
public class Executor {


    public static String checkCodeAndReturnVerify(String elasticPL) throws Exception{
        if(elasticPL.length()>MAX_SOURCE_SIZE) throw new Exceptions.SyntaxErrorException("Code length exceeded");
        TokenManager t = new TokenManager();
        t.build_token_list(elasticPL);
        ASTBuilder.parse_token_list(t.state);
        int wcet = WCETCalculator.calc_wcet(t.state);
        int verify_wcet = WCETCalculator.get_verify_wcet(t.state);
        //System.out.println("Debug: WCETS " + wcet + ", verify " + verify_wcet);
        if(wcet > Constants.ABSOLUTELY_MAXIMUM_WCET){
            throw new Exceptions.SyntaxErrorException("Absolutely maximum WCET of " + Constants
                    .ABSOLUTELY_MAXIMUM_WCET + " exceeded: your script has a WCET of " + wcet + ".");
        }
        if(verify_wcet < 0){
            throw new Exceptions.SyntaxErrorException("Absolutely maximum verify function WCET has a strange value.");
        }
        if(verify_wcet > Constants.ABSOLUTELY_MAXIMUM_VERIFY_WCET){
            throw new Exceptions.SyntaxErrorException("Absolutely maximum verify function WCET of " + Constants
                    .ABSOLUTELY_MAXIMUM_VERIFY_WCET + " exceeded: your script has a verify function WCET of " + wcet + ".");
        }
        CodeConverter.convert_verify(t.state);

        String result = "";
        for(int i=0;i<t.state.stack_code.size();++i){
            result += t.state.stack_code.get(i);
        }

        return result;
    }

    public static boolean executeCode(String verifyCode, int[] storage_array){
        try {
            delight.rhinosandox.RhinoSandbox sandbox = delight.rhinosandox.RhinoSandboxes.create();
            sandbox.setInstructionLimit(Constants.INSTRUCTION_LIMIT);
            sandbox.setMaxDuration(Constants.SAFE_TIME_LIMIT);
            sandbox.inject("s", storage_array);
            Object res = sandbox.eval("epl", verifyCode + " verify();");
            return res.equals(new Double(1.0));
        }catch(Exception e){
            return false; // Failed execution (reason does not matter)
        }
    }

}
