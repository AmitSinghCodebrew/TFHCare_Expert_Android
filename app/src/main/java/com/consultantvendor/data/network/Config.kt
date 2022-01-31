package com.consultantvendor.data.network


object Config {

    var BASE_URL = ""

//    var BASE_URL_DEV = "https://dev.thefinesthealthcare.com/"
    var BASE_URL_DEV = "https://thefinesthealthcare.com/"
    var BASE_URL_CLIENT = "https://thefinesthealthcare.com/"
    var BASE_URL_LIVE = "https://thefinesthealthcare.com/"

    /*Jitsi*/
    var BASE_URL_JITSI = "https://meet.thefinesthealthcare.com/"

    var IMAGE_URL = "https://cdn-assets.thefinesthealthcare.com/"

    private val appMode = AppMode.DEV

    val baseURL: String
        get() {
            init(appMode)
            return BASE_URL
        }

    val imageURL: String
        get() {
            init(appMode)
            return IMAGE_URL
        }

    private fun init(appMode: AppMode) {

        BASE_URL = when (appMode) {
            AppMode.DEV -> {
                BASE_URL_DEV
            }
            AppMode.CLIENT -> {
                BASE_URL_CLIENT
            }
            AppMode.LIVE -> {
                BASE_URL_LIVE
            }
        }
    }

    private enum class AppMode {
        DEV, CLIENT, LIVE
    }
}