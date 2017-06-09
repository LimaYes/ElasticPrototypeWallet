/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 * Copyright © 2016-2017 Jelurida IP B.V.                                     *
 *                                                                            *
 * See the LICENSE.txt file at the top-level directory of this distribution   *
 * for licensing information.                                                 *
 *                                                                            *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,*
 * no part of the Nxt software, including this file, may be copied, modified, *
 * propagated, or distributed except according to the terms contained in the  *
 * LICENSE.txt file.                                                          *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************/

/**
 * @depends {nrs.js}
 */
var NRS = (function (NRS, $, undefined) {
    var _password;

    NRS.setServerPassword = function (password) {
        _password = password;
    };

    NRS.sendRequest = function (requestType, data, callback, options) {
        if (!options) {
            options = {};
        }
        if (requestType == undefined) {
            NRS.logConsole("Undefined request type");
            return;
        }
        if (!NRS.isRequestTypeEnabled(requestType)) {
            callback({
                "errorCode": 1,
                "errorDescription": $.t("request_of_type", {
                    type: requestType
                })
            });
            return;
        }
        if (data == undefined) {
            NRS.logConsole("Undefined data for " + requestType);
            return;
        }
        if (callback == undefined) {
            NRS.logConsole("Undefined callback function for " + requestType);
            return;
        }

        $.each(data, function (key, val) {
            if (key != "secretPhrase") {
                if (typeof val == "string") {
                    data[key] = $.trim(val);
                }
            }
        });
        //convert NXT to NQT...
        var field = "N/A";
        try {
            var nxtFields = [
                ["feeNXT", "feeNQT"],
                ["amountNXT", "amountNQT"],
                ["priceNXT", "priceNQT"],
                ["refundNXT", "refundNQT"],
                ["discountNXT", "discountNQT"],
                ["phasingQuorumNXT", "phasingQuorum"],
                ["phasingMinBalanceNXT", "phasingMinBalance"],
                ["controlQuorumNXT", "controlQuorum"],
                ["controlMinBalanceNXT", "controlMinBalance"],
                ["controlMaxFeesNXT", "controlMaxFees"],
                ["minBalanceNXT", "minBalance"],
                ["shufflingAmountNXT", "amount"],
                ["monitorAmountNXT", "amount"],
                ["monitorThresholdNXT", "threshold"]
            ];

            for (i = 0; i < nxtFields.length; i++) {
                var nxtField = nxtFields[i][0];
                var nqtField = nxtFields[i][1];
                if (nxtField in data) {
                    data[nqtField] = NRS.convertToNQT(data[nxtField]);
                    delete data[nxtField];
                }
            }
        } catch (err) {
            callback({
                "errorCode": 1,
                "errorDescription": err + " (" + $.t(field) + ")"
            });
            return;
        }
        // convert asset/currency decimal amount to base unit
        try {
            var currencyFields = [
                ["phasingQuorumQNTf", "phasingHoldingDecimals"],
                ["phasingMinBalanceQNTf", "phasingHoldingDecimals"],
                ["controlQuorumQNTf", "controlHoldingDecimals"],
                ["controlMinBalanceQNTf", "controlHoldingDecimals"],
                ["minBalanceQNTf", "create_poll_asset_decimals"],
                ["minBalanceQNTf", "create_poll_ms_decimals"],
                ["amountQNTf", "shuffling_asset_decimals"],
                ["amountQNTf", "shuffling_ms_decimals"]
            ];
            var toDelete = [];
            for (i = 0; i < currencyFields.length; i++) {
                var decimalUnitField = currencyFields[i][0];
                var decimalsField = currencyFields[i][1];
                field = decimalUnitField.replace("QNTf", "");

                if (decimalUnitField in data && decimalsField in data) {
                    data[field] = NRS.convertToQNT(parseFloat(data[decimalUnitField]), parseInt(data[decimalsField]));
                    toDelete.push(decimalUnitField);
                    toDelete.push(decimalsField);
                }
            }
            for (var i = 0; i < toDelete.length; i++) {
                delete data[toDelete[i]];
            }
        } catch (err) {
            callback({
                "errorCode": 1,
                "errorDescription": err + " (" + $.t(field) + ")"
            });
            return;
        }

        //Fill phasing parameters when mandatory approval is enabled
        if (requestType != "approveTransaction"
            && NRS.accountInfo.accountControls && $.inArray('PHASING_ONLY', NRS.accountInfo.accountControls) > -1
                && NRS.accountInfo.phasingOnly
                && NRS.accountInfo.phasingOnly.votingModel >= 0) {

            var phasingControl = NRS.accountInfo.phasingOnly;
            var maxFees = new BigInteger(phasingControl.maxFees);
            if (maxFees > 0 && new BigInteger(data.feeNQT).compareTo(new BigInteger(phasingControl.maxFees)) > 0) {
                callback({
                    "errorCode": 1,
                    "errorDescription": $.t("error_fee_exceeds_max_account_control_fee", {
                        "maxFee": NRS.convertToNXT(phasingControl.maxFees)
                    })
                });
                return;
            }
            var phasingDuration = parseInt(data.phasingFinishHeight) - NRS.lastBlockHeight;
            var minDuration = parseInt(phasingControl.minDuration) > 0 ? parseInt(phasingControl.minDuration) : 0;
            var maxDuration = parseInt(phasingControl.maxDuration) > 0 ? parseInt(phasingControl.maxDuration) : NRS.constants.SERVER.maxPhasingDuration;

            if (phasingDuration < minDuration || phasingDuration > maxDuration) {
                callback({
                    "errorCode": 1,
                    "errorDescription": $.t("error_finish_height_out_of_account_control_interval", {
                        "min": NRS.lastBlockHeight + minDuration,
                        "max": NRS.lastBlockHeight + maxDuration
                    })
                });
                return;
            }

            var phasingParams = NRS.phasingControlObjectToPhasingParams(phasingControl);
            $.extend(data, phasingParams);
            data.phased = true;

            delete data.phasingHashedSecret;
            delete data.phasingHashedSecretAlgorithm;
            delete data.phasingLinkedFullHash;
        }

        if (!data.recipientPublicKey) {
            delete data.recipientPublicKey;
        }
        if (!data.referencedTransactionFullHash) {
            delete data.referencedTransactionFullHash;
        }

        //gets account id from passphrase client side, used only for login.
        var accountId;
        if (requestType == "getAccountId") {
            accountId = NRS.getAccountId(data.secretPhrase);

            var nxtAddress = new NxtAddress();
            var accountRS = "";
            if (nxtAddress.set(accountId)) {
                accountRS = nxtAddress.toString();
            }
            callback({
                "account": accountId,
                "accountRS": accountRS
            });
            return;
        }
        //check to see if secretPhrase supplied matches logged in account, if not - show error.
        if ("secretPhrase" in data) {
            accountId = NRS.getAccountId(NRS.rememberPassword ? _password : data.secretPhrase);
            if (accountId != NRS.account && !data.calculateFee) {
                callback({
                    "errorCode": 1,
                    "errorDescription": $.t("error_passphrase_incorrect")
                });
            } else {
                //ok, accountId matches..continue with the real request.
                NRS.processAjaxRequest(requestType, data, callback, options);
            }
        } else {
            NRS.processAjaxRequest(requestType, data, callback, options);
        }
    };

    function isVolatileRequest(doNotSign, type, requestType, secretPhrase) {
        if (secretPhrase && NRS.isMobileApp()) {
            return true;
        }
        return (NRS.isPassphraseAtRisk() || doNotSign) && type == "POST" && !NRS.isSubmitPassphrase(requestType);
    }

    NRS.processAjaxRequest = function (requestType, data, callback, options) {
        var extra = null;
        if (data["_extra"]) {
            extra = data["_extra"];
            delete data["_extra"];
        }
        var currentPage = null;
        var currentSubPage = null;

        //means it is a page request, not a global request.. Page requests can be aborted.
        if (requestType.slice(-1) == "+") {
            requestType = requestType.slice(0, -1);
            currentPage = NRS.currentPage;
        } else {
            //not really necessary... we can just use the above code..
            var plusCharacter = requestType.indexOf("+");

            if (plusCharacter > 0) {
                requestType = requestType.substr(0, plusCharacter);
                currentPage = NRS.currentPage;
            }
        }

        if (currentPage && NRS.currentSubPage) {
            currentSubPage = NRS.currentSubPage;
        }

        var type = (NRS.isRequirePost(requestType) || "secretPhrase" in data || "doNotSign" in data || "adminPassword" in data ? "POST" : "GET");
        var url;
        if (options.remoteNode) {
            url = options.remoteNode.getUrl() + "/nxt";
        } else {
            url = NRS.getRequestPath(options.noProxy);
        }
        url += "?requestType=" + requestType;

        if (type == "GET") {
            if (typeof data == "string") {
                data += "&random=" + Math.random();
            } else {
                data.random = Math.random();
            }
        }

        if ((NRS.isRequirePost(requestType) || "secretPhrase" in data) &&
            NRS.isRequireBlockchain(requestType) && NRS.accountInfo.errorCode && NRS.accountInfo.errorCode == 5) {
            callback({
                "errorCode": 2,
                "errorDescription": $.t("error_new_account")
            }, data);
            return;
        }

        if (data.referencedTransactionFullHash) {
            if (!/^[a-z0-9]{64}$/.test(data.referencedTransactionFullHash)) {
                callback({
                    "errorCode": -1,
                    "errorDescription": $.t("error_invalid_referenced_transaction_hash")
                }, data);
                return;
            }
        }

        var secretPhrase = "";
        var isVolatile = isVolatileRequest(data.doNotSign, type, requestType, data.secretPhrase);
        if (isVolatile) {
            if (NRS.rememberPassword) {
                secretPhrase = _password;
            } else {
                secretPhrase = data.secretPhrase;
            }

            delete data.secretPhrase;

            if (NRS.accountInfo && NRS.accountInfo.publicKey) {
                data.publicKey = NRS.accountInfo.publicKey;
            } else if (!data.doNotSign && secretPhrase) {
                data.publicKey = NRS.generatePublicKey(secretPhrase);
                NRS.accountInfo.publicKey = data.publicKey;
            }
            var ecBlock = NRS.getECBlock(NRS.isTestNet);
            data.ecBlockId = ecBlock.id;
            data.ecBlockHeight = ecBlock.height;
        } else if (type == "POST" && NRS.rememberPassword) {
            data.secretPhrase = _password;
        }

        $.support.cors = true;
        // Used for passing row query string which is too long for a GET request
        if (data.querystring) {
            data = data.querystring;
            type = "POST";
        }
        var contentType;
        var processData;
        var formData = null;

        var config = NRS.getFileUploadConfig(requestType, data);
        if (config && $(config.selector)[0] && $(config.selector)[0].files[0]) {
            // inspired by http://stackoverflow.com/questions/5392344/sending-multipart-formdata-with-jquery-ajax
            contentType = false;
            processData = false;
            formData = new FormData();
            var file;
            if (data.messageFile) {
                file = data.messageFile;
                delete data.messageFile;
                delete data.encrypt_message;
            } else {
                file = $(config.selector)[0].files[0];
            }
            if (!file && requestType == "uploadTaggedData") {
                callback({
                    "errorCode": 3,
                    "errorDescription": $.t("error_no_file_chosen")
                }, data);
                return;
            }
            if (file && file.size > config.maxSize) {
                callback({
                    "errorCode": 3,
                    "errorDescription": $.t(config.errorDescription, {
                        "size": file.size,
                        "allowed": config.maxSize
                    })
                }, data);
                return;
            }
            type = "POST";
            formData.append(config.requestParam, file);
            for (var key in data) {
                if (!data.hasOwnProperty(key)) {
                    continue;
                }
                formData.append(key, data[key]);
            }
        } else {
            // JQuery defaults
            contentType = "application/x-www-form-urlencoded; charset=UTF-8";
            processData = true;
        }
        NRS.logConsole("Send request " + requestType + " to url " + url);

        $.ajax({
            url: url,
            crossDomain: true,
            dataType: "json",
            type: type,
            timeout: 30000,
            async: (options.isAsync === undefined ? true : options.isAsync),
            currentPage: currentPage,
            currentSubPage: currentSubPage,
            shouldRetry: (type == "GET" ? 2 : undefined),
            traditional: true,
            data: (formData != null ? formData : data),
            contentType: contentType,
            processData: processData
        }).done(function (response) {
            if (typeof data == "string") {
                data = { "querystring": data };
                if (extra) {
                    data["_extra"] = extra;
                }
            }
            if (!options.remoteNode && NRS.isConfirmResponse() &&
                !(response.errorCode || response.errorDescription || response.errorMessage || response.error)) {
                var requestRemoteNode = NRS.isMobileApp() ? NRS.getRemoteNode() : {address: "localhost", announcedAddress: "localhost"}; //TODO unify getRemoteNode with apiProxyPeer
                NRS.confirmResponse(requestType, data, response, requestRemoteNode);
            }
            if (!options.doNotEscape) {
                NRS.escapeResponseObjStrings(response);
            }
            if (NRS.console) {
                NRS.addToConsole(this.url, this.type, this.data, response);
            }
            addAddressData(data);
            if (secretPhrase && response.unsignedTransactionBytes && !data.doNotSign && !response.errorCode && !response.error) {
                var publicKey = NRS.generatePublicKey(secretPhrase);
                var signature = NRS.signBytes(response.unsignedTransactionBytes, converters.stringToHexString(secretPhrase));

                if (!NRS.verifySignature(signature, response.unsignedTransactionBytes, publicKey, callback)) {
                    return;
                }
                addMissingData(data);
                if (file) {
                    var r = new FileReader();
                    r.onload = function (e) {
                        data.filebytes = e.target.result;
                        data.filename = file.name;
                        NRS.verifyAndSignTransactionBytes(response.unsignedTransactionBytes, signature, requestType, data, callback, response, extra, isVolatile);
                    };
                    r.readAsArrayBuffer(file);
                } else {
                    NRS.verifyAndSignTransactionBytes(response.unsignedTransactionBytes, signature, requestType, data, callback, response, extra, isVolatile);
                }
            } else {
                if (response.errorCode || response.errorDescription || response.errorMessage || response.error) {
                    response.errorDescription = NRS.translateServerError(response);
                    delete response.fullHash;
                    if (!response.errorCode) {
                        response.errorCode = -1;
                    }
                    callback(response, data);
                } else {
                    if (response.broadcasted == false && !data.calculateFee) {
                        async.waterfall([
                            function (callback) {
                                addMissingData(data);
                                if (!response.unsignedTransactionBytes) {
                                    callback(null);
                                }
                                if (file) {
                                    var r = new FileReader();
                                    r.onload = function (e) {
                                        data.filebytes = e.target.result;
                                        data.filename = file.name;
                                        callback(null);
                                    };
                                    r.readAsArrayBuffer(file);
                                } else {
                                    callback(null);
                                }
                            },
                            function (callback) {
                                if (response.unsignedTransactionBytes &&
                                    !NRS.verifyTransactionBytes(converters.hexStringToByteArray(response.unsignedTransactionBytes), requestType, data, response.transactionJSON.attachment, isVolatile)) {
                                    callback({
                                        "errorCode": 1,
                                        "errorDescription": $.t("error_bytes_validation_server")
                                    }, data);
                                    return;
                                }
                                callback(null);
                            }
                        ], function () {
                            NRS.showRawTransactionModal(response);
                        });
                    } else {
                        if (extra) {
                            data["_extra"] = extra;
                        }
                        callback(response, data);
                        if (data.referencedTransactionFullHash && !response.errorCode) {
                            $.growl($.t("info_referenced_transaction_hash"), {
                                "type": "info"
                            });
                        }
                    }
                }
            }
        }).fail(function (xhr, textStatus, error) {
            NRS.logConsole("Node " + (options.remoteNode ? options.remoteNode.getUrl() : NRS.getRemoteNodeUrl()) + " received an error for request type " + requestType +
                " status " + textStatus + " error " + error);
            if (NRS.console) {
                NRS.addToConsole(this.url, this.type, this.data, error, true);
            }

            if ((error == "error" || textStatus == "error") && (xhr.status == 404 || xhr.status == 0)) {
                if (type == "POST") {
                    NRS.connectionError();
                }
            }

            if (error != "abort") {
                if (options.remoteNode) {
                    options.remoteNode.blacklist();
                } else {
                    NRS.resetRemoteNode(true);
                }
                if (error == "timeout") {
                    error = $.t("error_request_timeout");
                }
                callback({
                    "errorCode": -1,
                    "errorDescription": error
                }, {});
            }
        });
    };

    NRS.verifyAndSignTransactionBytes = function (transactionBytes, signature, requestType, data, callback, response, extra, isVerifyECBlock) {
        var byteArray = converters.hexStringToByteArray(transactionBytes);
        if (!NRS.verifyTransactionBytes(byteArray, requestType, data, response.transactionJSON.attachment, isVerifyECBlock)) {
            callback({
                "errorCode": 1,
                "errorDescription": $.t("error_bytes_validation_server")
            }, data);
            return;
        }
        var payload = transactionBytes.substr(0, 192) + signature + transactionBytes.substr(320);
        if (data.broadcast == "false") {
            response.transactionBytes = payload;
            response.transactionJSON.signature = signature;
            NRS.showRawTransactionModal(response);
        } else {
            if (extra) {
                data["_extra"] = extra;
            }
            NRS.broadcastTransactionBytes(payload, callback, response, data);
        }
    };

    NRS.verifyTransactionBytes = function (byteArray, requestType, data, attachment, isVerifyECBlock) {
        var transaction = {};
        transaction.type = byteArray[0];
        transaction.version = (byteArray[1] & 0xF0) >> 4;
        transaction.subtype = byteArray[1] & 0x0F;
        transaction.timestamp = String(converters.byteArrayToSignedInt32(byteArray, 2));
        transaction.deadline = String(converters.byteArrayToSignedShort(byteArray, 6));
        transaction.publicKey = converters.byteArrayToHexString(byteArray.slice(8, 40));
        transaction.recipient = String(converters.byteArrayToBigInteger(byteArray, 40));
        transaction.amountNQT = String(converters.byteArrayToBigInteger(byteArray, 48));
        transaction.feeNQT = String(converters.byteArrayToBigInteger(byteArray, 56));

        var refHash = byteArray.slice(64, 96);
        transaction.referencedTransactionFullHash = converters.byteArrayToHexString(refHash);
        if (transaction.referencedTransactionFullHash == "0000000000000000000000000000000000000000000000000000000000000000") {
            transaction.referencedTransactionFullHash = "";
        }
        transaction.flags = 0;
        if (transaction.version > 0) {
            transaction.flags = converters.byteArrayToSignedInt32(byteArray, 160);
            transaction.ecBlockHeight = String(converters.byteArrayToSignedInt32(byteArray, 164));
            transaction.ecBlockId = String(converters.byteArrayToBigInteger(byteArray, 168));
            if (isVerifyECBlock) {
                var ecBlock = NRS.getECBlock(NRS.isTestNet);
                if (transaction.ecBlockHeight != ecBlock.height) {
                    return false;
                }
                if (transaction.ecBlockId != ecBlock.id) {
                    return false;
                }
            }
        }

        if (!(transaction.type==0 && transaction.subtype == 1) && transaction.publicKey != NRS.accountInfo.publicKey && transaction.publicKey != data.publicKey) {
            return false;
        }

        if (transaction.deadline !== data.deadline) {
            return false;
        }
        
        if (transaction.recipient !== data.recipient) {
        
            if ((transaction.type==0 && transaction.subtype==1) || ((data.recipient == NRS.constants.GENESIS || data.recipient == "") && transaction.recipient == "0")) {
                
                //ok
                
            } else {
                
                return false;
                    
            }
        }

        if (transaction.amountNQT !== data.amountNQT) {
            return false;
        }

        if ("referencedTransactionFullHash" in data) {
            if (transaction.referencedTransactionFullHash !== data.referencedTransactionFullHash) {
                return false;
            }
        } else if (transaction.referencedTransactionFullHash !== "") {
            return false;
        }
        var pos;
        if (transaction.version > 0) {
            //has empty attachment, so no attachmentVersion byte...
            if (requestType == "sendMoney" || requestType == "sendMessage") {
                pos = 176;
            } else {
                pos = 177;
            }
        } else {
            pos = 160;
        }
        return NRS.verifyTransactionTypes(byteArray, transaction, requestType, data, pos, attachment);
    };

    NRS.verifyTransactionTypes = function (byteArray, transaction, requestType, data, pos, attachment) {
        var length = 0;
        var i=0;
        var serverHash, sha256, utfBytes, isText, hashWords, calculatedHash;
        switch (requestType) {
            
            case "redeem":
                if (transaction.type !== 0 || transaction.subtype !== 1) {
                    return false;
                }
                break;
            case "sendMoney":
                if (transaction.type !== 0 || transaction.subtype !== 0) {
                    return false;
                }
                break;
            case "sendMessage":
                if (transaction.type !== 1 || transaction.subtype !== 0) {
                    return false;
                }
                break;
            case "createPoll":
                if (transaction.type !== 1 || transaction.subtype !== 1) {
                    return false;
                }
                length = converters.byteArrayToSignedShort(byteArray, pos);
                pos += 2;
                transaction.name = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                length = converters.byteArrayToSignedShort(byteArray, pos);
                pos += 2;
                transaction.description = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                transaction.finishHeight = converters.byteArrayToSignedInt32(byteArray, pos);
                pos += 4;
                var nr_options = byteArray[pos];
                pos++;

                for (i = 0; i < nr_options; i++) {
                    var optionLength = converters.byteArrayToSignedShort(byteArray, pos);
                    pos += 2;
                    transaction["option" + (i < 10 ? "0" + i : i)] = converters.byteArrayToString(byteArray, pos, optionLength);
                    pos += optionLength;
                }
                transaction.votingModel = String(byteArray[pos]);
                pos++;
                transaction.minNumberOfOptions = String(byteArray[pos]);
                pos++;
                transaction.maxNumberOfOptions = String(byteArray[pos]);
                pos++;
                transaction.minRangeValue = String(byteArray[pos]);
                pos++;
                transaction.maxRangeValue = String(byteArray[pos]);
                pos++;
                transaction.minBalance = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.minBalanceModel = String(byteArray[pos]);
                pos++;
                transaction.holding = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;

                if (transaction.name !== data.name || transaction.description !== data.description ||
                    transaction.minNumberOfOptions !== data.minNumberOfOptions || transaction.maxNumberOfOptions !== data.maxNumberOfOptions) {
                    return false;
                }

                for (i = 0; i < nr_options; i++) {
                    if (transaction["option" + (i < 10 ? "0" + i : i)] !== data["option" + (i < 10 ? "0" + i : i)]) {
                        return false;
                    }
                }

                if (("option" + (i < 10 ? "0" + i : i)) in data) {
                    return false;
                }
                break;
            case "castVote":
                if (transaction.type !== 1 || transaction.subtype !== 2) {
                    return false;
                }
                transaction.poll = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                var voteLength = byteArray[pos];
                pos++;
                transaction.votes = [];

                for (i = 0; i < voteLength; i++) {
                    transaction["vote" + (i < 10 ? "0" + i : i)] = byteArray[pos];
                    pos++;
                }
                if (transaction.poll !== data.poll) {
                    return false;
                }
                break;
            case "hubAnnouncement":
                if (transaction.type !== 1 || transaction.subtype != 3) {
                    return false;
                }
                return false;
                break;
            case "setAccountInfo":
                if (transaction.type !== 1 || transaction.subtype != 4) {
                    return false;
                }
                length = parseInt(byteArray[pos], 10);
                pos++;
                transaction.name = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                length = converters.byteArrayToSignedShort(byteArray, pos);
                pos += 2;
                transaction.description = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                if (transaction.name !== data.name || transaction.description !== data.description) {
                    return false;
                }
                break;
            
            case "approveTransaction":
                if (transaction.type !== 1 && transaction.subtype !== 5) {
                    return false;
                }
                var fullHashesLength = byteArray[pos];
                if (fullHashesLength !== 1) {
                    return false;
                }
                pos++;
                transaction.transactionFullHash = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
                pos += 32;
                if (transaction.transactionFullHash !== data.transactionFullHash) {
                    return false;
                }
                transaction.revealedSecretLength = converters.byteArrayToSignedInt32(byteArray, pos);
                pos += 4;
                if (transaction.revealedSecretLength > 0) {
                    transaction.revealedSecret = converters.byteArrayToHexString(byteArray.slice(pos, pos + transaction.revealedSecretLength));
                    pos += transaction.revealedSecretLength;
                }
                if (transaction.revealedSecret !== data.revealedSecret &&
                    transaction.revealedSecret !== converters.byteArrayToHexString(NRS.getUtf8Bytes(data.revealedSecretText))) {
                    return false;
                }
                break;
            
            case "leaseBalance":
                if (transaction.type !== 2 && transaction.subtype !== 0) {
                    return false;
                }
                transaction.period = String(converters.byteArrayToSignedShort(byteArray, pos));
                pos += 2;
                if (transaction.period !== data.period) {
                    return false;
                }
                break;
            case "setPhasingOnlyControl":
                if (transaction.type !== 2 && transaction.subtype !== 1) {
                    return false;
                }
                return validateCommonPhasingData(byteArray, pos, data, "control") != -1;
                break;
            
            case "uploadTaggedData":
                if (transaction.type !== 3 && transaction.subtype !== 0) {
                    return false;
                }
                serverHash = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
                pos += 32;
                sha256 = CryptoJS.algo.SHA256.create();
                utfBytes = NRS.getUtf8Bytes(data.name);
                sha256.update(converters.byteArrayToWordArrayEx(utfBytes));
                utfBytes = NRS.getUtf8Bytes(data.description);
                sha256.update(converters.byteArrayToWordArrayEx(utfBytes));
                utfBytes = NRS.getUtf8Bytes(data.tags);
                sha256.update(converters.byteArrayToWordArrayEx(utfBytes));
                utfBytes = NRS.getUtf8Bytes(attachment.type);
                sha256.update(converters.byteArrayToWordArrayEx(utfBytes));
                utfBytes = NRS.getUtf8Bytes(data.channel);
                sha256.update(converters.byteArrayToWordArrayEx(utfBytes));
                isText = [];
                if (attachment.isText) {
                    isText.push(1);
                } else {
                    isText.push(0);
                }
                sha256.update(converters.byteArrayToWordArrayEx(isText));
                utfBytes = NRS.getUtf8Bytes(data.filename);
                sha256.update(converters.byteArrayToWordArrayEx(utfBytes));
                var dataBytes = new Int8Array(data.filebytes);
                sha256.update(converters.byteArrayToWordArrayEx(dataBytes));
                hashWords = sha256.finalize();
                calculatedHash = converters.wordArrayToByteArrayEx(hashWords);
                if (serverHash !== converters.byteArrayToHexString(calculatedHash)) {
                    return false;
                }
                break;
            case "extendTaggedData":
                if (transaction.type !== 3 && transaction.subtype !== 1) {
                    return false;
                }
                transaction.taggedDataId = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.taggedDataId !== data.transaction) {
                    return false;
                }
                break;
            
            default:
                //invalid requestType..
                return false;
        }

        var position = 1;
        var attachmentVersion;
        //non-encrypted message
        if ((transaction.flags & position) != 0 ||
            ((requestType == "sendMessage" && data.message && !(data.messageIsPrunable === "true")))) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return false;
            }
            pos++;
            var messageLength = converters.byteArrayToSignedInt32(byteArray, pos);
            transaction.messageIsText = messageLength < 0; // ugly hack??
            if (messageLength < 0) {
                messageLength &= NRS.constants.MAX_INT_JAVA;
            }
            pos += 4;
            if (transaction.messageIsText) {
                transaction.message = converters.byteArrayToString(byteArray, pos, messageLength);
            } else {
                var slice = byteArray.slice(pos, pos + messageLength);
                transaction.message = converters.byteArrayToHexString(slice);
            }
            pos += messageLength;
            var messageIsText = (transaction.messageIsText ? "true" : "false");
            if (messageIsText != data.messageIsText) {
                return false;
            }
            if (transaction.message !== data.message) {
                return false;
            }
        } else if (data.message && !(data.messageIsPrunable === "true")) {
            return false;
        }

        position <<= 1;

        //encrypted note
        if ((transaction.flags & position) != 0) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return false;
            }
            pos++;
            var encryptedMessageLength = converters.byteArrayToSignedInt32(byteArray, pos);
            transaction.messageToEncryptIsText = encryptedMessageLength < 0;
            if (encryptedMessageLength < 0) {
                encryptedMessageLength &= NRS.constants.MAX_INT_JAVA;
            }
            pos += 4;
            transaction.encryptedMessageData = converters.byteArrayToHexString(byteArray.slice(pos, pos + encryptedMessageLength));
            pos += encryptedMessageLength;
            transaction.encryptedMessageNonce = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
            pos += 32;
            var messageToEncryptIsText = (transaction.messageToEncryptIsText ? "true" : "false");
            if (messageToEncryptIsText != data.messageToEncryptIsText) {
                return false;
            }
            if (transaction.encryptedMessageData !== data.encryptedMessageData || transaction.encryptedMessageNonce !== data.encryptedMessageNonce) {
                return false;
            }
        } else if (data.encryptedMessageData && !(data.encryptedMessageIsPrunable === "true")) {
            return false;
        }

        position <<= 1;

        if ((transaction.flags & position) != 0 && requestType != "redeem") {
            
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return false;
            }
            pos++;
            var recipientPublicKey = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
            if (recipientPublicKey != data.recipientPublicKey) {
                return false;
            }
            pos += 32;
            
        } else if (data.recipientPublicKey && requestType != "redeem") {

            return false;

        }

        position <<= 1;

        if ((transaction.flags & position) != 0) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return false;
            }
            pos++;
            var encryptedToSelfMessageLength = converters.byteArrayToSignedInt32(byteArray, pos);
            transaction.messageToEncryptToSelfIsText = encryptedToSelfMessageLength < 0;
            if (encryptedToSelfMessageLength < 0) {
                encryptedToSelfMessageLength &= NRS.constants.MAX_INT_JAVA;
            }
            pos += 4;
            transaction.encryptToSelfMessageData = converters.byteArrayToHexString(byteArray.slice(pos, pos + encryptedToSelfMessageLength));
            pos += encryptedToSelfMessageLength;
            transaction.encryptToSelfMessageNonce = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
            pos += 32;
            var messageToEncryptToSelfIsText = (transaction.messageToEncryptToSelfIsText ? "true" : "false");
            if (messageToEncryptToSelfIsText != data.messageToEncryptToSelfIsText) {
                return false;
            }
            if (transaction.encryptToSelfMessageData !== data.encryptToSelfMessageData || transaction.encryptToSelfMessageNonce !== data.encryptToSelfMessageNonce) {
                return false;
            }
        } else if (data.encryptToSelfMessageData) {
            return false;
        }

        position <<= 1;

        if ((transaction.flags & position) != 0) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return false;
            }
            pos++;
            if (String(converters.byteArrayToSignedInt32(byteArray, pos)) !== data.phasingFinishHeight) {
                return false;
            }
            pos += 4;
            pos = validateCommonPhasingData(byteArray, pos, data, "phasing");
            if (pos == -1) {
                return false;
            }
            var linkedFullHashesLength = byteArray[pos];
            pos++;
            for (i = 0; i < linkedFullHashesLength; i++) {
                var fullHash = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
                pos += 32;
                if (fullHash !== data.phasingLinkedFullHash[i]) {
                    return false;
                }
            }
            var hashedSecretLength = byteArray[pos];
            pos++;
            if (hashedSecretLength > 0 && converters.byteArrayToHexString(byteArray.slice(pos, pos + hashedSecretLength)) !== data.phasingHashedSecret) {
                return false;
            }
            pos += hashedSecretLength;
            var algorithm = String(byteArray[pos]);
            if (algorithm !== "0" && algorithm !== data.phasingHashedSecretAlgorithm) {
                return false;
            }
            pos++;
        }

        position <<= 1;

        if ((transaction.flags & position) != 0) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return false;
            }
            pos++;
            serverHash = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
            pos += 32;
            sha256 = CryptoJS.algo.SHA256.create();
            isText = [];
            if (data.messageIsText == "true") {
                isText.push(1);
            } else {
                isText.push(0);
            }
            sha256.update(converters.byteArrayToWordArrayEx(isText));
            if (data.filebytes) {
                utfBytes = new Int8Array(data.filebytes);
            } else {
                utfBytes = NRS.getUtf8Bytes(data.message);
            }
            sha256.update(converters.byteArrayToWordArrayEx(utfBytes));
            hashWords = sha256.finalize();
            calculatedHash = converters.wordArrayToByteArrayEx(hashWords);
            if (serverHash !== converters.byteArrayToHexString(calculatedHash)) {
                return false;
            }
        }
        position <<= 1;

        if ((transaction.flags & position) != 0) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return false;
            }
            pos++;
            serverHash = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
            sha256 = CryptoJS.algo.SHA256.create();
            if (data.messageToEncryptIsText == "true") {
                sha256.update(converters.byteArrayToWordArrayEx([1]));
            } else {
                sha256.update(converters.byteArrayToWordArrayEx([0]));
            }
            sha256.update(converters.byteArrayToWordArrayEx([1])); // compression
            if (data.filebytes) {
                utfBytes = new Int8Array(data.filebytes);
            } else {
                utfBytes = converters.hexStringToByteArray(data.encryptedMessageData);
            }
            sha256.update(converters.byteArrayToWordArrayEx(utfBytes));
            sha256.update(converters.byteArrayToWordArrayEx(converters.hexStringToByteArray(data.encryptedMessageNonce)));
            hashWords = sha256.finalize();
            calculatedHash = converters.wordArrayToByteArrayEx(hashWords);
            if (serverHash !== converters.byteArrayToHexString(calculatedHash)) {
                return false;
            }
        }

        return true;
    };

    NRS.broadcastTransactionBytes = function (transactionData, callback, originalResponse, originalData) {
        var requestType = NRS.state.apiProxy ? "sendTransaction": "broadcastTransaction";
        $.ajax({
            url: NRS.getRequestPath() + "?requestType=" + requestType,
            crossDomain: true,
            dataType: "json",
            type: "POST",
            timeout: 30000,
            async: true,
            data: {
                "transactionBytes": transactionData,
                "prunableAttachmentJSON": JSON.stringify(originalResponse.transactionJSON.attachment),
                "adminPassword": NRS.settings.admin_password
            }
        }).done(function (response) {
            NRS.escapeResponseObjStrings(response);
            if (NRS.console) {
                NRS.addToConsole(this.url, this.type, this.data, response);
            }

            if (response.errorCode) {
                if (!response.errorDescription) {
                    response.errorDescription = (response.errorMessage ? response.errorMessage : "Unknown error occurred.");
                }
                callback(response, originalData);
            } else if (response.error) {
                response.errorCode = 1;
                response.errorDescription = response.error;
                callback(response, originalData);
            } else {
                if ("transactionBytes" in originalResponse) {
                    delete originalResponse.transactionBytes;
                }
                originalResponse.broadcasted = true;
                originalResponse.transaction = response.transaction;
                originalResponse.fullHash = response.fullHash;
                callback(originalResponse, originalData);
                if (originalData.referencedTransactionFullHash) {
                    $.growl($.t("info_referenced_transaction_hash"), {
                        "type": "info"
                    });
                }
            }
        }).fail(function (xhr, textStatus, error) {
            NRS.logConsole("request failed, status: " + textStatus + ", error: " + error);
            if (NRS.console) {
                NRS.addToConsole(this.url, this.type, this.data, error, true);
            }
            NRS.resetRemoteNode(true);
            if (error == "timeout") {
                error = $.t("error_request_timeout");
            }
            callback({
                "errorCode": -1,
                "errorDescription": error
            }, {});
        });
    };

    NRS.generateQRCode = function(target, qrCodeData, minType) {
        var type = minType ? minType : 2;
        while (type <= 40) {
            try {
                var qr = qrcode(type, 'M');
                qr.addData(qrCodeData);
                qr.make();
                var img = qr.createImgTag();
                $(target).empty().append(img);
                NRS.logConsole("Encoded QR code of type " + type);
                return;
            } catch (e) {
                type++;
            }
        }
        $(target).empty().html($.t("cannot_encode_message", qrCodeData.length));
    };

    function addAddressData(data) {
        if (typeof data == "object" && ("recipient" in data)) {
            var address = new NxtAddress();
            if (/^XEL\-/i.test(data.recipient)) {
                data.recipientRS = data.recipient;
                if (address.set(data.recipient)) {
                    data.recipient = address.account_id();
                }
            } else {
                if (address.set(data.recipient)) {
                    data.recipientRS = address.toString();
                }
            }
        }
    }

    function addMissingData(data) {
        if (!("amountNQT" in data)) {
            data.amountNQT = "0";
        }
        if (!("recipient" in data)) {
            data.recipient = NRS.constants.GENESIS;
            data.recipientRS = NRS.constants.GENESIS_RS;
        }
    }

    function validateCommonPhasingData(byteArray, pos, data, prefix) {
        if (byteArray[pos] != (parseInt(data[prefix + "VotingModel"]) & 0xFF)) {
            return -1;
        }
        pos++;
        var quorum = String(converters.byteArrayToBigInteger(byteArray, pos));
        if (quorum !== "0" && quorum !== String(data[prefix + "Quorum"])) {
            return -1;
        }
        pos += 8;
        var minBalance = String(converters.byteArrayToBigInteger(byteArray, pos));
        if (minBalance !== "0" && minBalance !== data[prefix + "MinBalance"]) {
            return -1;
        }
        pos += 8;
        var whiteListLength = byteArray[pos];
        pos++;
        for (i = 0; i < whiteListLength; i++) {
            var accountId = converters.byteArrayToBigInteger(byteArray, pos);
            var accountRS = NRS.convertNumericToRSAccountFormat(accountId);
            pos += 8;
            if (String(accountId) !== data[prefix + "Whitelisted"][i] && String(accountRS) !== data[prefix + "Whitelisted"][i]) {
                return -1;
            }
        }
        var holdingId = String(converters.byteArrayToBigInteger(byteArray, pos));
        if (holdingId !== "0" && holdingId !== data[prefix + "Holding"]) {
            return -1;
        }
        pos += 8;
        var minBalanceModel = String(byteArray[pos]);
        if (minBalanceModel !== "0" && minBalanceModel !== String(data[prefix + "MinBalanceModel"])) {
            return -1;
        }
        pos++;
        return pos;
    }

    return NRS;
}(Object.assign(NRS || {}, isNode ? global.client : {}), jQuery));

if (isNode) {
    module.exports = NRS;
}
