package com.hims.controller;


import com.hims.utils.RandomNumGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/generate")

public class GenerateRandomNum {

//    private final RandomNumGenerator randomNumGenerator;
//
//    public GenerateRandomNum(RandomNumGenerator randomNumGenerator) {
//        this.randomNumGenerator = randomNumGenerator;
//    }
//
//    @GetMapping("/randomNum")
//    public String generate(@RequestParam String prefix) {
//        return randomNumGenerator.generateOrderNumber(prefix);
//    }

}
