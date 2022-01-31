package com.consultantvendor.ui.dashboard.home.prescription

import androidx.lifecycle.ViewModel
import com.consultantvendor.data.apis.WebService
import com.consultantvendor.data.models.requests.AddPrescription
import com.consultantvendor.data.models.responses.UserData
import com.consultantvendor.data.network.responseUtil.ApiResponse
import com.consultantvendor.data.network.responseUtil.ApiUtils
import com.consultantvendor.data.network.responseUtil.Resource
import com.consultantvendor.di.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class AddPrescriptionViewModel @Inject constructor(private val webService: WebService) : ViewModel() {


    val prescreptions by lazy { SingleLiveEvent<Resource<UserData>>() }

    fun prescreptions(addPrescription: AddPrescription) {
        prescreptions.value = Resource.loading()

        webService.prescreptions(addPrescription)
                .enqueue(object : Callback<ApiResponse<UserData>> {

                    override fun onResponse(call: Call<ApiResponse<UserData>>,
                                            response: Response<ApiResponse<UserData>>) {
                        if (response.isSuccessful) {
                            prescreptions.value = Resource.success(response.body()?.data)
                        } else {
                            prescreptions.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
                        prescreptions.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }
}