package com.seaman.controller;

import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health", description = "ตรวจสอบสถานะการทำงานของ API")
@RestController
@RequiredArgsConstructor
public class HomeController {

    @Operation(summary = "ข้อมูล Application", description = "แสดงชื่อแอปพลิเคชัน")
    @GetMapping("/")
    public String index() {
        return AppSys.APPLICATION_NAME;
    }

    @Operation(summary = "Health Check", description = "ตรวจสอบว่า API ทำงานปกติ")
    @GetMapping(Routes.HEALTH)
    public String health(){
        return "Success";
    }

}
