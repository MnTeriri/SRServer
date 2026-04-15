CREATE TABLE sr_task
(
    id                INT PRIMARY KEY AUTO_INCREMENT,
    task_id           CHAR(20)    NOT NULL,
    model_name        VARCHAR(20) NOT NULL,
    scale             INT         NOT NULL,
    input_file        VARCHAR(50) NOT NULL,
    input_width       INT         NOT NULL,
    input_height      INT         NOT NULL,
    input_size_bytes  LONG        NOT NULL,
    output_file       VARCHAR(50),
    output_width      INT,
    output_height     INT,
    output_size_bytes LONG,
    state             INT         NOT NULL,
    create_time       DATETIME    NOT NULL,
    finish_time       DATETIME,
    CONSTRAINT task_id_unique_index UNIQUE (task_id)
);