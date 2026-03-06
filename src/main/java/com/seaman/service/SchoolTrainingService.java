package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.ListSchoolTrainingEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.CommonException;
import com.seaman.model.response.SchoolTrainingResponse;
import com.seaman.repository.SchoolTrainingRepository;
import com.seaman.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SchoolTrainingService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final SchoolTrainingRepository schoolTrainingRepository;
    private final TransactionLogsService transactionLogsService;

    private final DateUtil dateUtil;

    public SchoolTrainingResponse listSchoolTraining(HttpServletRequest httpServletRequest, String courseCode) {

        SchoolTrainingResponse response = new SchoolTrainingResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "SCHOOL TRAINING CHECKED";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            List<ListSchoolTrainingEntity> listSchoolTrainingEntities = schoolTrainingRepository.listSchoolTrainings(courseCode);
            log.info("Module -> {} -> listSchoolTrainingEntities is size -> {}, by course code -> {}", serviceName, listSchoolTrainingEntities.size(), courseCode);
            List<ListSchoolTrainingEntity> result = new ArrayList<>();

            Map<String, ListSchoolTrainingEntity> mapResult = new HashMap<>();

            for (ListSchoolTrainingEntity item : listSchoolTrainingEntities) {
                String keyMap = item.getCompanyCode() + item.getCourseCode();
                if (!mapResult.containsKey(keyMap)) {
                    mapResult.put(keyMap, item);
                }
            }

            for (Map.Entry<String, ListSchoolTrainingEntity> entry : mapResult.entrySet()) {

                // Calculate start date, End date
                ListSchoolTrainingEntity item = entry.getValue();

                List<String> onlinesDates = new ArrayList<>();
                if(null != item.getCourseOnlineDate() && !"".equals(item.getCourseOnlineDate())) {
                    onlinesDates = Arrays.asList(item.getCourseOnlineDate().split("\\s*,\\s*"));
                }

                List<String> onsiteDates = new ArrayList<>();
                if(null != item.getCourseOnsiteDate() && !"".equals(item.getCourseOnsiteDate())) {
                    onsiteDates =  Arrays.asList(item.getCourseOnsiteDate().split("\\s*,\\s*"));
                }

                List<String>  onsiteDatesRenew = this.convertDateForCal(onsiteDates);
                List<String>  onlinesDatesRenew = this.convertDateForCal(onlinesDates);

                List<String> dateAllList = new ArrayList<>();
                dateAllList.addAll(onsiteDatesRenew);
                dateAllList.addAll(onlinesDatesRenew);

                Collections.sort(dateAllList);

                String startDate = "";
                String endDate = "";

                if(dateAllList.size() > 0) {
                    startDate = dateUtil.formatStrToStrDDMMYYYY(dateAllList.get(0), DateUtil.YYYYMMDD);
                    endDate = dateUtil.formatStrToStrDDMMYYYY(dateAllList.get(dateAllList.size() - 1), DateUtil.YYYYMMDD); ;
                }

                item.setCourseStartDate(startDate);
                item.setCourseEndDate(endDate);

                result.add(item);
            }

            // Short collection
            Collections.sort(result);

            response.setSchoolTrainings(result);

        } catch (CommonException ce) {
            log.error("Module -> {} -> exception -> {}", serviceName, String.valueOf(ce));
            log.error("{}", ce);
            throw ce;
        } catch (Exception ex) {
            log.error("Module -> {} -> exception -> {}", serviceName, String.valueOf(ex));
            log.error("{}", ex);
            throw ex;
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    private List<String> convertDateForCal(List<String> dateList) {

        List<String> reformatDate = new ArrayList<>();

        for(String item : dateList) {
            String dateFormat = dateUtil.formatStrToStr(item, DateUtil.DDMMYYYY);
            reformatDate.add(dateFormat);
        }

        return reformatDate;
    }

    public SchoolTrainingResponse schoolTrainingDetail(HttpServletRequest httpServletRequest, String companyCode, String courseCode) {

        SchoolTrainingResponse response = new SchoolTrainingResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "TRAINING SCHEDULE CHECKED";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            List<ListSchoolTrainingEntity> listSchoolTrainingEntities = schoolTrainingRepository.schoolTrainingsDetail(companyCode, courseCode);
            response.setSchoolTrainings(listSchoolTrainingEntities);

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            log.error("{} error -> {}", serviceName, ex);
            throw ex;
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }
}
