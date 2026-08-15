package com.seaman.constant;

public class Routes {

    // === API Version / Health ===
    public static final String VERSION = "/v1";
    public static final String HEALTH = "/health";

    // === Auth ===
    public static final String LOGIN = "/login";
    public static final String REFRESH_TOKEN = "/refresh-token";
    public static final String REGISTER = "/register";
    public static final String CHANGE_HERO = "/change-password";
    public static final String FORGOT_HERO = "/activate-forgot-password";
    public static final String SEND_EMAIL = "/send-email";
    public static final String ACTIVATE_USER = "/activate-user";
    public static final String RESET_PASSWORD = "/reset-password";

    // === Profile ===
    public static final String PROFILE = "/profile";
    public static final String PROFILE_UPDATE = "/profile-update";
    public static final String PROFILE_IMAGE = "/profile-image";
    public static final String PROFILE_INACTIVE = "/profile/inactive";
    public static final String PROFILE_ACTIVE = "/profile/active";

    // === Master Data ===
    public static final String MASTER = "/master";
    public static final String MASTER_DOCUMENTS = "/master/documents";
    public static final String MASTER_COURSES = "/master/courses";
    public static final String MASTER_PROVINCES = "/master/provinces";
    public static final String MASTER_DISTRICTS = "/master/districts";
    public static final String MASTER_SUBDISTRICTS = "/master/subdistricts";

    // === Delivery Address ===
    public static final String DELIVERY_ADDRESSES = "/delivery-addresses";
    public static final String DELIVERY_ADDRESSES_RENEWAL = "/delivery-addresses/{requestNo}";
    public static final String DELIVERY_ADDRESSES_BY_ID = "/delivery-addresses/{addressId}";

    // === Documents & Certification ===
    public static final String DOCUMENTS_LIST_COT = "/documents/certification/COT";
    public static final String DOCUMENTS_LIST_DOC = "/documents/certification/DOC";
    public static final String DOCUMENTS_LIST_CLOSE_TO_EXPIRATION = "/documents/certification/to-expiration";
    public static final String CREATE_CERT = "/documents/certification/create";
    public static final String UPDATE_CERT = "/documents/certification/update";
    public static final String DELETE_CERT = "/documents/certification/delete";
    public static final String EDIT_CERT = "/documents/certification/edit";
    public static final String VIEW_CERT = "/documents/certification/view";
    public static final String DOCUMENT_REQUEST_ITEM_FILES = "/documents/request-items/{itemCode}/files";

    // === School Training ===
    public static final String SCHOOL_TRAINING_LIST = "/school-trainings";
    public static final String SCHOOL_TRAINING_DETAIL = "/school-trainings/detail";

    // === Forms ===
    public static final String FORM_LIST = "/forms";
    public static final String FORM_BY_CODE = "/form";

    // === FCM ===
    public static final String FCM_UPDATE = "/fcm-update";

    // === Notifications ===
    public static final String NOTI_MANUAL = "/send-noti";
    public static final String NOTIFICATIONS = "/notifications";
    public static final String NOTIFICATIONS_UPDATE = "/notifications/update";
    public static final String NOTIFICATIONS_UPDATE_VALUE_ID = "/notifications/update/valueId";
    public static final String NOTIFICATIONS_UPDATE_ALL = "/notifications/update/all";

    // === News ===
    public static final String NEWS = "/news";
    public static final String NEWS_DETAIL = "/news/detail";
    public static final String PREVIEW_PIC_NEWS = "/news/preview";

    // === Banner ===
    public static final String BANNER = "/banners";
    public static final String PREVIEW_PIC_BANNER = "/banners/preview";

    // === Vouchers ===
    public static final String VOUCHERS = "/vouchers";
    public static final String VOUCHERS_DETAIL = "/vouchers/detail";
    public static final String VOUCHERS_PREVIEW = "/vouchers/preview";
    public static final String VOUCHERS_PREVIEW_QR = "/vouchers/qr";

    // === Policy ===
    public static final String POLICY = "/privacy_policy";

    // === Document Renewal ===
    public static final String DOCUMENT_RENEWALS_REQUEST_VALIDATE_AND_CREATE = "/documents-renewals/requests/validate-and-create";
    public static final String DOCUMENT_RENEWALS = "/documents-renewals";
    public static final String DOCUMENT_RENEWAL_REQUEST_MOBILE = "/documents-renewals/requests/{requestNo}/mobile";
    public static final String DOCUMENT_RENEWAL_REQUEST_DELETE = "/documents-renewals/requests/{requestNo}";
    public static final String DOCUMENT_RENEWAL_REQUEST_HARD_DELETE = "/documents-renewals/requests/{requestNo}/hard";
    public static final String DOCUMENT_RENEWAL_STATUSES = "/document-renewals/statuses";
    public static final String DOCUMENT_RENEWAL_PRICES = "/document-renewals/prices";
    public static final String DOCUMENT_RENEWALS_MY = "/document-renewals/my";
    public static final String DOCUMENT_RENEWAL_DETAIL = "/document-renewals/{requestNo}";
    public static final String DOCUMENT_RENEWAL_TIMELINE = "/document-renewals/{requestNo}/timeline";
    public static final String DOCUMENT_RENEWAL_ITEM_PREVIEW_BY_QUERY = "/document-renewals/items/preview";
    public static final String DOCUMENT_RENEWAL_ITEM_PREVIEW = "/document-renewals/{requestNo}/items/{documentRequestItemCode}/preview";
    public static final String DOCUMENT_RENEWAL_ITEM_FILE = "/document-renewals/{requestNo}/items/{documentRequestItemCode}/file";
    public static final String DOCUMENT_RENEWAL_RESUBMIT = "/document-renewals/{requestNo}/resubmit";
    public static final String DOCUMENT_RENEWAL_PAYMENTS = "/document-renewals/{requestId}/payments";
    public static final String DOCUMENT_RENEWAL_PAYMENT = "/document-renewals/{requestId}/payments/{transactionId}";

    // === Payment ===
    public static final String OMISE_WEBHOOK = "/payments/omise/webhook";
}
