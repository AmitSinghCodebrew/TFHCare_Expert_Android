package com.consultantvendor.data.models.responses.appdetails

import com.consultantvendor.data.models.responses.Page
import java.util.*

class AppVersion {

    var update_type: Int? = null
    var message: String? = null
    var client_id: String? = null
    var privateKey: String? = null
    var publicKey: String? = null

    var version_name: String? = null
    var current_version: Int? = null
    var currency_code: String? = null
    var charges: String? = null
    var audio_video: String? = null
    var class_calling: String? = null
    var unit_price: String? = null
    var slot_duration: String? = null
    var vendor_auto_approved: Boolean? = null
    var currency: String? = null
    var jitsi_id: String? = null
    var applogo: String? = null
    var domain = ""
    var domain_url: String? = null
    var payment_type: String? = null
    var gateway_key: String? = null
    var insurance: Boolean? = null
    var insurances: ArrayList<Insurance>? = null
    var custom_fields: CustomFields? = null
    var country_id: String? = null
    var support_url: String? = null

    var client_features: ArrayList<ClientFeatures>? = null
    var clientFeaturesKeys = ClientFeaturesKeys()

    /*Pages*/
    var pages: List<Page>? = null

}