package com.seaman.controller;

import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.service.DeleteUserMobileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HomeController {



    @GetMapping("/")
    public String index() {
        return AppSys.APPLICATION_NAME;
    }

    @GetMapping(Routes.HEALTH)
    public String health(){
        return "Success";
    }

//    @GetMapping("/v1/test")
//    public String test(){
//        deleteUserMobileService.deleteUserIsOverDueDate();
//        return "Success";
//    }

}

