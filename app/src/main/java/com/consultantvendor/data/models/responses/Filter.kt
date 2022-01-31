package com.consultantvendor.data.models.responses

import java.io.Serializable

class Filter :Serializable{
    var id: Int? = null
    var category_id: Int? = null
    var filter_name: String? = null
    var preference_name: String? = null
    var is_multi: String? = null
    var options: List<FilterOption>? = null

    /*Options*/
    var option_name: String? = null
    var filter_type_id: Int? = null
    var isSelected = false

    /*Symptom*/
    var name: String? = null
    var image: String? = null
    var symptom_id: String? = null

}