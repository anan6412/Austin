# 生活圈 · Life Circle

一个「附近」场景的全栈练习项目：**发一条带定位的动态，发现附近的人和店**。

- 后端：Spring Boot 3.2 + MyBatis-Plus + MySQL + Redis（GEO）+ 高德开放平台 API
- 前端：Vue 3 + Vite + 自研设计系统（主色 **玫粉色 #FF5C8D** × **薄荷蓝 #33C6B7**）

```
Austin/
├── life-circle-api/      后端（Spring Boot）
└── life-circle-web/      前端（Vue 3 + Vite）
```

---

## 一、这个项目做什么

| 能力 | 实现方式 |
| --- | --- |
| 发帖 | 文字 + 图片（dataURL 或外链） + 经纬度，落 `posts` 表 |
| 地址补全 | 后端调高德**逆地理编码**，把经纬度换成可读地址 |
| 找附近的帖子 | 坐标写入 Redis **GEO**，用 `GEOSEARCH` 按半径查，再回表取详情 |
| 找附近的店 | 后端调高德**周边搜索** `/v3/place/around`，带 Redis 缓存 |
| 缓存失效 | 发帖后 `@CacheEvict(value = "post:nearby", allEntries = true)` |

---

## 二、快速开始

### 1. 数据库

```bash
mysql -u root -p < life-circle-api/src/main/resources/db/schema.sql
```

### 2. Redis

需要一个可用的 Redis（6379）。Windows 可用 Docker：

```bash
docker run -d --name lifecircle-redis -p 6379:6379 redis:7-alpine
```

### 3. 后端配置

复制模板并填入自己的配置：

```bash
cp life-circle-api/src/main/resources/application.example.yml \
   life-circle-api/src/main/resources/application.yml
```

> `application.yml` 已在 `.gitignore` 中忽略 —— 它包含数据库口令与高德 Key，不要提交。

需要填的关键项：

- `spring.datasource.*`：MySQL 连接（库名 `lifecircle`）
- `spring.data.redis.*`：Redis 连接
- `amap.key`：高德 **Web 服务** 类型的 Key（不是 Web 端 JS Key）
- `amap.base-url`：`https://restapi.amap.com`

### 4. 启动后端

```bash
cd life-circle-api
mvn spring-boot:run
# 默认端口 8081（见 application.properties 的 server.port）
```

验证：

```bash
curl http://127.0.0.1:8081/api/hello
# {"code":200,"msg":"success","data":null}
```

### 5. 启动前端

```bash
cd life-circle-web
npm install
npm run dev      # http://127.0.0.1:5173
```

开发模式下 Vite 会把 `/api/**` 代理到 `http://127.0.0.1:8081`，无需处理跨域
（后端本身也开了 `CorsConfig` 允许全部来源）。

---

## 三、接口一览

统一响应体：`{ "code": 200, "msg": "success", "data": ... }`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/hello` | 健康检查 |
| GET | `/api/posts/nearby` | 附近帖子 `?lng&lat&radius`（radius 默认 5000，上限 50000） |
| POST | `/api/posts` | 创建帖子 `{ userId, text, images[], lng, lat }` |
| GET | `/api/shops/nearby` | 附近店铺 `?lng&lat&radius&keyword` |

`PostDTO`：`id / userId / text / images[] / lng / lat / address / createdAt / distance`
（其中 `address`、`distance`、格式化的 `createdAt` 由后端加工，前端不用传）

`ShopDTO`：`name / address / location / distance / type`，字段全为字符串，
Service 层已经处理成可直接渲染的样子。

---

## 四、前端设计说明

主色是**玫粉色**与**薄荷蓝**，二者不是简单拼色，而是一套可互换的语义变量：

```
--brand-*    主角色（默认玫粉 #FF5C8D）
--accent-*   副色  （默认薄荷蓝 #33C6B7）
```

组件只引用角色变量，所以「我的 → 主色调」里一键切换后，
按钮、标签、图表、雷达图、进度条会**整体换肤**，而不是只改一个背景。

前端还额外做了几件事，让后端没起时也能完整走通交互：

- **附近雷达**：自绘 SVG 极坐标，把帖子和店铺按真实相对方位画出来，不依赖任何地图 JS API 或 Key
- **本机图片压缩**：选图后压到最长边 1080px 再转 dataURL 提交，不需要自备图床
- **演示数据降级**：接口不可达时自动切到演示数据，并在页面顶部明确标注「当前展示的是演示数据」
- **位置来源**：浏览器 Geolocation 优先，失败时可用 8 个城市预设或手动输入经纬度

---

## 五、目录结构（前端）

```
life-circle-web/src/
├── api/
│   ├── http.js          axios 实例，统一解包 Result、区分网络层错误
│   ├── index.js         四个接口的封装
│   └── mock.js          演示数据 + 经纬度换算
├── components/          Ico / PostCard / ShopCard / RadarMap / Sheet / Lightbox …
├── composables/
│   └── useToast.js
├── router/index.js      hash 路由（纯静态托管也不会 404）
├── store/app.js         全局状态：身份、位置、主题、后端连通性
├── styles/theme.css     设计系统（玫粉 × 薄荷蓝）
├── utils/format.js      距离 / 时间 / 图片压缩 / 坐标格式化
├── views/               NearbyView · PublishView · ShopsView · MineView
├── App.vue              响应式外壳（桌面侧栏 / 移动底栏）
└── main.js
```

---

## 六、技术栈

后端：Spring Boot 3.2.0 · JDK 17 · MyBatis-Plus 3.5.5 · MySQL 8 · Spring Data Redis · Spring Cache · 高德开放平台

前端：Vue 3.5 · Vite 5 · vue-router 4 · axios · 零 UI 框架（自研 CSS 设计系统）
