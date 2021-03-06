package com.consultantvendor.data.models.responses

import java.io.Serializable

class Wallet : Serializable {

    var id: String? = null
    var from: UserData? = null
    var to: UserData? = null
    var transaction_id: Int? = null
    var created_at: String? = null
    var updated_at: String? = null
    var type: String? = null
    var closing_balance: String? = null
    var amount: String? = null
    var status: String? = null

    /*Cards*/
    var isSelected = false
    var card_brand: String? = null
    var last_four_digit: String? = null

    /*Payout*/
    var payout_message: String? = null
}