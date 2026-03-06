package com.seaman.service;

import com.seaman.entity.CertificateEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.repository.CertificateRepository;
import com.seaman.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeleteUserMobileService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final UserRepository userRepository;

    private final CertificateRepository certificateRepository;

    private final EmailService emailService;

    @Value("${batch.delete.user.mobile}")
    private String emailTo;

    /**
     * เพิ่ม batch สำหรับลบข้อมูลในตาราง m_mobile_user และ m_course โดยมีขั้นตอนดังนี้
     * 1. ลบข้อมูลในตาราง m_mobile_user ก่อน แล้วค่อยลบในตาราง m_course
     * - กรณี execute batch fail ให้จบการทำงาน และส่งอีเมลบอกแอดมิน ให้รู้ว่า batch fail
     * - กรณี execute batch ได้ ให้รันที่ละ record จนจบ และส่งอีเมล บอกรายละเอียด ให้แอดมินทราบ ฟอแมทดังนี้
     * number, id, mobile_uuid, smart_seaman_id, username, status, response/error
     * 1, 27, 63aee2aa-0524-4c28-bdac-20211c72afd9, 00020, kanuengnit333@gmail.com, SUCCESS, Delete successfully
     * 2, 49, ebb8ea31-bbff-4803-abbe-d61479b330ad, 00042, nuengnit@gmail.com, SUCCESS, Delete successfully
     * 3, 50, ebb8ea31-bbff-4803-abbe-d61479b33333, 00043, nuengnit2@gmail.com, FAILED, Connection fail
     * 2. ลบข้อมูลในตาราง m_course ต่อ
     * - กรณี execute batch fail ให้จบการทำงาน และส่งอีเมลบอกแอดมิน ให้รู้ว่า batch fail
     * - กรณี execute batch ได้ ให้รันที่ละ record จนจบ และส่งอีเมล บอกรายละเอียด ให้แอดมินทราบ ฟอแมทดังนี้
     * number, id, mobile_uuid, cert_doc_code, status, response/error
     * 1, 437, ebb8ea31-bbff-4803-abbe-d61479b330ad, DOC014, COP-Ship cook, SUCCESS, Delete successfully
     * 2, 438, ebb8ea31-bbff-4803-abbe-d61479b330ad, DOC013, COP-Seafarers with Designated Security Duties (DSD), FAILED, Connection fail
     * 3, 439, ebb8ea31-bbff-4803-abbe-d61479b330ad, DOC012, COP-Ship Security Officer (SSO), FAILED, Connection fail
     * 3. ส่งเข้าอีเมล info.smartseaman@gmail.com, service.smartseaman@gmail.com
     *
     * @return
     */

    public void deleteUserIsOverDueDate() {

        Map<String, String> result = new HashMap<>();
        Map<String, String> resultCert = new HashMap<>();

        List<UsersEntity> usersEntityList = userRepository.getUserIsDeleteOverDueDate();
        log.info("Start User is delete size : {}", usersEntityList.size());

        int index = 1;
        for (UsersEntity entity : usersEntityList) {
            String var1 = entity.getMobileUuid();

            try {
                boolean isDelete = userRepository.deleteUserIsStatusDeleteOverDueDate(var1);

                String var2 = "";
                if (isDelete) {
                    var2 = entity.getMobileUserId() + ",  " + entity.getMobileUuid() + ",  " + entity.getSmartSeamanId() + ",  " + entity.getEmail() + ",  SUCCESS <br/>";
                } else {
                    var2 = entity.getMobileUserId() + ",  " + entity.getMobileUuid() + ",  " + entity.getSmartSeamanId() + ",  " + entity.getEmail() + ",  FAILED <br/>";
                }
                result.put(String.valueOf(index), var2);
            } catch (Exception exception) {
                result.put(String.valueOf(index), String.valueOf(exception));
            } finally {
                index++;
            }
        }

        List<CertificateEntity> certificateEntities = certificateRepository.getCertificationIsNotUserMobileOwner();
        log.info("Start Cert is delete size -> {}", certificateEntities.size());

        int index1 = 1;
        for (CertificateEntity entity : certificateEntities) {

            String var1 = entity.getCertId();
            String var2 = "";

            try {
                boolean isDelete = certificateRepository.delete(var1);
                if (isDelete) {
                    var2 = entity.getCertMobileUuid() + ",  " + entity.getCertDocumentCode() + ",  SUCCESS<br/>";
                } else {
                    var2 = entity.getCertMobileUuid() + ",  " + entity.getCertDocumentCode() + ",  FAILED<br/>";
                }
                resultCert.put(String.valueOf(index1), var2);

            } catch (Exception exception) {
                resultCert.put(String.valueOf(index1), String.valueOf(exception));
            } finally {
                index1++;
            }
        }

        /** Send email **/
        emailService.sendEmailDeleteUser(emailTo, result, resultCert);
    }
}
