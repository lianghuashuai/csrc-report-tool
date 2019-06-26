package com.csrc.tool.service;



import com.csrc.tool.config.pfid;
import com.csrc.tool.config.report;
import com.csrc.tool.mapper.ToolMapper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Clob;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ToolService {
    @Autowired
    private ToolMapper toolMapper;
    @Value("${filepath}")
    private String filepath;
    @Value("${headerfile}")
    private String headerfile;

    /**
     * 生成
     * @param
     * @return
     */
    public void  generate(String subdate, String[]reportid){
        List<report> reportdata=toolMapper.report();
        Map<String,report> reportmap=new HashMap();
        String sql=null;
        String ywdate=null;
        List<List<Object>> result=null;
        List<String> re=null;
        for(report report:reportdata){
            reportmap.put(report.getReportid(),report);
        }
        Map<String,List<Object>> headers=header();
            for (int i = 0; i < reportid.length; i++) {
                report temp = reportmap.get(reportid[i]);
                List<Object> header=headers.get(reportid[i]);
                if ("0".equals(temp.getIsrelateproduct())|"31".equals(temp.getSubmittimelimit())) {
                    if("31".equals(temp.getSubmittimelimit())){
                        Calendar c=Calendar.getInstance();
                        c.set(Integer.valueOf(subdate.substring(0,4)),Integer.valueOf(subdate.substring(4,6)),Integer.valueOf(subdate.substring(6,8)));
                        c.add(Calendar.MONTH, -2);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                        String begindate = sdf.format(c.getTime()); //上月最后一天
                        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMM01");
                        String enddate = sdf2.format(c.getTime()); //上月第一天
                        sql  = sqlreplace(temp.getReportsql()," is not null ",begindate,enddate);
                    }else{
                        ywdate= toolMapper.ywdate(temp.getSubmittimelimit(), subdate);
                        sql  = sqlreplace(temp.getReportsql()," is not null ",ywdate,ywdate);
                    }
                    result = resultdata(sql, reportid[i], header);
                }else{
                    List<pfid> pfids=pfiddata();
                    for(int j=0;j<pfids.size();j++){
                        int days=Integer.valueOf(pfids.get(j).getDays())+Integer.valueOf(temp.getSubmittimelimit());
                        ywdate= toolMapper.ywdate(String.valueOf(days), subdate);
                        sql  = sqlreplace(temp.getReportsql()," ='1060"+pfids.get(j).getPfid()+"' ",ywdate,ywdate);
                        if(j==0) {
                            result = resultdata(sql, reportid[i], header);
                        }else{
                            result.addAll(resultdata(sql, reportid[i], null));
                        }
                    }
                }
                    saveToExcel(result, temp.getReportname());
            }

   }

    /*
     * 获取表头
     * */
    public Map<String,List<Object>> header(){
        XSSFWorkbook xssfWorkbook=null;
        Map<String,List<Object>> listmap=new HashMap<>();
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(headerfile));
            xssfWorkbook = new XSSFWorkbook(fileInputStream);
        }catch (IOException e){
            e.printStackTrace();
        }
        if (xssfWorkbook != null) {
            // 获取第一个Sheet 索引为０
            XSSFSheet sheet = xssfWorkbook.getSheetAt(0);
            // 获取总共有多少行
            for (int i = 0; i < sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i);
                List<Object> list=new ArrayList<>();
                String key=null;
                    for (int k = 0; k < row.getLastCellNum(); k++) {
                        if(k>0) {
                            list.add(row.getCell(k).toString());
                        }else{
                            key=row.getCell(k).toString();
                        }

                    }
                listmap.put(key,list);
            }}
        return listmap;
    }

    /*
    * 获取生成数据添加表头
    * */
    public List<List<Object>> resultdata (String sql,String reportid,List<Object> headers){
        List<List<Object>> result = new ArrayList<>();
        List<Map<String, Object>> tempData=null;
        try {
          tempData = toolMapper.generate(sql);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(reportid);
        }
        Map<String, Object> tempMap = null;
        List<Object> tempList = null;
        if(headers!=null) {
            result.add(headers);
        }
        for (int i = 0; i < tempData.size(); i++) {
            tempMap = tempData.get(i);
            tempList = new ArrayList<>();
            if (tempMap != null) {
                for (String keyName : tempMap.keySet()) {
                    tempList.add(tempMap.get(keyName));
                }
            }
            result.add(tempList);
        }
        return result;
    }

    /*
     * 保存EXCEL
     * */
    public void saveToExcel(List<List<Object>> result, String filename){
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(filename);
        Row row=null;
        List<Object> tableMap=null;
        String val="";
        for (int i = 0; i < result.size(); i++) {
            tableMap = result.get(i);
            row= sheet.createRow(i);
            for (int j = 0; j < tableMap.size(); j++) {
                if(tableMap.get(j)!=null){
                    val=tableMap.get(j).toString();
                    row.createCell(j).setCellValue(val);
                }else{
                    row.createCell(j).setCellValue("");
                }

            }
        }
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(filepath+"/"+filename+".xls"));
            workbook.write(os);
            os.flush();
            os.close();
    } catch (Exception e) {
        e.printStackTrace();
    }



    }

    /*
    * sql参数替换
    * */
   public String sqlreplace(String sql,String pfid,String begindate,String enddate){
       sql=sql.replace("${PFID}", pfid)
               .replace("${FBEGINDATE}", begindate)
               .replace("${FENDDATE}", enddate );
       return sql;
   }


    /**
     * 查询报表
     * @param
     * @return
     */
    public List<report> reportdata(){
        List<report> reportdata=toolMapper.report();
        return reportdata;
    }

    /**
     * 查询产品T日
     * @param
     * @return
     */
    public List<pfid> pfiddata(){
        List<pfid> pfiddata=toolMapper.pfid();
        return pfiddata;
    }




}
