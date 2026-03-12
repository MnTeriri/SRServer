package com.example.srcontroller.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
public class SentinelConfig {

    public SentinelConfig() {
        log.debug("创建Configuration对象：{}", this);
    }

    @PostConstruct
    public void initFlowRules() {
        FlowRule rule = new FlowRule();
        rule.setResource("submitTask");// 与 @SentinelResource value 对应
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(1);// QPS限制
        rule.setLimitApp("default");

        FlowRuleManager.loadRules(List.of(rule));
    }
}
