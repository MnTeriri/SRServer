package com.example.srcontroller.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.srcommon.model.SRTask;
import org.springframework.stereotype.Repository;

@Repository
public interface ISRTaskDao extends BaseMapper<SRTask> {
}
