package com.csrc.tool.controller;



import com.csrc.tool.config.report;
import com.csrc.tool.service.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping(path = "/csrc")
public class ToolController {

   @Autowired
   private ToolService toolService;

    @GetMapping(path = "/generate")
    public String generate(Model model, @RequestParam(value = "subdate") String subdate, @RequestParam(value = "reportid") String reportid, HttpServletResponse response){
        if(reportid!=null&&subdate!=null) {
            String[] reportids = reportid.split(",");
            toolService.generate(subdate,reportids);
        }
        return "tool";
    }




    @GetMapping(path = "/tool")
    public String tool(Model model){
        List<report> reportdata=toolService.reportdata();;
        Map<String,List<report>> sqlname=new HashMap<String,List<report>>();
        sqlname.put("reportlist",reportdata);
        model.addAllAttributes(sqlname);
        return "index";
    }
}
