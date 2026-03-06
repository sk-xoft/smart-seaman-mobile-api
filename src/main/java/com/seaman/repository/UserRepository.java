package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository extends CommonRepository {

    private static final String FIND_BY_USERNAME = "select * from m_mobile_users where USERNAME = :USERNAME ";
    private static final String FIND_BY_MOBILE_UUID = "select * from m_mobile_users where MOBILE_UUID = :MOBILE_UUID ";
    private static final String FIND_BY_EMAIL = "select * from m_mobile_users where EMAIL = :EMAIL ";
    private static final String MAX_MEMBER_CODE = "select MAX(CAST(SMART_SEAMAN_ID as DECIMAL) + 1) as max_member from m_mobile_users";
    private static final String INSERT = "insert into m_mobile_users (SMART_SEAMAN_ID, MOBILE_UUID, USERNAME, PASSWORD, FIRST_NAME, LAST_NAME, COMPANY_CODE, POSITION_CODE, EMAIL, MOBILE_NUMBER, PROFILE_PICTURE, USER_STATUS, LAST_LOGON, CREATE_BY, " +
            " CREATE_DATE, DATE_OF_BIRTH, DISPLAY_NAME, DISPLAY_TYPE) " +
            " values (:SMART_SEAMAN_ID, :MOBILE_UUID, :USERNAME, :PASSWORD, :FIRST_NAME, :LAST_NAME,  :COMPANY_CODE, :POSITION_CODE, :EMAIL, :MOBILE_NUMBER, :PROFILE_PICTURE, :USER_STATUS, :LAST_LOGON, :CREATE_BY, NOW(), :DATE_OF_BIRTH, :DISPLAY_NAME, :DISPLAY_TYPE)";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public UsersEntity findByUsername(String username) {

        List<UsersEntity> listRecord = null;

        UsersEntity result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("USERNAME", username);

            listRecord = template.query(FIND_BY_USERNAME, namedParameters, new BeanPropertyRowMapper(UsersEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public UsersEntity findByUserUID(String userUid) {

        List<UsersEntity> listRecord = null;

        UsersEntity result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("MOBILE_UUID", userUid);

            listRecord = template.query(FIND_BY_MOBILE_UUID, namedParameters, new BeanPropertyRowMapper(UsersEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public UsersEntity findByEmail(String email) {

        List<UsersEntity> listRecord = null;

        UsersEntity result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("EMAIL", email);

            listRecord = template.query(FIND_BY_EMAIL, namedParameters, new BeanPropertyRowMapper(UsersEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public Integer countMax() {

        Integer maxMember;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            maxMember = template.queryForObject(MAX_MEMBER_CODE, namedParameters, Integer.class);

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        maxMember = (maxMember == null) ? 1 : maxMember;

        return maxMember;
    }

    public boolean insert(UsersEntity entity) {

        boolean result = false;
        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("SMART_SEAMAN_ID", entity.getSmartSeamanId())
                    .addValue("MOBILE_UUID", entity.getMobileUuid())
                    .addValue("USERNAME", entity.getUsername())
                    .addValue("PASSWORD", entity.getPassword())
                    .addValue("FIRST_NAME", entity.getFirstName())
                    .addValue("LAST_NAME", entity.getLastName())
                    .addValue("COMPANY_CODE", entity.getCompanyCode())
                    .addValue("POSITION_CODE", entity.getPositionCode())
                    .addValue("EMAIL", entity.getEmail())
                    .addValue("MOBILE_NUMBER", entity.getMobileNumber())
                    .addValue("PROFILE_PICTURE", entity.getProfilePicture())
                    .addValue("USER_STATUS", entity.getUserStatus())
                    .addValue("LAST_LOGON", entity.getLastLogin())
                    .addValue("CREATE_BY", entity.getCreateBy())
                    .addValue("DATE_OF_BIRTH", entity.getDateOfBirth())
                    // .addValue("CREATE_DATE", entity.getUpdateDate())
                    .addValue("DISPLAY_NAME", entity.getDisplayName())
                    .addValue("DISPLAY_TYPE", entity.getDisplayType());

            int rowAffected = template.update(INSERT, namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public boolean changePassword(UsersEntity entity) {

        boolean result = false;

        StringBuilder sql = new StringBuilder();
        sql.append(" update m_mobile_users set PASSWORD = :PASSWORD where MOBILE_UUID = :MOBILE_UUID ");
        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("MOBILE_UUID", entity.getMobileUuid())
                    .addValue("PASSWORD", entity.getPassword());

            int rowAffected = template.update(sql.toString(), namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public boolean update(UsersEntity entity) {

        boolean result = false;

        StringBuilder sql = new StringBuilder();
        sql.append("  update m_mobile_users ");
        sql.append(" set ");
        sql.append("    USERNAME = :USERNAME, ");
        sql.append("    FIRST_NAME = :FIRST_NAME, ");
        sql.append("    LAST_NAME = :LAST_NAME, ");
        sql.append("    COMPANY_CODE = :COMPANY_CODE, ");
        sql.append("    POSITION_CODE = :POSITION_CODE, ");
        sql.append("    EMAIL = :EMAIL, ");
        sql.append("    MOBILE_NUMBER = :MOBILE_NUMBER, ");
        sql.append("    DATE_OF_BIRTH = :DATE_OF_BIRTH, ");
        sql.append("    UPDATE_DATE =  now(),");
        sql.append("    UPDATE_BY = :UPDATE_BY ");
        sql.append(" where ");
        sql.append("    MOBILE_UUID = :MOBILE_UUID ");
        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("USERNAME", entity.getUsername())
                    .addValue("FIRST_NAME", entity.getFirstName())
                    .addValue("LAST_NAME", entity.getLastName())
                    .addValue("COMPANY_CODE", entity.getCompanyCode())
                    .addValue("POSITION_CODE", entity.getPositionCode())
                    .addValue("EMAIL", entity.getEmail())
                    .addValue("MOBILE_NUMBER", entity.getMobileNumber())
                    .addValue("DATE_OF_BIRTH", entity.getDateOfBirth())
                    .addValue("MOBILE_UUID", entity.getMobileUuid())
                    .addValue("UPDATE_BY", entity.getUsername());

            int rowAffected = template.update(sql.toString(), namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public boolean updateStatus(UsersEntity entity) {

        boolean result = false;

        StringBuilder sql = new StringBuilder();
        sql.append("  update m_mobile_users ");
        sql.append(" set ");
        sql.append("    USER_STATUS =  :USER_STATUS,");
        sql.append("    UPDATE_DATE =  now(),");
        sql.append("    UPDATE_BY = :UPDATE_BY ");
        sql.append(" where ");
        sql.append("    MOBILE_UUID = :MOBILE_UUID ");
        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("USER_STATUS", entity.getUserStatus())
                    .addValue("MOBILE_UUID", entity.getMobileUuid())
                    .addValue("UPDATE_BY", entity.getUsername());

            int rowAffected = template.update(sql.toString(), namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public boolean updateProfilePicture(UsersEntity entity) {

        boolean result = false;

        StringBuilder sql = new StringBuilder();
        sql.append("  update m_mobile_users ");
        sql.append(" set ");
        sql.append("    PROFILE_PICTURE = :PROFILE_PICTURE, ");
        sql.append("    UPDATE_DATE =  now(),");
        sql.append("    UPDATE_BY = :UPDATE_BY ");
        sql.append(" where ");
        sql.append("    MOBILE_UUID = :MOBILE_UUID ");
        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("MOBILE_UUID", entity.getMobileUuid())
                    .addValue("PROFILE_PICTURE", entity.getProfilePicture())
                    .addValue("UPDATE_BY", entity.getUsername());

            int rowAffected = template.update(sql.toString(), namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public boolean updateStatusProfile(UsersEntity entity) {

        boolean result = false;

        StringBuilder sql = new StringBuilder();
        sql.append("  update m_mobile_users ");
        sql.append(" set ");
        sql.append("    USER_STATUS = :USER_STATUS, ");
        sql.append("    UPDATE_DATE =  now(),");
        sql.append("    UPDATE_BY = :UPDATE_BY ");
        sql.append(" where ");
        sql.append("    MOBILE_UUID = :MOBILE_UUID ");

        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("MOBILE_UUID", entity.getMobileUuid())
                    .addValue("USER_STATUS", entity.getUserStatus())
                    .addValue("UPDATE_BY", entity.getUsername());

            int rowAffected = template.update(sql.toString(), namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public List<UsersEntity> getUserIsDeleteOverDueDate() {
        List<UsersEntity> items = null;

        try {
            StringBuilder sql =  new StringBuilder();
            sql.append(" select * from m_mobile_users where USER_STATUS = 'D' and DATEDIFF(now(), UPDATE_DATE) > 14 ");

            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            items = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(UsersEntity.class));
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return items;
    }

    public boolean deleteUserIsStatusDeleteOverDueDate(String uuid) {
        boolean result = false;

        try {

            StringBuilder sql = new StringBuilder();
            sql.append(" delete from m_mobile_users where  USER_STATUS = 'D' and MOBILE_UUID = :MOBILE_UUID ");

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("MOBILE_UUID", uuid);

            int rowAffected = template.update(sql.toString(), namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

}
