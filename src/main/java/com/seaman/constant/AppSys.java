package com.seaman.constant;

public class AppSys {

    // Global

    public static final String PROFILE_PROD = "prod";
    public static final String APPLICATION_NAME = "smart-seaman-mobile-api";
    public static final String APPLICATION_DESC = "Smart Seaman MOBILE API.";

    public static final String ASIA_BANGKOK_ZONE = "Asia/Bangkok";
    public static final String APPLICATION_VERSION = "V 0.1";
    public static final String LANGUAGE = "language";
    public static final String LANG_EN = "EN";
    public static final String LANG_TH = "TH";

    public static final String REQUEST_BODY = "requestBody";
    public static final String RESPONSE_BODY = "responseBody";

    // Interceptor
    public static final String  API_EXECUTIME  =  "executime";
    public static final String  TRACE_ID =  "trace_id";

    public static final String HEADER_CORRELATION_ID = "correlationid";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    // public static final String HEADER_ACCEPT_LANGUAGE = "language";
    public static final String HEADER_DEVICE_MODEL = "devicemodel";
    public static final String HEADER_DEVICE_INFO =  "deviceinfo";

    public static final String CLIENT_IP = "client_ip";


    /** JWT Payload **/
    public static final String CLAIMS_ISSUER = "iss";       // Issuer
    public static final String CLAIMS_SUBJECT = "sub";      // Subject
    public static final String CLAIMS_AUDIENCE = "aud";     // Audience
    public static final String CLAIMS_EXPIRATION = "exp";   // Expiration
    public static final String CLAIMS_NOT_BEFORE = "nbf";   // Not Before
    public static final String CLAIMS_ISSUED_AT = "iat";    // Issued At
    public static final String CLAIMS_JTI = "jti";          // JWT ID


    /** Company Condition **/
    public static final String NOTI_TYPE_CERT_EXPIRED = "CERT_EXPIRED";


    /** For mobile register **/
    public static final String TITLE_EMAIL_REGISTER = "กรุณายืนยันอีเมลเพื่อเข้าสู่ระบบ Smart Seaman";
    public static final String REGISTER_BODY_EMAIL_ROW_1 = "ขอต้อนรับ คุณ {full_name} เข้าสู่แอปพลิเคชั่น Smart Seaman";
    public static final String REGISTER_BODY_EMAIL_ROW_2 = "<br/>การลงทะเบียนใกล้เสร็จสมบูรณ์ กรุณาคลิกลิงก์เพื่อยืนยันอีเมล ";
    public static final String REGISTER_BODY_EMAIL_ROW_3 = "<br/>{link_register}";


    /** For mobile forget password **/
    public static final String TITLE_EMAIL_FORGOT_PASSWORD = "ขอตั้งรหัสผ่านใหม่";
    public static final String FORGOT_PASSWORD_BODY_ROW_1 = "เรียนคุณ {full_name}";
    public static final String FORGOT_PASSWORD_BODY_ROW_2 = "<br/>คุณได้ส่งคำขอเพื่อตั้งรหัสผ่านใหม่ กรุณาคลิกลิงก์เพื่อดำเนินการ";
    public static final String SENDER_NAME = "SmartSeaman";
    public static final String SENDER_NAME_SPACE = "Smart Seaman";

}
