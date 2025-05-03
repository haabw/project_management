package com.coop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TaskViewController {

    // http://localhost:8080/gantt 로 접속하면 templates/gantt.html 보여줌
    @GetMapping("/gantt")
    public String openGanttPage() {
        return "gantt"; // templates/gantt.html
    }
}
