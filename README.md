SRServer/
├── sr-common/            # 公共模块
├── sr-controller/        # Web 接入服务（无 GPU）
├── sr-inference/         # 推理微服务（有 GPU）
└── docker-compose.yml


docker compose up --scale sr-inference=3 #部署多个实例（需要去除port映射）