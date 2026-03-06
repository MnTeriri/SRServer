package com.example.srcommon.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true)
@TableName("sr_task")
public class SRTask {
    @TableId(type = IdType.AUTO)
    private Integer id;//自增id
    private String taskId;//任务id
    //private String uid;
    private String modelName;//模型名称
    private Integer scale;//放大倍率
    private String inputFile;//输入文件
    private String outputFile;//输出文件
    private SRTaskState state;//任务状态
    private LocalDateTime createTime;//创建时间
    private LocalDateTime finishTime;//完成时间

    @AllArgsConstructor
    @ToString
    @Getter
    public enum SRTaskState {
        CREATE(0, "已创建"),
        RUNNING(1, "正在执行"),
        FINISH(2, "完成");

        @JsonValue
        @EnumValue
        private final int code;
        private final String name;

        @JsonCreator
        public static SRTaskState fromCode(int code) {
            for (SRTaskState state : values()) {
                if (state.code == code) {
                    return state;
                }
            }
            return null;
        }
    }
}
