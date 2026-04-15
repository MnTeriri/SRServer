package com.example.srcontroller.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.srcommon.model.SRTask;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISRTaskDao extends BaseMapper<SRTask> {
    @Select("SELECT * FROM sr_task " +
            "ORDER BY id " +
            "DESC LIMIT #{start},#{pageSize}")
    public List<SRTask> searchSRTaskList(Integer start, Integer pageSize);
}
