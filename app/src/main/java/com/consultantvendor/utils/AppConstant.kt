package com.consultantvendor.utils

/*Links*/

const val PLAY_STORE = "https://play.google.com/store/apps/details?id="

/*Live*/
const val SOCKET_URL = "https://socket.thefinesthealthcare.com/"
/*DEV*/
//const val SOCKET_URL = "https://dev-socket.thefinesthealthcare.com/"

const val APP_UNIQUE_ID = "c8ce267181881c2d0e7251cda66172b07" /*a59ef14422c898df221e0f4da0ed85611*/


const val STORAGE_DIRECTORY = "/MyDoctor/"
const val FILE_PATH_DIRECTORY = "file://"

const val ANDROID = "ANDROID"
const val APP_TYPE = "service_provider"

const val USER_DATA = "user data"
const val APP_DETAILS = "APP_DETAILS"
const val USER_LANGUAGE = "user language"
const val POSITION = "POSITION"
const val COUNTRY_CODE = "COUNTRY_CODE"
const val PHONE_NUMBER = "PHONE_NUMBER"
const val UPDATE_NUMBER = "UPDATE_NUMBER"
const val UPDATE_PROFILE = "UPDATE_PROFILE"
const val USER_ADDRESS = "user address"
const val UPDATE_AVAILABILITY = "UPDATE_AVAILABILITY"
const val UPDATE_CATEGORY = "UPDATE_CATEGORY"
const val UPDATE_PREFRENCES = "UPDATE_PREFRENCES"

const val PUSH_DATA = "PUSH_DATA"
const val PAGE_TO_OPEN = "PAGE_TO_OPEN"

const val LAST_MESSAGE = "last message"
const val USER_ID = "user id"
const val USER_NAME = "user name"
const val OTHER_USER_ID = "other user id"
const val UPDATE_CHAT = "updateChat"
const val EXTRA_REQUEST_ID = "EXTRA_REQUEST_ID"
const val EXTRA_IS_FIRST = "EXTRA_IS_FIRST"
const val EXTRA_CALL_NAME = "extra call name"
const val EXTRA_TAB = "extra tab"
const val EXTRA_SYMPTOM = "EXTRA_SYMPTOM"

object CallType {
    const val CALL = "call"
    const val CHAT = "chat"
    const val FEED = "feed"
    const val ALL = "all"
}

object ClassType {
    const val ADDED = "added"
    const val STARTED = "started"
    const val COMPLETED = "completed"
}

object ConsultType {
    const val CHAT = "chat"
    const val AUDIO_CALL = "audio_call"
    const val VIDEO_CALL = "video_call"
    const val HOME_VISIT = "home_visit"
    const val CLINIC_VISIT = "clinic_visit"
    const val OTHER = "other"
}

object WalletMoney {
    const val DEPOSIT = "deposit"
    const val WITHDRAWAL = "withdrawal"
    const val ALL = "all"
    const val PAYOUTS = "payouts"
    const val ADD_MONEY = "add_money"
    const val FAILED="failed"
    const val SUCCESS="success"
}

object CallAction {
    const val PENDING = "pending"
    const val ACCEPT = "accept"
    const val REJECT = "reject"
    const val INPROGRESS = "in-progress"
    const val COMPLETED = "completed"
    const val FAILED = "failed"
    const val CANCELED = "canceled"
    const val START = "start"
    const val REACHED = "reached"
    const val START_SERVICE = "start_service"
    const val CANCEL_SERVICE = "cancel_service"
}

object PriceType {
    const val PRICE_RANGE = "price_range"
}

object AvailabilityType {
    const val WEEK_WISE = "weekwise"
    const val SPECIFIC_DATE = "specific_date"
    const val SPECIFIC_DAY = "specific_day"
    const val WEEKDAYS = "weekdays"
}

object AppRequestCode {
    const val AUTOCOMPLETE_REQUEST_CODE: Int = 100
    const val IMAGE_PICKER: Int = 101
    const val ADD_MONEY: Int = 102
    const val PROFILE_UPDATE: Int = 103
    const val ARTICLE_CHANGES = 104
    const val REQ_CHAT = 105
    const val ADD_CLASS = 106
    const val ADD_AVAILABILITY = 107
    const val ADD_CARD: Int = 108
    const val PAYOUT_MONEY: Int = 109
    const val ADD_PRESCRIPTION: Int = 110
    const val APPOINTMENT_DETAILS: Int = 111
    const val DOC_PICKER: Int = 112
    const val ASK_FOR_LOCATION: Int = 113
    const val LOCATION_PERMISSION_ID = 114
}


object DocType {
    const val TEXT = "TEXT"
    const val IMAGE = "IMAGE"
    const val PDF = "PDF"
    const val AUDIO = "AUDIO"
    const val MESSAGE_TYPING = "TYPING"
}

object ImageFolder{
    const val UPLOADS = "uploads/"
    const val THUMBS = "thumbs/"
    const val PDF = "pdf/"
}

object MediaUploadStatus {
    const val NOT_UPLOADED = "not_uploaded"
    const val UPLOADING = "uploading"
    const val CANCELED = "canceled"
    const val UPLOADED = "unloaded"
}

object DeepLink {
    const val USER_PROFILE = "userProfile"
    const val INVITE = "Invite"
}

object PageLink {
    const val TERMS_CONDITIONS = "terms-conditions"
    const val PRIVACY_POLICY = "privacy-policy"
}

object PaymentFrom {
    const val STRIPE = "stripe"
    const val RAZOR_PAY = "razor pay"
    const val CCA_VENUE = "cca venue"
}

object CustomFields {
    const val ZIP_CODE = "Zip Code"
    const val QUALIFICATION = "Qualification"
}

object ClientFeatures {
    const val ADDRESS = "Address Required"
}

object BlogType {
    const val BLOG = "blog"
    const val ARTICLE = "article"
}

object PreferencesType {
    const val LANGUAGES = "Languages"
    const val GENDER = "Gender"
    const val SIGNUP_AS = "Signup As"
    const val ALL = "All"
}

object PrescriptionType {
    const val MANUAL = "manual"
    const val DIGITAL = "digital"
}