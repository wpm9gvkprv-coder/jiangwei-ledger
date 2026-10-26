import { test } from "node:test";
import assert from "node:assert/strict";
import {
  validMoney,
  monthRange,
  shiftMonth,
  budgetText,
  pct,
} from "../../src/lib/format.js";
import {
  barOption,
  trendOption,
  pieOption,
  balanceOption,
} from "../../src/lib/charts.js";
test("金额合法边界和小数精度", () => {
  for (const s of ["0.01", "1", "1.2", "9999999999.99"])
    assert.equal(validMoney(s), true, s);
});
test("拒绝零负数科学计数法及超范围金额", () => {
  for (const s of [
    "0",
    "0.00",
    "-1",
    "1.001",
    "1e3",
    "01",
    "NaN",
    "",
    "10000000000",
  ])
    assert.equal(validMoney(s), false, s);
});
test("闰月和年末的月份范围", () => {
  assert.deepEqual(monthRange("2024-02"), ["2024-02-01", "2024-02-29"]);
  assert.deepEqual(monthRange("2025-02"), ["2025-02-01", "2025-02-28"]);
  assert.equal(shiftMonth("2026-01", -1), "2025-12");
});
test("预算已用完和超支分别说明，进度条不溢出", () => {
  assert.equal(budgetText({ status: "EXCEEDED", remaining: 0 }), "预算已用完");
  assert.equal(budgetText({ status: "EXCEEDED", remaining: -1 }), "已超出预算");
  assert.equal(pct(150), 100);
  assert.equal(pct(-1), 0);
});
const rows = [
  { month: "2026-09", income: 0, expense: 0, balance: 0 },
  { month: "2026-10", income: 1000, expense: 236, balance: 764 },
];
test("柱状图及折线图保持接口顺序和金额", () => {
  for (const f of [barOption, trendOption]) {
    const o = f(rows);
    assert.deepEqual(o.xAxis.data, ["2026-09", "2026-10"]);
    assert.deepEqual(o.series[0].data, [0, 1000]);
    assert.deepEqual(o.series[1].data, [0, 236]);
  }
});
test("结余和饼图直接使用接口金额", () => {
  assert.deepEqual(balanceOption(rows).series[0].data, [0, 764]);
  assert.deepEqual(pieOption([{ name: "餐饮", amount: 236 }]).series[0].data, [
    { name: "餐饮", value: 236 },
  ]);
  assert.deepEqual(pieOption([]).series[0].data, []);
});
