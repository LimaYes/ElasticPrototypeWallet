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
 * @depends {nrs.modals.js}
 */
var NRS = (function (NRS, $, undefined) {
    $('body').on("click", ".show_transaction_modal_action", function (e) {
        e.preventDefault();

        var transactionId = $(this).data("transaction");
        var sharedKey = $(this).data("sharedkey");
        var infoModal = $('#transaction_info_modal');
        var isModalVisible = false;
        if (infoModal && infoModal.data('bs.modal')) {
            isModalVisible = infoModal.data('bs.modal').isShown;
        }
        if ($(this).data("back") == "true") {
            NRS.modalStack.pop(); // The forward modal
            NRS.modalStack.pop(); // the current modal
        }
        NRS.showTransactionModal(transactionId, isModalVisible, sharedKey);
    });

    NRS.showTransactionModal = function (transaction, isModalVisible, sharedKey) {
        if (NRS.fetchingModalData) {
            return;
        }

        NRS.fetchingModalData = true;

        $("#transaction_info_output_top, #transaction_info_output_bottom, #transaction_info_bottom").html("").hide();
        $("#transaction_info_callout").hide();
        var infoTable = $("#transaction_info_table");
        infoTable.hide();
        infoTable.find("tbody").empty();

        try {
            if (typeof transaction != "object") {
                NRS.sendRequest("getTransaction", {
                    "transaction": transaction
                }, function (response, input) {
                    response.transaction = input.transaction;
                    NRS.processTransactionModalData(response, isModalVisible, sharedKey);
                });
            } else {
                NRS.processTransactionModalData(transaction, isModalVisible, sharedKey);
            }
        } catch (e) {
            NRS.fetchingModalData = false;
            throw e;
        }
    };

    NRS.getPhasingDetails = function(phasingDetails, phasingParams) {
        var votingModel = NRS.getVotingModelName(parseInt(phasingParams.phasingVotingModel));
        phasingDetails.votingModel = $.t(votingModel);
        switch (votingModel) {
            case 'ASSET':
                NRS.sendRequest("getAsset", { "asset": phasingParams.phasingHolding }, function(response) {
                    phasingDetails.quorum = NRS.convertToQNTf(phasingParams.phasingQuorum, response.decimals);
                    phasingDetails.minBalance = NRS.convertToQNTf(phasingParams.phasingMinBalance, response.decimals);
                }, { isAsync: false });
                break;
            case 'CURRENCY':
                NRS.sendRequest("getCurrency", { "currency": phasingParams.phasingHolding }, function(response) {
                    phasingDetails.quorum = NRS.convertToQNTf(phasingParams.phasingQuorum, response.decimals);
                    phasingDetails.minBalance = NRS.convertToQNTf(phasingParams.phasingMinBalance, response.decimals);
                }, { isAsync: false });
                break;
            default:
                phasingDetails.quorum = phasingParams.phasingQuorum;
                phasingDetails.minBalance = phasingParams.phasingMinBalance;
        }
        var phasingTransactionLink = NRS.getTransactionLink(phasingParams.phasingHolding);
        if (NRS.constants.VOTING_MODELS[votingModel] == NRS.constants.VOTING_MODELS.ASSET) {
            phasingDetails.asset_formatted_html = phasingTransactionLink;
        } else if (NRS.constants.VOTING_MODELS[votingModel] == NRS.constants.VOTING_MODELS.CURRENCY) {
            phasingDetails.currency_formatted_html = phasingTransactionLink;
        }
        var minBalanceModel = NRS.getMinBalanceModelName(parseInt(phasingParams.phasingMinBalanceModel));
        phasingDetails.minBalanceModel = $.t(minBalanceModel);
        var rows = "";
        if (phasingParams.phasingWhitelist && phasingParams.phasingWhitelist.length > 0) {
            rows = "<table class='table table-striped'><thead><tr>" +
                "<th>" + $.t("Account") + "</th>" +
                "</tr></thead><tbody>";
            for (var i = 0; i < phasingParams.phasingWhitelist.length; i++) {
                var account = NRS.convertNumericToRSAccountFormat(phasingParams.phasingWhitelist[i]);
                rows += "<tr><td><a href='#' data-user='" + NRS.escapeRespStr(account) + "' class='show_account_modal_action'>" + NRS.getAccountTitle(account) + "</a></td></tr>";
            }
            rows += "</tbody></table>";
        } else {
            rows = "-";
        }
        phasingDetails.whitelist_formatted_html = rows;
        if (phasingParams.phasingLinkedFullHashes && phasingParams.phasingLinkedFullHashes.length > 0) {
            rows = "<table class='table table-striped'><tbody>";
            for (i = 0; i < phasingParams.phasingLinkedFullHashes.length; i++) {
                rows += "<tr><td>" + phasingParams.phasingLinkedFullHashes[i] + "</td></tr>";
            }
            rows += "</tbody></table>";
        } else {
            rows = "-";
        }
        phasingDetails.full_hash_formatted_html = rows;
        if (phasingParams.phasingHashedSecret) {
            phasingDetails.hashedSecret = phasingParams.phasingHashedSecret;
            phasingDetails.hashAlgorithm = NRS.getHashAlgorithm(phasingParams.phasingHashedSecretAlgorithm);
        }
    };

    NRS.processTransactionModalData = function (transaction, isModalVisible, sharedKey) {
        NRS.setBackLink();
        NRS.modalStack.push({ class: "show_transaction_modal_action", key: "transaction", value: transaction.transaction });
        try {
            var async = false;

            var transactionDetails = $.extend({}, transaction);
            delete transactionDetails.attachment;
            if (transactionDetails.referencedTransaction == "0") {
                delete transactionDetails.referencedTransaction;
            }
            delete transactionDetails.transaction;

            if (!transactionDetails.confirmations) {
                transactionDetails.confirmations = "/";
            }
            if (!transactionDetails.block) {
                transactionDetails.block = "unconfirmed";
            }
            if (transactionDetails.timestamp) {
                transactionDetails.transactionTime = NRS.formatTimestamp(transactionDetails.timestamp);
            }
            if (transactionDetails.blockTimestamp) {
                transactionDetails.blockGenerationTime = NRS.formatTimestamp(transactionDetails.blockTimestamp);
            }
            if (transactionDetails.height == NRS.constants.MAX_INT_JAVA) {
                transactionDetails.height = "unknown";
            } else {
                transactionDetails.height_formatted_html = NRS.getBlockLink(transactionDetails.height);
                delete transactionDetails.height;
            }
            $("#transaction_info_modal_transaction").html(NRS.escapeRespStr(transaction.transaction));

            $("#transaction_info_tab_link").tab("show");

            $("#transaction_info_details_table").find("tbody").empty().append(NRS.createInfoTable(transactionDetails, true));
            var infoTable = $("#transaction_info_table");
            infoTable.find("tbody").empty();

            var incorrect = false;
            if (transaction.senderRS == NRS.accountRS) {
                $("#transaction_info_modal_send_money").attr('disabled','disabled');
                $("#transaction_info_modal_transfer_currency").attr('disabled','disabled');
                $("#transaction_info_modal_send_message").attr('disabled','disabled');
            } else {
                $("#transaction_info_modal_send_money").removeAttr('disabled');
                $("#transaction_info_modal_transfer_currency").removeAttr('disabled');
                $("#transaction_info_modal_send_message").removeAttr('disabled');
            }
            var accountButton;
            if (transaction.senderRS in NRS.contacts) {
                accountButton = NRS.contacts[transaction.senderRS].name.escapeHTML();
                $("#transaction_info_modal_add_as_contact").attr('disabled','disabled');
            } else {
                accountButton = transaction.senderRS;
                $("#transaction_info_modal_add_as_contact").removeAttr('disabled');
            }
            var approveTransactionButton = $("#transaction_info_modal_approve_transaction");
            if (!transaction.attachment || !transaction.block ||
                !transaction.attachment.phasingFinishHeight ||
                transaction.attachment.phasingFinishHeight <= NRS.lastBlockHeight) {
                approveTransactionButton.attr('disabled', 'disabled');
            } else {
                approveTransactionButton.removeAttr('disabled');
                approveTransactionButton.data("transaction", transaction.transaction);
                approveTransactionButton.data("fullhash", transaction.fullHash);
                approveTransactionButton.data("timestamp", transaction.timestamp);
                approveTransactionButton.data("minBalanceFormatted", "");
                approveTransactionButton.data("votingmodel", transaction.attachment.phasingVotingModel);
            }
            var extendDataButton = $("#transaction_info_modal_extend_data");
            if (transaction.type == NRS.subtype.TaggedDataUpload.type && transaction.subtype == NRS.subtype.TaggedDataUpload.subtype) {
                extendDataButton.removeAttr('disabled');
                extendDataButton.data("transaction", transaction.transaction);
            } else {
                extendDataButton.attr('disabled','disabled');
            }

            $("#transaction_info_actions").show();
            $("#transaction_info_actions_tab").find("button").data("account", accountButton);

            if (transaction.attachment && transaction.attachment.phasingFinishHeight) {
                var finishHeight = transaction.attachment.phasingFinishHeight;
                var phasingDetails = {};
                phasingDetails.finishHeight = finishHeight;
                phasingDetails.finishIn = ((finishHeight - NRS.lastBlockHeight) > 0) ? (finishHeight - NRS.lastBlockHeight) + " " + $.t("blocks") : $.t("finished");
                NRS.getPhasingDetails(phasingDetails, transaction.attachment);
                $("#phasing_info_details_table").find("tbody").empty().append(NRS.createInfoTable(phasingDetails, true));
                $("#phasing_info_details_link").show();
            } else {
                $("#phasing_info_details_link").hide();
            }
            // TODO Someday I'd like to replace it with if (NRS.isOfType(transaction, "OrdinaryPayment"))
            var data;
            var message;
            var fieldsToDecrypt = {};
            var i;
            if (transaction.type == 0) {
                switch (transaction.subtype) {
                    case 0:
                        data = {
                            "type": $.t("ordinary_payment"),
                            "amount": transaction.amountNQT,
                            "fee": transaction.feeNQT,
                            "recipient": transaction.recipientRS ? transaction.recipientRS : transaction.recipient,
                            "sender": transaction.senderRS ? transaction.senderRS : transaction.sender
                        };

                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 1:
                        data = {
                            "type": $.t("redeem_payment"),
                            "amount": transaction.amountNQT,
                            "fee": transaction.feeNQT,
                            "recipient": transaction.recipientRS ? transaction.recipientRS : transaction.recipient,
                            "sender": transaction.senderRS ? transaction.senderRS : transaction.sender
                        };

                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    default:
                        incorrect = true;
                        break;
                }
            } else if (transaction.type == 1) {
                switch (transaction.subtype) {
                    case 0:
                        var $output = $("#transaction_info_output_top");
                        if (transaction.attachment) {
                            if (transaction.attachment.message) {
                                if (!transaction.attachment["version.Message"] && !transaction.attachment["version.PrunablePlainMessage"]) {
                                    try {
                                        message = converters.hexStringToString(transaction.attachment.message);
                                    } catch (err) {
                                        //legacy
                                        if (transaction.attachment.message.indexOf("feff") === 0) {
                                            message = NRS.convertFromHex16(transaction.attachment.message);
                                        } else {
                                            message = NRS.convertFromHex8(transaction.attachment.message);
                                        }
                                    }
                                } else {
                                    if (transaction.attachment.messageIsText) {
                                        message = String(transaction.attachment.message);
                                    } else {
                                        message = $.t("binary_data");
                                    }
                                }
                                $output.html("<div style='color:#999999;padding-bottom:10px'><i class='fa fa-unlock'></i> " + $.t("public_message") + "</div><div style='padding-bottom:10px'>" + NRS.escapeRespStr(message).nl2br() + "</div>");
                            }

                            if (transaction.attachment.encryptedMessage || (transaction.attachment.encryptToSelfMessage && NRS.account == transaction.sender)) {
                                $output.append("" +
                                    "<div id='transaction_info_decryption_form'></div>" +
                                    "<div id='transaction_info_decryption_output' style='display:none;padding-bottom:10px;'></div>"
                                );
                                if (transaction.attachment.encryptedMessage) {
                                    fieldsToDecrypt.encryptedMessage = $.t("encrypted_message");
                                }
                                if (transaction.attachment.encryptToSelfMessage && NRS.account == transaction.sender) {
                                    fieldsToDecrypt.encryptToSelfMessage = $.t("note_to_self");
                                }
                                var options = {
                                    "noPadding": true,
                                    "formEl": "#transaction_info_decryption_form",
                                    "outputEl": "#transaction_info_decryption_output"
                                };
                                if (sharedKey) {
                                    options["sharedKey"] = sharedKey;
                                }
                                NRS.tryToDecrypt(transaction, fieldsToDecrypt, NRS.getAccountForDecryption(transaction), options);
                            }
                        } else {
                            $output.append("<div style='padding-bottom:10px'>" + $.t("message_empty") + "</div>");
                        }
                        var isCompressed = false;
                        if (transaction.attachment.encryptedMessage) {
                            isCompressed = transaction.attachment.encryptedMessage.isCompressed;
                        } else if (transaction.attachment.encryptToSelfMessage) {
                            isCompressed = transaction.attachment.encryptToSelfMessage.isCompressed;
                        }
                        var hash = transaction.attachment.messageHash || transaction.attachment.encryptedMessageHash;
                        var hashRow = hash ? ("<tr><td><strong>" + $.t("hash") + "</strong>:&nbsp;</td><td>" + hash + "</td></tr>") : "";
                        var downloadLink = "";
                        if (transaction.attachment.messageHash && !NRS.isTextMessage(transaction) && transaction.block) {
                            downloadLink = "<tr><td>" + NRS.getMessageDownloadLink(transaction.transaction, sharedKey) + "</td></tr>";
                        }
                        $output.append("<table>" +
                            "<tr><td><strong>" + $.t("from") + "</strong>:&nbsp;</td><td>" + NRS.getAccountLink(transaction, "sender") + "</td></tr>" +
                            "<tr><td><strong>" + $.t("to") + "</strong>:&nbsp;</td><td>" + NRS.getAccountLink(transaction, "recipient") + "</td></tr>" +
                            "<tr><td><strong>" + $.t("compressed") + "</strong>:&nbsp;</td><td>" + isCompressed + "</td></tr>" +
                            hashRow + downloadLink +
                        "</table>");
                        $output.show();
                        break;

                    case 1:
                        data = {
                            "type": $.t("poll_creation"),
                            "name": transaction.attachment.name,
                            "description": transaction.attachment.description,
                            "finish_height": transaction.attachment.finishHeight,
                            "min_number_of_options": transaction.attachment.minNumberOfOptions,
                            "max_number_of_options": transaction.attachment.maxNumberOfOptions,
                            "min_range_value": transaction.attachment.minRangeValue,
                            "max_range_value": transaction.attachment.maxRangeValue,
                            "min_balance": transaction.attachment.minBalance,
                            "min_balance_model": transaction.attachment.minBalanceModel
                        };

                        if (transaction.attachment.votingModel == -1) {
                            data["voting_model"] = $.t("vote_by_none");
                        } else if (transaction.attachment.votingModel == 0) {
                            data["voting_model"] = $.t("vote_by_account");
                        } else if (transaction.attachment.votingModel == 1) {
                            data["voting_model"] = $.t("vote_by_balance");
                        } else if (transaction.attachment.votingModel == 2) {
                            data["voting_model"] = $.t("vote_by_transaction");
                        } else if (transaction.attachment.votingModel == 3) {
                            data["voting_model"] = $.t("vote_by_hash");
                        } else {
                            data["voting_model"] = transaction.attachment.votingModel;
                        }


                        for (i = 0; i < transaction.attachment.options.length; i++) {
                            data["option_" + i] = transaction.attachment.options[i];
                        }

                        if (transaction.sender != NRS.account) {
                            data["sender"] = NRS.getAccountTitle(transaction, "sender");
                        }

                        data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 2:
                        var vote = "";
                        var votes = transaction.attachment.vote;
                        if (votes && votes.length > 0) {
                            for (i = 0; i < votes.length; i++) {
                                if (votes[i] == -128) {
                                    vote += "N/A";
                                } else {
                                    vote += votes[i];
                                }
                                if (i < votes.length - 1) {
                                    vote += " , ";
                                }
                            }
                        }
                        data = {
                            "type": $.t("vote_casting"),
                            "poll_formatted_html": NRS.getTransactionLink(transaction.attachment.poll),
                            "vote": vote
                        };
                        data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 3:
                        data = {
                            "type": $.t("hub_announcement")
                        };

                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 4:
                        data = {
                            "type": $.t("account_info"),
                            "name": transaction.attachment.name,
                            "description": transaction.attachment.description
                        };

                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 5:
                        data = {
                            "type": $.t("transaction_approval")
                        };
                        for (i = 0; i < transaction.attachment.transactionFullHashes.length; i++) {
                            var transactionBytes = converters.hexStringToByteArray(transaction.attachment.transactionFullHashes[i]);
                            var transactionId = converters.byteArrayToBigInteger(transactionBytes, 0).toString().escapeHTML();
                            data["transaction" + (i + 1) + "_formatted_html"] =
                                NRS.getTransactionLink(transactionId);
                        }

                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;

                    default:
                        incorrect = true;
                        break;
                }
            } else if (transaction.type == 2) {
                switch (transaction.subtype) {
                    case 0:
                        data = {
                            "type": $.t("balance_leasing"),
                            "period": transaction.attachment.period,
                            "lessee": transaction.recipientRS ? transaction.recipientRS : transaction.recipient
                        };

                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 1:
                        data = {
                            "type": $.t("phasing_only")
                        };

                        NRS.getPhasingDetails(data, transaction.attachment.phasingControlParams);

                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;

                    default:
                        incorrect = true;
                        break;
                }
            }
            else if (transaction.type == 3) {
                switch (transaction.subtype) {
                    case 0:
                        data = NRS.getTaggedData(transaction.attachment, 0, transaction);
                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;
                    case 1:
                        data = NRS.getTaggedData(transaction.attachment, 1, transaction);
                        infoTable.find("tbody").append(NRS.createInfoTable(data));
                        infoTable.show();

                        break;

                    default:
                        incorrect = true;
                        break;
                }
            }
            if (!(transaction.type == 1 && transaction.subtype == 0)) {
                if (transaction.attachment) {
                    var transactionInfoOutputBottom = $("#transaction_info_output_bottom");
                    if (transaction.attachment.message) {
                        if (!transaction.attachment["version.Message"] && !transaction.attachment["version.PrunablePlainMessage"]) {
                            try {
                                message = converters.hexStringToString(transaction.attachment.message);
                            } catch (err) {
                                //legacy
                                if (transaction.attachment.message.indexOf("feff") === 0) {
                                    message = NRS.convertFromHex16(transaction.attachment.message);
                                } else {
                                    message = NRS.convertFromHex8(transaction.attachment.message);
                                }
                            }
                        } else {
                            if (NRS.isTextMessage(transaction)) {
                                message = String(transaction.attachment.message);
                            } else {
                                message = $.t("binary_data")
                            }
                        }

                        transactionInfoOutputBottom.append("<div style='padding-left:5px;'><label><i class='fa fa-unlock'></i> " + $.t("public_message") + "</label><div>" + NRS.escapeRespStr(message).nl2br() + "</div></div>");
                    }

                    if (transaction.attachment.encryptedMessage || (transaction.attachment.encryptToSelfMessage && NRS.account == transaction.sender)) {
                        var account;
                        if (transaction.attachment.message) {
                            transactionInfoOutputBottom.append("<div style='height:5px'></div>");
                        }
                        if (transaction.attachment.encryptedMessage) {
                            fieldsToDecrypt.encryptedMessage = $.t("encrypted_message");
                            account = NRS.getAccountForDecryption(transaction);
                        }
                        if (transaction.attachment.encryptToSelfMessage && NRS.account == transaction.sender) {
                            fieldsToDecrypt.encryptToSelfMessage = $.t("note_to_self");
                            if (!account) {
                                account = transaction.sender;
                            }
                        }
                        NRS.tryToDecrypt(transaction, fieldsToDecrypt, account, {
                            "formEl": "#transaction_info_output_bottom",
                            "outputEl": "#transaction_info_output_bottom"
                        });
                    }

                    transactionInfoOutputBottom.show();
                }
            }

            if (incorrect) {
                $.growl($.t("error_unknown_transaction_type"), {
                    "type": "danger"
                });

                NRS.fetchingModalData = false;
                return;
            }

            if (!async) {
                if (!isModalVisible) {
                    $("#transaction_info_modal").modal("show");
                }
                NRS.fetchingModalData = false;
            }
        } catch (e) {
            NRS.fetchingModalData = false;
            throw e;
        }
    };

    NRS.formatAssetOrder = function (asset, transaction, isModalVisible) {
        var data = {
            "type": (transaction.subtype == 2 ? $.t("ask_order_placement") : $.t("bid_order_placement")),
            "asset_formatted_html": NRS.getTransactionLink(transaction.attachment.asset),
            "asset_name": asset.name,
            "quantity": [transaction.attachment.quantityQNT, asset.decimals],
            "price_formatted_html": NRS.formatOrderPricePerWholeQNT(transaction.attachment.priceNQT, asset.decimals) + " XEL",
            "total_formatted_html": NRS.formatAmount(NRS.calculateOrderTotalNQT(transaction.attachment.quantityQNT, transaction.attachment.priceNQT)) + " XEL"
        };
        data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
        var rows = "";
        var params;
        if (transaction.subtype == 2) {
            params = {"askOrder": transaction.transaction};
        } else {
            params = {"bidOrder": transaction.transaction};
        }
        var transactionField = (transaction.subtype == 2 ? "bidOrder" : "askOrder");
        NRS.sendRequest("getOrderTrades", params, function (response) {
            var tradeQuantity = BigInteger.ZERO;
            var tradeTotal = BigInteger.ZERO;
            if (response.trades && response.trades.length > 0) {
                rows = "<table class='table table-striped'><thead><tr>" +
                "<th>" + $.t("Date") + "</th>" +
                "<th>" + $.t("Quantity") + "</th>" +
                "<th>" + $.t("Price") + "</th>" +
                "<th>" + $.t("Total") + "</th>" +
                "<tr></thead><tbody>";
                for (var i = 0; i < response.trades.length; i++) {
                    var trade = response.trades[i];
                    tradeQuantity = tradeQuantity.add(new BigInteger(trade.quantityQNT));
                    tradeTotal = tradeTotal.add(new BigInteger(trade.quantityQNT).multiply(new BigInteger(trade.priceNQT)));
                    rows += "<tr>" +
                    "<td>" + NRS.getTransactionLink(trade[transactionField], NRS.formatTimestamp(trade.timestamp)) + "</td>" +
                    "<td>" + NRS.formatQuantity(trade.quantityQNT, asset.decimals) + "</td>" +
                    "<td>" + NRS.calculateOrderPricePerWholeQNT(trade.priceNQT, asset.decimals) + "</td>" +
                    "<td>" + NRS.formatAmount(NRS.calculateOrderTotalNQT(trade.quantityQNT, trade.priceNQT)) +
                    "</td>" +
                    "</tr>";
                }
                rows += "</tbody></table>";
                data["trades_formatted_html"] = rows;
            } else {
                data["trades"] = $.t("no_matching_trade");
            }
            data["quantity_traded"] = [tradeQuantity, asset.decimals];
            data["total_traded"] = NRS.formatAmount(tradeTotal, false, true) + " XEL";
        }, { isAsync: false });

        var infoTable = $("#transaction_info_table");
        infoTable.find("tbody").append(NRS.createInfoTable(data));
        infoTable.show();
        if (!isModalVisible) {
            $("#transaction_info_modal").modal("show");
        }
        NRS.fetchingModalData = false;
    };

    NRS.formatCurrencyExchange = function (currency, transaction, type) {
        var rateUnitsStr = " [ " + currency.code + " / XEL ]";
        var data = {
            "type": type == "sell" ? $.t("sell_currency") : $.t("buy_currency"),
            "code": currency.code,
            "units": [transaction.attachment.units, currency.decimals],
            "rate": NRS.calculateOrderPricePerWholeQNT(transaction.attachment.rateNQT, currency.decimals) + rateUnitsStr
        };
        var rows = "";
        NRS.sendRequest("getExchangesByExchangeRequest", {
            "transaction": transaction.transaction
        }, function (response) {
            var exchangedUnits = BigInteger.ZERO;
            var exchangedTotal = BigInteger.ZERO;
            if (response.exchanges && response.exchanges.length > 0) {
                rows = "<table class='table table-striped'><thead><tr>" +
                "<th>" + $.t("Date") + "</th>" +
                "<th>" + $.t("Units") + "</th>" +
                "<th>" + $.t("Rate") + "</th>" +
                "<th>" + $.t("Total") + "</th>" +
                "<tr></thead><tbody>";
                for (var i = 0; i < response.exchanges.length; i++) {
                    var exchange = response.exchanges[i];
                    exchangedUnits = exchangedUnits.add(new BigInteger(exchange.units));
                    exchangedTotal = exchangedTotal.add(new BigInteger(exchange.units).multiply(new BigInteger(exchange.rateNQT)));
                    rows += "<tr>" +
                    "<td>" + NRS.getTransactionLink(exchange.offer, NRS.formatTimestamp(exchange.timestamp)) + "</td>" +
                    "<td>" + NRS.formatQuantity(exchange.units, currency.decimals) + "</td>" +
                    "<td>" + NRS.calculateOrderPricePerWholeQNT(exchange.rateNQT, currency.decimals) + "</td>" +
                    "<td>" + NRS.formatAmount(NRS.calculateOrderTotalNQT(exchange.units, exchange.rateNQT)) +
                    "</td>" +
                    "</tr>";
                }
                rows += "</tbody></table>";
                data["exchanges_formatted_html"] = rows;
            } else {
                data["exchanges"] = $.t("no_matching_exchange_offer");
            }
            data["units_exchanged"] = [exchangedUnits, currency.decimals];
            data["total_exchanged"] = NRS.formatAmount(exchangedTotal, false, true) + " [XEL]";
        }, { isAsync: false });
        return data;
    };

    NRS.formatCurrencyOffer = function (currency, transaction) {
        var rateUnitsStr = " [ " + currency.code + " / XEL ]";
        var buyOffer;
        var sellOffer;
        NRS.sendRequest("getOffer", {
            "offer": transaction.transaction
        }, function (response) {
            buyOffer = response.buyOffer;
            sellOffer = response.sellOffer;
        }, { isAsync: false });
        var data = {};
        if (buyOffer && sellOffer) {
            data = {
                "type": $.t("exchange_offer"),
                "code": currency.code,
                "buy_supply_formatted_html": NRS.formatQuantity(buyOffer.supply, currency.decimals) + " (initial: " + NRS.formatQuantity(transaction.attachment.initialBuySupply, currency.decimals) + ")",
                "buy_limit_formatted_html": NRS.formatQuantity(buyOffer.limit, currency.decimals) + " (initial: " + NRS.formatQuantity(transaction.attachment.totalBuyLimit, currency.decimals) + ")",
                "buy_rate_formatted_html": NRS.calculateOrderPricePerWholeQNT(transaction.attachment.buyRateNQT, currency.decimals) + rateUnitsStr,
                "sell_supply_formatted_html": NRS.formatQuantity(sellOffer.supply, currency.decimals) + " (initial: " + NRS.formatQuantity(transaction.attachment.initialSellSupply, currency.decimals) + ")",
                "sell_limit_formatted_html": NRS.formatQuantity(sellOffer.limit, currency.decimals) + " (initial: " + NRS.formatQuantity(transaction.attachment.totalSellLimit, currency.decimals) + ")",
                "sell_rate_formatted_html": NRS.calculateOrderPricePerWholeQNT(transaction.attachment.sellRateNQT, currency.decimals) + rateUnitsStr,
                "expiration_height": transaction.attachment.expirationHeight
            };
        } else {
            data["offer"] = $.t("no_matching_exchange_offer");
        }
        var rows = "";
        NRS.sendRequest("getExchangesByOffer", {
            "offer": transaction.transaction
        }, function (response) {
            var exchangedUnits = BigInteger.ZERO;
            var exchangedTotal = BigInteger.ZERO;
            if (response.exchanges && response.exchanges.length > 0) {
                rows = "<table class='table table-striped'><thead><tr>" +
                "<th>" + $.t("Date") + "</th>" +
                "<th>" + $.t("Type") + "</th>" +
                "<th>" + $.t("Units") + "</th>" +
                "<th>" + $.t("Rate") + "</th>" +
                "<th>" + $.t("Total") + "</th>" +
                "<tr></thead><tbody>";
                for (var i = 0; i < response.exchanges.length; i++) {
                    var exchange = response.exchanges[i];
                    exchangedUnits = exchangedUnits.add(new BigInteger(exchange.units));
                    exchangedTotal = exchangedTotal.add(new BigInteger(exchange.units).multiply(new BigInteger(exchange.rateNQT)));
                    var exchangeType = exchange.seller == transaction.sender ? "Buy" : "Sell";
                    if (exchange.seller == exchange.buyer) {
                        exchangeType = "Same";
                    }
                    rows += "<tr>" +
                    "<td>" + NRS.getTransactionLink(exchange.transaction, NRS.formatTimestamp(exchange.timestamp)) + "</td>" +
                    "<td>" + exchangeType + "</td>" +
                    "<td>" + NRS.formatQuantity(exchange.units, currency.decimals) + "</td>" +
                    "<td>" + NRS.calculateOrderPricePerWholeQNT(exchange.rateNQT, currency.decimals) + "</td>" +
                    "<td>" + NRS.formatAmount(NRS.calculateOrderTotalNQT(exchange.units, exchange.rateNQT)) +
                    "</td>" +
                    "</tr>";
                }
                rows += "</tbody></table>";
                data["exchanges_formatted_html"] = rows;
            } else {
                data["exchanges"] = $.t("no_matching_exchange_request");
            }
            data["units_exchanged"] = [exchangedUnits, currency.decimals];
            data["total_exchanged"] = NRS.formatAmount(exchangedTotal, false, true) + " [XEL]";
        }, { isAsync: false });
        return data;
    };

    NRS.getUnknownCurrencyData = function (transaction) {
        if (!transaction) {
            return {};
        }
        return {
            "status": "Currency Deleted or not Issued",
            "type": transaction.type,
            "subType": transaction.subtype
        };
    };

    NRS.getTaggedData = function (attachment, subtype, transaction) {
        var data = {
            "type": $.t(NRS.transactionTypes[3].subTypes[subtype].i18nKeyTitle)
        };
        if (attachment.hash) {
            data["hash"] = attachment.hash;
        }
        if (attachment.taggedData) {
            data["tagged_data_formatted_html"] = NRS.getTransactionLink(attachment.taggedData);
            transaction = attachment.taggedData;
        }
        if (attachment.data) {
            data["name"] = attachment.name;
            data["description"] = attachment.description;
            data["tags"] = attachment.tags;
            data["mime_type"] = attachment.type;
            data["channel"] = attachment.channel;
            data["is_text"] = attachment.isText;
            data["filename"] = attachment.filename;
            if (attachment.isText) {
                data["data_size"] = NRS.getUtf8Bytes(attachment.data).length;
            } else {
                data["data_size"] = converters.hexStringToByteArray(attachment.data).length;
            }
        }
        if (transaction.block) {
            data["link_formatted_html"] = NRS.getTaggedDataLink(transaction.transaction, attachment.isText);
        }
        return data;
    };

    function listPublicKeys(publicKeys) {
        var rows = "<table class='table table-striped'><tbody>";
        for (var i = 0; i < publicKeys.length; i++) {
            var recipientPublicKey = publicKeys[i];
            var recipientAccount = {accountRS: NRS.getAccountIdFromPublicKey(recipientPublicKey, true)};
            rows += "<tr>" +
                "<td>" + NRS.getAccountLink(recipientAccount, "account") + "<td>" +
                "</tr>";
        }
        rows += "</tbody></table>";
        return rows;
    }

    $(document).on("click", ".approve_transaction_btn", function (e) {
        e.preventDefault();
        var approveTransactionModal = $('#approve_transaction_modal');
        approveTransactionModal.find('.at_transaction_full_hash_display').text($(this).data("transaction"));
        approveTransactionModal.find('.at_transaction_timestamp').text(NRS.formatTimestamp($(this).data("timestamp")));
        $("#approve_transaction_button").data("transaction", $(this).data("transaction"));
        approveTransactionModal.find('#at_transaction_full_hash').val($(this).data("fullhash"));

        var mbFormatted = $(this).data("minBalanceFormatted");
        var minBalanceWarning = $('#at_min_balance_warning');
        if (mbFormatted && mbFormatted != "") {
            minBalanceWarning.find('.at_min_balance_amount').html(mbFormatted);
            minBalanceWarning.show();
        } else {
            minBalanceWarning.hide();
        }
        var revealSecretDiv = $("#at_revealed_secret_div");
        if ($(this).data("votingmodel") == NRS.constants.VOTING_MODELS.HASH) {
            revealSecretDiv.show();
        } else {
            revealSecretDiv.hide();
        }
    });

    $("#approve_transaction_button").on("click", function () {
        $('.tr_transaction_' + $(this).data("transaction") + ':visible .approve_transaction_btn').attr('disabled', true);
    });

    $("#transaction_info_modal").on("hide.bs.modal", function () {
        NRS.removeDecryptionForm($(this));
        $("#transaction_info_output_bottom, #transaction_info_output_top, #transaction_info_bottom").html("").hide();
    });

    return NRS;
}(NRS || {}, jQuery));
