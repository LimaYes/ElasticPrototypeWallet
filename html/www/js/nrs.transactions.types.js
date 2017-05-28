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
var NRS = (function(NRS, $, undefined) {
    // If you add new mandatory attributes, please make sure to add them to
    // NRS.loadTransactionTypeConstants as well (below)
    NRS.transactionTypes = {
        0: {
            'title': "Payment",
            'i18nKeyTitle': 'payment',
            'iconHTML': "<i class='ion-calculator'></i>",
            'subTypes': {
                0: {
                    'title': "Ordinary Payment",
                    'i18nKeyTitle': 'ordinary_payment',
                    'iconHTML': "<i class='fa fa-money'></i>",
                    'receiverPage': 'transactions'
                },
                1: {
                    'title': "Genesis Redeem",
                    'i18nKeyTitle': 'redeem_payment',
                    'iconHTML': "<i class='fa fa-money'></i>",
                    'receiverPage': 'transactions'
                }
            }
        },
        1: {
            'title': "Messaging/Voting/Aliases",
            'i18nKeyTitle': 'messaging_voting_aliases',
            'iconHTML': "<i class='fa fa-envelope-square'></i>",
            'subTypes': {
                0: {
                    'title': "Arbitrary Message",
                    'i18nKeyTitle': 'arbitrary_message',
                    'iconHTML': "<i class='fa fa-envelope-o'></i>",
                    'receiverPage': 'messages'
                },
                1: {
                    'title': "Poll Creation",
                    'i18nKeyTitle': 'poll_creation',
                    'iconHTML': "<i class='fa fa-check-square-o'></i>"
                },
                2: {
                    'title': "Vote Casting",
                    'i18nKeyTitle': 'vote_casting',
                    'iconHTML': "<i class='fa fa-check'></i>"
                },
                3: {
                    'title': "Hub Announcement",
                    'i18nKeyTitle': 'hub_announcement',
                    'iconHTML': "<i class='ion-radio-waves'></i>"
                },
                4: {
                    'title': "Account Info",
                    'i18nKeyTitle': 'account_info',
                    'iconHTML': "<i class='fa fa-info'></i>"
                },
                5: {
                    'title': "Transaction Approval",
                    'i18nKeyTitle': 'transaction_approval',
                    'iconHTML': "<i class='fa fa-gavel'></i>",
                    'receiverPage': "transactions"
                }
            }
        },
        2: {
            'title': "Account Control",
            'i18nKeyTitle': 'account_control',
            'iconHTML': '<i class="ion-locked"></i>',
            'subTypes': {
                0: {
                    'title': "Balance Leasing",
                    'i18nKeyTitle': 'balance_leasing',
                    'iconHTML': '<i class="fa fa-arrow-circle-o-right"></i>',
                    'receiverPage': "transactions"
                },
                1: {
                    'title': "Mandatory Approval",
                    'i18nKeyTitle': 'phasing_only',
                    'iconHTML': '<i class="fa fa-gavel"></i>',
                    'receiverPage': "transactions"
                }
            }
        },
        3: {
            'title': "Data Cloud",
            'i18nKeyTitle': 'tagged_data',
            'iconHTML': '<i class="fa fa-dashboard"></i>',
            'subTypes': {
                0: {
                    'title': "Upload Data",
                    'i18nKeyTitle': 'upload_tagged_data',
                    'iconHTML': '<i class="fa fa-upload"></i>'
                },
                1: {
                    'title': "Extend Data Lifetime",
                    'i18nKeyTitle': 'extend_tagged_data',
                    'iconHTML': '<i class="fa fa-expand"></i>'
                }
            }
        }
    };

    NRS.subtype = {};

    NRS.loadTransactionTypeConstants = function(response) {
        if (response.genesisAccountId) {
            $.each(response.transactionTypes, function(typeIndex, type) {
                if (!(typeIndex in NRS.transactionTypes)) {
                    NRS.transactionTypes[typeIndex] = {
                        'title': "Unknown",
                        'i18nKeyTitle': 'unknown',
                        'iconHTML': '<i class="fa fa-question-circle"></i>',
                        'subTypes': {}
                    }
                }
                $.each(type.subtypes, function(subTypeIndex, subType) {
                    if (!(subTypeIndex in NRS.transactionTypes[typeIndex]["subTypes"])) {
                        NRS.transactionTypes[typeIndex]["subTypes"][subTypeIndex] = {
                            'title': "Unknown",
                            'i18nKeyTitle': 'unknown',
                            'iconHTML': '<i class="fa fa-question-circle"></i>'
                        }
                    }
                    NRS.transactionTypes[typeIndex]["subTypes"][subTypeIndex]["serverConstants"] = subType;
                });
            });
            NRS.subtype = response.transactionSubTypes;
        }
    };

    NRS.isOfType = function(transaction, type_str) {
        if (!NRS.subtype[type_str]) {
            $.growl($.t("unsupported_transaction_type"));
            return;
        }
        return transaction.type == NRS.subtype[type_str].type && transaction.subtype == NRS.subtype[type_str].subtype;
    };
    
    return NRS;
}(Object.assign(NRS || {}, isNode ? global.client : {}), jQuery));

if (isNode) {
    module.exports = NRS;
}