package com.consultantvendor.data.models.responses

import com.consultantvendor.data.models.requests.AddPrescription
import com.consultantvendor.data.models.requests.DocImage
import com.consultantvendor.data.models.requests.SecondOpinion
import java.io.Serializable

class Request : Serializable {

    var id: String? = null
    var booking_date: String? = null
    var from_user: UserData? = null
    var to_user: UserData? = null
    var time: String? = null
    var service_type: String? = null
    var main_service_type: String? = null
    var status: String? = null
    var price: String? = null
    var created_at: String? = null
    var bookingDateUTC: String? = null
    var canReschedule = false
    var canCancel = false

    var call_id: String? = null
    var is_prescription: Boolean? = null
    var symptoms: List<Filter>? = null
    var symptom_details: String? = null
    var symptom_image:Feed?=null
    var symptom_images: List<DocImage>? = null

    var is_second_oponion: Boolean? = null
    var second_oponion: SecondOpinion? = null

    var extra_detail: Extra_detail? = null

    var pre_scription: AddPrescription? = null
}