"""Run against a fresh demonstration database; creates two dedicated demo accounts.
Usage: python scripts/http_smoke.py http://127.0.0.1:8088
Refuses to overwrite an existing demo account. No third-party services are used.
"""
import json
import sys
from pathlib import Path
from datetime import datetime, timezone, timedelta
from urllib.request import Request, urlopen
from urllib.error import HTTPError

BASE = sys.argv[1] if len(sys.argv) > 1 else "http://127.0.0.1:8088"
OUT = Path(__file__).resolve().parents[1] / "evidence" / "http-smoke.json"
checks = []
day = datetime.now(timezone(timedelta(hours=8))).date().isoformat()
month = day[:7]
def api(method, path, body=None, token=None, expected=200):
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = "Bearer " + token
    req = Request(BASE + "/api" + path, data=json.dumps(body, ensure_ascii=False).encode("utf-8") if body is not None else None, headers=headers, method=method)
    try:
        with urlopen(req, timeout=20) as response:
            status = response.status
            result = json.load(response)
    except HTTPError as error:
        status = error.code
        result = json.load(error)
    assert status == expected, (method, path, status, result)
    checks.append({"method": method, "path": path, "expected": expected, "actual": status})
    return result.get("data")

api("GET", "/health")
for user in ["demo_student", "demo_other"]:
    api("POST", "/auth/register", {"username": user, "password": "DemoPass123!", "displayName": "课程演示用户"})
token = api("POST", "/auth/login", {"username": "demo_student", "password": "DemoPass123!"})["token"]
other = api("POST", "/auth/login", {"username": "demo_other", "password": "DemoPass123!"})["token"]
def body(amount, category=1, kind="EXPENSE"):
    return {"categoryId": category, "type": kind, "amount": amount, "billDate": day, "description": "人工构造演示账单"}
for amount in [10, 12, 14, 16, 18, 20, 22, 24]:
    api("POST", "/bills", body(amount), token)
bill_id = api("POST", "/bills", body(100), token)["id"]
api("POST", "/bills", body(1000, 6, "INCOME"), token)
summary = api("GET", "/statistics/summary?month=" + month, token=token)
assert summary["income"] == 1000 and summary["expense"] == 236 and summary["balance"] == 764
budget = api("PUT", "/budgets/" + month, {"amount": 200}, token)
assert budget["status"] == "EXCEEDED" and budget["usageRate"] == 118
classification = api("POST", "/intelligence/classify", {"description": "学校食堂午餐", "type": "EXPENSE"}, token)
assert classification["categoryName"] == "餐饮"
anomaly = api("GET", f"/intelligence/bills/{bill_id}/anomaly", token=token)
assert anomaly["status"] == "ANOMALY" and anomaly["upperBound"] == 31
api("GET", f"/bills/{bill_id}", token=other, expected=404)
assert api("GET", "/bills", token=other)["total"] == 0
api("PUT", f"/bills/{bill_id}", body(20), token)
assert api("GET", "/alerts", token=token)["total"] == 0
api("PUT", f"/bills/{bill_id}", body(100), token)
assert api("GET", "/alerts", token=token)["total"] == 1
api("POST", "/auth/logout", token=token)
api("GET", "/me", token=token, expected=401)
OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_text(json.dumps({"executedAt": datetime.now(timezone(timedelta(hours=8))).isoformat(), "baseUrl": BASE, "requestsPassed": len(checks), "checks": checks, "summary": summary, "budget": budget, "classification": classification, "anomaly": anomaly, "note": "人工构造数据；不保存认证令牌。"}, ensure_ascii=False, indent=2), encoding="utf-8")
print(f"PASS: {len(checks)} HTTP requests; statistics, budget, classification, anomaly, isolation and logout verified.")
