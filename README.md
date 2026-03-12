# 图片超分辨率服务后端（SRServer）

## 项目说明

SRServer 是一个基于 Spring Boot 构建的分布式后端服务，用于提供图像超分辨率（Super-Resolution, SR）推理服务。 
系统通过消息队列 + 推理服务解耦架构，支持高并发任务提交、异步推理处理以及任务状态管理，适用于 AI 推理服务的后端部署场景。

在模型推理方面，系统基于 Deep Java Library（DJL）实现深度学习模型的加载与执行。
超分辨率模型首先使用 PyTorch 进行训练，然后转换为 TorchScript 格式，以便在 Java 服务中通过 DJL 的 PyTorch Engine 进行高效推理，从而实现 Java 后端与深度学习模型的无缝集成。

系统核心目标：

* 提供稳定、高并发的图像超分辨率推理接口
* 支持多推理节点横向扩展
* 实现任务队列化处理与负载均衡
* 防止服务过载并保证系统稳定性


## 系统特性

该项目实现了完整的图像超分辨率推理服务后端能力，主要功能包括：

[//]: # (### 用户与权限)

[//]: # ()
[//]: # (* 用户注册与登录)

[//]: # (* 用户身份认证与权限控制)

[//]: # (* 用户基础信息管理)

### 配置管理
* 使用 Nacos 作为统一发现、配置中心，对系统中的超分辨率模型参数及推理服务相关配置进行集中管理
* 支持模型参数、推理服务参数等配置的统一维护与动态更新，提高系统的可维护性和灵活性

### 系统保护与限流
* 使用 Sentinel 实现接口限流保护
* 服务整体 QPS 控制
* 推理任务队列积压保护
* 防止系统在高并发情况下过载

### 推理任务管理
* 图像上传与推理任务提交
* 支持多模型与多倍率超分任务
* 推理任务状态查询
* 推理结果下载
* 任务历史记录管理

### 推理服务架构
* 使用 RocketMQ 实现任务异步处理
* 推理服务与 API 服务解耦
* 支持多推理节点并行处理
* 推理服务自动负载均衡

### 模型推理
* 使用 Deep Java Library（DJL）在 Java 后端服务中执行深度学习模型推理
* 将 PyTorch 模型转换为 TorchScript 格式，使其能够在 Java 环境中加载运行
* 基于 DJL 的 PyTorch Engine 实现模型加载、推理执行以及结果处理
* 支持部署多个超分辨率模型（如不同倍率或不同架构模型）
* 推理服务节点可水平扩展，提高系统整体吞吐能力

### 存储与缓存
* 使用 Redis 实现缓存与状态管理
* 使用 MySQL 实现推理任务持久化存储

### 容器化部署
* 提供 Docker 容器化部署支持
* 使用 Docker Compose 快速部署完整系统
* 支持多推理节点横向扩展

## 使用的框架

* Spring Boot
* Spring Cloud
* Deep Java Library（DJL）
* Nacos
* Sentinel
* RocketMQ
* Spring Cache
* Spring Redis
* MyBatis
* MyBatis-Plus
* Knife4j
* Docker

## 目录说明
~~~
SRServer/
├── docker/               # docker文件
├── sr-common/            # 公共模块
├── sr-controller/        # Web 接入服务（无 GPU）
├── sr-inference/         # 推理服务（有 GPU）
└── pom.xml               # 公共依赖
~~~

## 系统架构
系统整体架构如下：
~~~
Client
   │
   ▼
SRController (Web 服务)
   │
   ▼
RocketMQ (消息队列)
   │
   ▼
SRInference (推理单元)
   │
   ▼
Result Storage
~~~

任务流程：
1. 用户提交图片超分任务
2. Web 服务生成任务并发送到 RocketMQ
3. 推理服务节点从队列消费任务
4. 执行模型推理
5. 保存推理结果并更新任务状态

docker compose up --scale sr-inference=3 #部署多个实例（需要去除port映射）