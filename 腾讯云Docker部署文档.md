# 腾讯云服务器 Docker 容器化部署文档（124.220.70.121）

适用项目：`基于SpringBoot的自习室预约系统`  
部署目标：在腾讯云 `124.220.70.121` 通过 `docker compose` 运行前端 + 后端 + MySQL。

---

## 1. 当前项目已完成的容器化改造

你本地代码已包含以下内容（可直接用于部署）：

- 后端上传返回路径已改为相对路径（避免线上 `localhost` 失效）
- 后端数据库连接支持环境变量
- 新增 `docker-compose.yml`
- 新增后端 Dockerfile：`RoomAppoint.springboot/Dockerfile`
- 新增前端 Dockerfile：`RoomAppoint.elementui/Dockerfile`
- 新增 Nginx 配置：`deploy/nginx/default.conf`
- 新增环境变量样例：`.env.example`
- 新增前端生产环境变量：`RoomAppoint.elementui/.env.production`

---

## 2. 服务器前置准备

> 以下命令在腾讯云服务器上执行（Linux）。

### 2.1 登录服务器

```bash
ssh root@124.220.70.121
```

### 2.2 安装 Docker 与 Compose 插件（Ubuntu/Debian）

```bash
apt update
apt install -y ca-certificates curl gnupg lsb-release
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo $VERSION_CODENAME) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
apt update
apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
systemctl enable docker
systemctl start docker
```

验证：

```bash
docker --version
docker compose version
```

---

## 3. 腾讯云安全组配置

在腾讯云控制台放通：

- `80/tcp`（必须）
- `443/tcp`（可选，后续 HTTPS）

建议不要对公网放通：

- `3306`
- `7245`

---

## 4. 上传/拉取项目代码

你可以任选其一：

### 方案 A：服务器直接 git 拉取

```bash
cd /opt
git clone <你的仓库地址> roomappoint
cd /opt/roomappoint
```

### 方案 B：本地打包上传

把当前项目目录上传到服务器，例如：

```bash
scp -r "基于SpringBoot的自习室预约系统" root@124.220.70.121:/opt/roomappoint
```

---

## 5. 配置环境变量

进入项目根目录：

```bash
cd /opt/roomappoint
```

复制示例文件：

```bash
cp .env.example .env
```

编辑 `.env`（至少修改密码）：

```env
MYSQL_ROOT_PASSWORD=请改成强密码
MYSQL_DB=roomappoint
```

---

## 6. 首次启动

```bash
cd /opt/roomappoint
docker compose up -d --build
```

查看状态：

```bash
docker compose ps
```

查看日志：

```bash
docker compose logs -f backend
docker compose logs -f frontend-nginx
docker compose logs -f mysql
```

---

## 7. 初始化数据库（首次）

如果 MySQL 是全新空库，需要导入 `roomappoint.sql`。

### 7.1 拷贝 SQL 到服务器（如尚未在项目目录）

确保项目根目录存在 `roomappoint.sql`。

### 7.2 执行导入

```bash
docker compose exec -T mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DB" < roomappoint.sql
```

导入后可重启后端：

```bash
docker compose restart backend
```

---

## 8. 验证部署结果

### 8.1 页面访问

浏览器打开：

- `http://124.220.70.121/`

### 8.2 接口验证

```bash
curl -s http://124.220.70.121/api/Banner/List -H 'Content-Type: application/json' -d '{}'
```

返回 `"Success":true` 表示后端联通。

### 8.3 上传与图片验证

上传图片后，确认返回路径是 `/uploads/...`（不是 `localhost`）。

再直接访问：

- `http://124.220.70.121/uploads/xxx/xxx.jpg`

可打开即正常。

---

## 9. 日常运维命令

### 启动

```bash
docker compose up -d
```

### 停止

```bash
docker compose down
```

### 重启某个服务

```bash
docker compose restart backend
docker compose restart frontend-nginx
```

### 更新代码后重建

```bash
git pull
docker compose up -d --build
```

---

## 10. 常见问题排查

### Q1：访问 124.220.70.121 打不开

1. 检查安全组是否开放 80
2. 检查容器是否启动：`docker compose ps`
3. 查看 nginx 日志：`docker compose logs -f frontend-nginx`

### Q2：前端页面开了但接口报错

1. 查看后端日志：`docker compose logs -f backend`
2. 检查数据库容器状态：`docker compose ps`
3. 检查 `.env` 的数据库密码是否正确

### Q3：图片上传成功但显示不出来

1. 确认上传返回是否为 `/uploads/...`
2. 检查该 URL 能否直接访问
3. 查看后端 uploads 挂载是否存在：
   - `docker volume ls`
   - `docker compose exec backend ls -la /app/uploads`

---

## 11. 回滚建议

上线前建议：

1. 备份数据库（`mysqldump`）
2. 记录当前镜像 tag

回滚方式：

- 回退代码版本后执行：

```bash
git checkout <稳定版本tag或commit>
docker compose up -d --build
```

---

如果你愿意，我下一步可以给你一版 **“一键部署脚本 deploy.sh”**（包含拉代码、构建、启动、健康检查），你在服务器执行一次就能完成发布。