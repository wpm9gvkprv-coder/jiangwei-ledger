# 酱味账本 前端与数据可视化

项目：基于 Spring Boot 与 Vue 的智能个人记账与消费分析系统。组名：酱味大鸡。
庞润泽（241310122，组长）负责前端与可视化；后端由王天昊（241310124）负责。
本目录提供真实接口驱动的 Vue 应用，包含7项功能对应页面、测试、说明和演示脚本。

## 启动条件

Windows 10/11，Node.js 22.12或以上（本次使用24.22.1）、npm、Edge或Chrome。
后端需要JDK 17、MySQL 8.0。先按相邻 ledger-backend/README.md 创建数据库、导入01及02脚本、设置数据库账号和随机JWT密钥，再启动后端。默认后端端口8080。

在本目录打开终端：

```powershell
npm ci
Copy-Item .env.example .env.local
npm run dev
```

打开 http://127.0.0.1:5173 。`.env.local` 的 `API_TARGET` 要与后端地址一致。此变量供开发/预览代理使用，不打入浏览器；修改后需重启前端。API访问失败时先检查后端和数据库，再检查接口地址。

可直接在页面注册新账号，也可运行 `node scripts/seed-demo.mjs` 通过真实接口创建专用模拟账号；脚本默认连8080，可设 `BACKEND_URL=http://127.0.0.1:8088/api`。脚本只新增自己的演示数据，不清空任何账户。

## 构建和本地预览

```powershell
npm run build
npm run preview
```

构建输出为dist，预览地址默认 http://127.0.0.1:4173 。后端环境变量 `CORS_ORIGINS` 应包含使用的前端地址，例如 `http://127.0.0.1:5173,http://127.0.0.1:4173`，随后重启后端。
若保持默认跨域配置，可在停止开发服务后执行 `npm run preview -- --port 5173`。
不能直接双击dist/index.html运行。部署时用静态Web服务托管dist，并把 `/api` 反向代理到后端；Hash路由无需服务端路由回退。不得把数据库密码或JWT密钥写进前端。

## 验证

```powershell
npm test
# 先启动独立测试用后端和前端服务
$env:BACKEND_URL='http://127.0.0.1:8088/api'
$env:FRONTEND_URL='http://127.0.0.1:5173'
npm run test:e2e
```

浏览器测试使用已安装的Microsoft Edge，每项测试建立新的人工模拟账号，数据保留在指定测试库。后端默认测试地址是8088，前端API_TARGET必须指向同一个后端。
没有Edge时可修改playwright.config.js的channel并安装所选浏览器。结果写入test-results/results.json。交付时的真实记录见evidence，测试覆盖范围和限制见docs/03_前端测试与联调记录.md。

## 目录

```text
src/
  components/  导航布局、图表、账单表单、异常详情、状态组件
  lib/         请求、会话、日期金额格式、图表配置
  views/       登录、总览、账单、分类、预算、分析、提醒、个人信息
  router.js    页面路由和登录检查
tests/         6项逻辑测试和10项真实接口浏览器测试
scripts/       新建模拟账号及演示账单
docs/          页面接口规划、用户说明、测试记录、阶段成果、截图
evidence/      实际测试及构建记录
dist/          已构建的静态资源（交付包内提供）
```

各页面按路由延迟加载，图表使用SVG并监听容器尺寸变化，离开页面销毁图表。
Token存于当前标签页sessionStorage，刷新仍可登录，关闭标签页后需重登；退出会使同账号所有既有令牌失效。前端路由检查仅改善交互，安全边界由后端权限校验保证。

## 阶段与交付说明

第一周至第四周为课程阶段归档编号，不代表实际跨越四个自然周。已完成真实前后端自动化联调，现场双人确认和课堂答辩由同学完成。旧王天昊周报保留了后端交付时的状态，最新联调结果以本次报告为准。
Git使用独立pang-frontend分支；未向远端发布。源码和版本bundle均随交付提供。
