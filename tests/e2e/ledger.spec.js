import { test, expect } from "@playwright/test";
import { mkdir } from "node:fs/promises";
const backend = process.env.BACKEND_URL || "http://127.0.0.1:8088/api";
const password = "CoursePass123!";
const d = new Date();
const date = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`,
  month = date.slice(0, 7);
async function account(request, seed = false) {
  const username =
    "p_" + Date.now() + "_" + Math.random().toString(36).slice(2, 6);
  await request.post(backend + "/auth/register", {
    data: { username, password, displayName: "课程演示同学" },
  });
  const res = await request.post(backend + "/auth/login", {
    data: { username, password },
  });
  const login = (await res.json()).data;
  expect(login.token).toBeTruthy();
  const headers = { Authorization: "Bearer " + login.token };
  const cats = (
    await (await request.get(backend + "/categories", { headers })).json()
  ).data;
  const exp = cats.find((c) => c.type === "EXPENSE"),
    inc = cats.find((c) => c.type === "INCOME");
  if (seed) {
    for (const amount of [10, 12, 14, 16, 18, 20, 22, 24, 100])
      expect(
        (
          await request.post(backend + "/bills", {
            headers,
            data: {
              categoryId: exp.id,
              type: "EXPENSE",
              amount,
              billDate: date,
              description: "课程模拟午餐 " + amount,
            },
          })
        ).ok(),
      ).toBeTruthy();
    await request.post(backend + "/bills", {
      headers,
      data: {
        categoryId: inc.id,
        type: "INCOME",
        amount: 1000,
        billDate: date,
        description: "课程模拟收入",
      },
    });
    await request.put(backend + "/budgets/" + month, {
      headers,
      data: { amount: 200 },
    });
  }
  return { username, password, headers, cats, exp, inc };
}
async function login(page, a) {
  await page.goto("/#/login");
  await page.getByLabel("用户名", { exact: true }).fill(a.username);
  await page.getByLabel("密码", { exact: true }).fill(a.password);
  await page.getByRole("button", { name: "登录账本" }).click();
  await expect(
    page.getByRole("heading", { name: "收支总览", exact: true }),
  ).toBeVisible();
  await expect(page.locator(".el-loading-mask")).toHaveCount(0);
}
async function nav(page, name) {
  await page
    .getByRole("navigation")
    .getByRole("link", { name, exact: true })
    .click();
  await expect(
    page.getByRole("heading", { name, exact: true }).first(),
  ).toBeVisible();
  await expect(page.locator(".el-loading-mask")).toHaveCount(0);
}
test("注册与空账本、未登录路由控制", async ({ page }) => {
  await page.goto("/#/bills");
  await expect(page).toHaveURL(/login/);
  await page.getByRole("tab", { name: "注册", exact: true }).click();
  await page.getByRole("button", { name: "注册并登录" }).click();
  await expect(page.getByText("请输入用户名", { exact: true })).toBeVisible();
  await page.getByLabel("用户名", { exact: true }).fill("ui_" + Date.now());
  await page.getByLabel("显示名称").fill("庞润泽演示");
  await page.getByLabel("密码", { exact: true }).fill(password);
  await page.getByRole("button", { name: "注册并登录" }).click();
  await expect(page).toHaveURL(/#\/bills/);
  await expect(page.getByText("没有找到相关账单")).toBeVisible();
  await nav(page, "收支总览");
  await expect(page.getByTestId("overview-expense")).toContainText("0.00");
  await expect(page.getByText("本月还没有支出")).toBeVisible();
});
test("真实数据的总览、三类图表、预算和异常详情", async ({ page, request }) => {
  const a = await account(request, true);
  const errors = [];
  page.on("pageerror", (e) => errors.push(e.message));
  await login(page, a);
  await expect(page.getByTestId("overview-expense")).toContainText("236.00");
  await expect(page.getByTestId("overview-income")).toContainText("1,000.00");
  await expect(page.getByTestId("overview-balance")).toContainText("764.00");
  await expect(page.locator(".chart svg")).toHaveCount(2);
  await mkdir("docs/screenshots", { recursive: true });
  await page.screenshot({
    path: "docs/screenshots/01_收支总览.png",
    fullPage: true,
    animations: "disabled",
  });
  await nav(page, "消费分析");
  await expect(page.locator(".chart svg")).toHaveCount(3);
  await expect(page.locator(".el-table__body")).toContainText("236.00");
  await page.screenshot({
    path: "docs/screenshots/02_消费分析.png",
    fullPage: true,
    animations: "disabled",
  });
  await nav(page, "月度预算");
  await expect(page.getByText("已超出预算", { exact: true })).toBeVisible();
  await expect(page.getByText("已经超出 ¥36.00")).toBeVisible();
  await page.getByRole("button", { name: "调整预算" }).click();
  await page.getByLabel("预算金额（元）").fill("295");
  await page.getByRole("button", { name: "保存预算" }).click();
  await expect(page.getByText("接近预算上限", { exact: true })).toBeVisible();
  await nav(page, "异常提醒");
  await expect(page.getByText("¥100.00 的支出值得核对")).toBeVisible();
  await page.getByRole("button", { name: "查看分析" }).click();
  await expect(page.getByRole("dialog")).toContainText("31.00");
  await page.screenshot({
    path: "docs/screenshots/03_异常分析.png",
    fullPage: false,
    animations: "disabled",
  });
  await page.getByRole("button", { name: "关闭此对话框" }).click();
  await page.getByRole("button", { name: "标记已读" }).click();
  await expect(page.getByText("已读", { exact: true })).toBeVisible();
  expect(errors).toEqual([]);
});
test("账单校验、智能推荐确认、编辑筛选和删除", async ({ page, request }) => {
  const a = await account(request);
  await login(page, a);
  await nav(page, "账单明细");
  await page.getByRole("button", { name: "新增账单" }).click();
  let dialog = page.getByRole("dialog");
  await dialog.getByLabel("金额（元）").fill("0");
  await dialog.getByLabel("账单描述").fill("学校食堂午餐");
  await dialog.getByRole("button", { name: "保存账单" }).click();
  await expect(
    dialog.getByText("请输入0.01至9999999999.99之间的金额，最多两位小数"),
  ).toBeVisible();
  await dialog.getByLabel("金额（元）").fill("23.50");
  await dialog.getByRole("button", { name: "智能推荐分类" }).click();
  await expect(dialog.getByRole("button", { name: "采用建议" })).toBeVisible();
  await dialog.getByRole("button", { name: "采用建议" }).click();
  await dialog.getByRole("button", { name: "保存账单" }).click();
  await expect(dialog).toBeHidden();
  await expect(page.locator(".el-table__body")).toContainText("23.50");
  await page.getByRole("button", { name: "编辑", exact: true }).click();
  await dialog.getByLabel("金额（元）").fill("25");
  await dialog.getByRole("button", { name: "保存账单" }).click();
  await expect(dialog).toBeHidden();
  await page.getByLabel("搜索账单描述", { exact: true }).fill("不存在的内容");
  await page.getByRole("button", { name: "查询", exact: true }).click();
  await expect(page.getByText("没有找到相关账单")).toBeVisible();
  await page.getByRole("button", { name: "重置", exact: true }).click();
  await expect(page.locator(".el-table__body")).toContainText("25.00");
  await page.getByRole("button", { name: "删除", exact: true }).click();
  await page.getByRole("button", { name: "确认删除" }).click();
  await expect(page.getByText("没有找到相关账单")).toBeVisible();
});
test("自定义分类增改删与默认分类保护", async ({ page, request }) => {
  const a = await account(request);
  await login(page, a);
  await nav(page, "收支分类");
  await expect(page.getByText("不可编辑", { exact: true })).toBeVisible();
  await page.getByRole("button", { name: "新建分类" }).click();
  await page.getByLabel("分类名称").fill("课程资料");
  await page.getByRole("button", { name: "保存分类" }).click();
  await expect(
    page.getByRole("heading", { name: "课程资料", exact: true }),
  ).toBeVisible();
  await page.getByRole("button", { name: "编辑", exact: true }).click();
  await page.getByLabel("分类名称").fill("专业书籍");
  await page.getByRole("button", { name: "保存分类" }).click();
  await expect(page.getByRole("heading", { name: "专业书籍" })).toBeVisible();
  await page.getByRole("button", { name: "删除", exact: true }).click();
  await page.getByRole("button", { name: "确认删除" }).click();
  await expect(page.getByText("还没有自定义分类")).toBeVisible();
});
test("个人信息、退出登录及服务端令牌失效", async ({ page, request }) => {
  const a = await account(request);
  await login(page, a);
  await page.goto("/#/profile");
  await page.getByLabel("显示名称").fill("新昵称");
  await page.getByRole("button", { name: "保存个人信息" }).click();
  await expect(page.locator(".user-link")).toContainText("新昵称");
  const token = await page.evaluate(() =>
    sessionStorage.getItem("jiangwei-session"),
  );
  await page.getByRole("button", { name: "退出登录", exact: true }).click();
  await page.getByRole("button", { name: "确认退出" }).click();
  await expect(page).toHaveURL(/login/);
  expect(
    (
      await request.get(backend + "/me", {
        headers: { Authorization: "Bearer " + token },
      })
    ).status(),
  ).toBe(401);
});
test("服务端登录过期统一返回登录页", async ({ page, request }) => {
  const a = await account(request);
  await login(page, a);
  await request.post(backend + "/auth/logout", { headers: a.headers });
  await nav(page, "账单明细").catch(() => {});
  await expect(page).toHaveURL(/login/);
  await expect(page.getByText("登录已失效，请重新登录")).toBeVisible();
});
test("请求失败提供重试并恢复", async ({ page, request }) => {
  const a = await account(request);
  await login(page, a);
  await page.route("**/api/bills?**", (r) => r.abort());
  await page
    .getByRole("navigation")
    .getByRole("link", { name: "账单明细" })
    .click();
  await expect(page.getByText("暂时没有加载成功")).toBeVisible();
  await page.unroute("**/api/bills?**");
  await page.getByRole("button", { name: "重新加载" }).click();
  await expect(page.getByText("没有找到相关账单")).toBeVisible();
});
test("手机导航、图表缩放和表单布局", async ({ page, request }) => {
  const a = await account(request, true);
  await page.setViewportSize({ width: 390, height: 844 });
  await login(page, a);
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth,
    ),
  ).toBeTruthy();
  await page.screenshot({
    path: "docs/screenshots/04_手机总览.png",
    fullPage: true,
    animations: "disabled",
  });
  await page.getByRole("button", { name: "展开导航" }).click();
  await nav(page, "账单明细");
  await page.getByRole("button", { name: "新增账单" }).click();
  await expect(page.getByRole("dialog")).toBeVisible();
  const box = await page.getByRole("dialog").boundingBox();
  expect(box.x).toBeGreaterThanOrEqual(0);
  expect(box.x + box.width).toBeLessThanOrEqual(391);
  await page.screenshot({
    path: "docs/screenshots/05_手机记账.png",
    fullPage: false,
    animations: "disabled",
  });
});

test("账单分页、收入筛选与日期筛选", async ({ page, request }) => {
  const a = await account(request, true);
  await request.post(backend + "/bills", {
    headers: a.headers,
    data: {
      categoryId: a.exp.id,
      type: "EXPENSE",
      amount: 5,
      billDate: date,
      description: "分页补充账单",
    },
  });
  await login(page, a);
  await nav(page, "账单明细");
  await expect(page.locator(".el-table__body tr")).toHaveCount(10);
  await page.getByRole("button", { name: "下一页", exact: true }).click();
  await expect(page.locator(".el-table__body tr")).toHaveCount(1);
  await page.getByText("全部收支", { exact: true }).click();
  await page.getByRole("option", { name: "收入", exact: true }).click();
  await page.getByRole("button", { name: "查询", exact: true }).click();
  await expect(page.locator(".el-table__body tr")).toHaveCount(1);
  await expect(page.locator(".el-table__body")).toContainText("课程模拟收入");
  await page.getByRole("button", { name: "重置", exact: true }).click();
  await page.getByPlaceholder("开始日期").fill(date);
  await page.getByPlaceholder("结束日期").fill(date);
  await page.getByPlaceholder("结束日期").press("Enter");
  await page.getByRole("button", { name: "查询", exact: true }).click();
  await expect(page.getByText("共11笔记录")).toBeVisible();
});
test("重复分类错误及已引用分类删除保护", async ({ page, request }) => {
  const a = await account(request);
  const res = await request.post(backend + "/categories", {
    headers: a.headers,
    data: { name: "课程资料", type: "EXPENSE" },
  });
  const c = (await res.json()).data;
  await request.post(backend + "/bills", {
    headers: a.headers,
    data: {
      categoryId: c.id,
      type: "EXPENSE",
      amount: 9,
      billDate: date,
      description: "测试资料",
    },
  });
  await login(page, a);
  await nav(page, "收支分类");
  await page.getByRole("button", { name: "新建分类" }).click();
  await page.getByLabel("分类名称").fill("课程资料");
  await page.getByRole("button", { name: "保存分类" }).click();
  await expect(page.locator(".el-message--error")).toBeVisible();
  await expect(page.getByRole("dialog")).toBeVisible();
  await page
    .getByRole("dialog")
    .getByRole("button", { name: "取消", exact: true })
    .click();
  await page.getByRole("button", { name: "删除", exact: true }).click();
  await page.getByRole("button", { name: "确认删除" }).click();
  await expect(page.locator(".el-message--error").last()).toBeVisible();
  await expect(
    page.getByRole("heading", { name: "课程资料", exact: true }),
  ).toBeVisible();
});


