package com.seaman.service;

import com.seaman.constant.AppSys;
import com.seaman.constant.BusinessConstant;
import com.seaman.entity.MessageCodeEntity;
import com.seaman.repository.MessageCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

@Service
@RequiredArgsConstructor
public class MessageCodeService {

    private final MessageCodeRepository messageCodeRepository;

    @Cacheable(cacheNames = BusinessConstant.MASTER_MESSAGE_CODE,
            key = "#msgCode + ':' + (#lang == null ? 'EN' : #lang.toUpperCase())",
            sync = true)
    public String getMessageDescription(String msgCode, String lang){

        String msgRes = "";

//        List<MessageCodeEntity> msgList = messageCodeRepository.findAll();
//        for(MessageCodeEntity entity : msgList){
//            if(msgCode.equals(entity.getMessageCode())) {
//                msgRes = AppSys.LANG_EN.equals(lang) ? entity.getMessageDescriptionEn() : entity.getMessageDescriptionTh();
//                break;
//            }
//        }

        MessageCodeEntity item = messageCodeRepository.findByCode(msgCode);
        if(item != null) {
            msgRes  = AppSys.LANG_EN.equalsIgnoreCase(lang)
                    ? item.getMessageDescriptionEn()
                    : item.getMessageDescriptionTh();
        }
        return msgRes.equals("") ? "Message code : " + msgCode + " is not matching in master." : msgRes;
    }
}
