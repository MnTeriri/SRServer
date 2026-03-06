CREATE TABLE sr_task
(
    id          INT PRIMARY KEY AUTO_INCREMENT,
    task_id     CHAR(20)    NOT NULL,
    model_name  VARCHAR(20) NOT NULL,
    scale       INT         NOT NULL,
    input_file  VARCHAR(50) NOT NULL,
    output_file VARCHAR(50),
    state       INT         NOT NULL,
    create_time DATETIME    NOT NULL,
    finish_time DATETIME,
    CONSTRAINT task_id_unique_index UNIQUE (task_id)
);