package com.csrc.tool.mapper;


import com.csrc.tool.config.pfid;
import com.csrc.tool.config.report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
@Mapper
public interface ToolMapper {

    /**
     * 生成
     * @param reportsql sql语句
     * @return
     */
    List<Map<String, Object>> generate(@Param("reportsql") String reportsql);
    /**
     * 业务日期
     * @param days T日 subdate 报送日期
     * @return
     */
    String ywdate(@Param("days") String days, @Param("subdate") String subdate);
    /**
     * 查询报表
     * @param
     * @return
     */
    List<report> report();
    /**
     * 查询产品T日
     * @param
     * @return
     */
    List<pfid> pfid();
}