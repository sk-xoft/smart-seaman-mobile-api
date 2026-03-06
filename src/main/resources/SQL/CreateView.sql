select mcom.COMPANY_CODE,
       mcom.COMPANY_NAME_EN,
       mcom.COMPANY_NAME_TH,
       mcom.COMPANY_COLOUR,
       mcn.COURSE_NAME_EN,
       mcn.COURSE_NAME_TH,
       mc.COURSE_ID,
       mc.COURSE_ONLINE_DATE, mc.COURSE_ONSITE_DATE,
       concat(
               substr (case when COURSE_ONLINE_DATE is not null then COURSE_ONLINE_DATE else COURSE_ONSITE_DATE end, 7, 4),
               substr (case when COURSE_ONLINE_DATE is not null then COURSE_ONLINE_DATE else COURSE_ONSITE_DATE end, 4, 2),
               substr (case when COURSE_ONLINE_DATE is not null then COURSE_ONLINE_DATE else COURSE_ONSITE_DATE end, 1, 2)
           ) as DATE_FOR_CHECK,
       mc.COURSE_TOTAL_DAYS, mc.COURSE_PRICE,
       mcom.COMPANY_LINE, mcom.COMPANY_FACEBOOK, mcom.COMPANY_PHONE1
from m_courses mc
         inner join m_course_name mcn on mc.COURSE_CODE = mcn.COURSE_CODE
         inner join m_companys mcom on mc.COURSE_COMPANY_CODE = mcom.COMPANY_CODE
where mc.COURSE_STATUS = 'A'
order by mc.CREATE_DATE desc, mc.COURSE_COMPANY_CODE;




drop view  user_send_notifications;

create view user_send_notifications as
select distinct  * from (select distinct USER_MOBILE_UUID, TOKEN_FCM
                         from m_certificates mc
                                  inner join m_fcm_notifications mfn on mc.CERT_MOBILE_UUID = mfn.USER_MOBILE_UUID
                         where DATE_FORMAT(CERT_END_DATE, '%Y-%m-%d') = DATE_FORMAT(((NOW() + INTERVAL 18 MONTH) + INTERVAL  1 DAY ), '%Y-%m-%d')
                         union all
                         select distinct USER_MOBILE_UUID, TOKEN_FCM
                         from m_certificates mc
                             inner join m_fcm_notifications mfn on mc.CERT_MOBILE_UUID = mfn.USER_MOBILE_UUID
                         where DATE_FORMAT(CERT_END_DATE, '%Y-%m-%d') = DATE_FORMAT(((NOW() + INTERVAL 12 MONTH) + INTERVAL  1 DAY ), '%Y-%m-%d')
                         union all
                         select distinct USER_MOBILE_UUID, TOKEN_FCM
                         from m_certificates mc
                             inner join m_fcm_notifications mfn on mc.CERT_MOBILE_UUID = mfn.USER_MOBILE_UUID
                         where DATE_FORMAT(CERT_END_DATE, '%Y-%m-%d') = DATE_FORMAT(((NOW() + INTERVAL 6 MONTH) + INTERVAL  1 DAY ), '%Y-%m-%d')
                         union all
                         select distinct USER_MOBILE_UUID, TOKEN_FCM
                         from m_certificates mc
                             inner join m_fcm_notifications mfn on mc.CERT_MOBILE_UUID = mfn.USER_MOBILE_UUID
                         where DATE_FORMAT(CERT_END_DATE, '%Y-%m-%d') = DATE_FORMAT(((NOW() + INTERVAL 3 MONTH) + INTERVAL  1 DAY ), '%Y-%m-%d')
                        ) as view_a;



