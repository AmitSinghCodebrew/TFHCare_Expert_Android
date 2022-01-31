package com.consultantvendor.data.models.requests

import java.io.Serializable

class AddPrescription : Serializable {
    var request_id: String? = null
    var type: String? = null

    /*Manual*/
    var title: String? = null
    var image: ArrayList<String>? = null

    /*Digital*/
    var pre_scription_notes: String? = null
    var pre_scriptions: ArrayList<DigitalPrescription>? = null

    /*get data*/
    var medicines: ArrayList<DigitalPrescription>? = null
    var images: ArrayList<String>? = null
}