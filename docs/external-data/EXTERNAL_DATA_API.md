# 외부 데이터 API

## 저장

RPA를 포함한 외부 프로그램은 데이터 종류와 관계없이 `POST /api/internal/external-data` 한 곳으로 전송합니다.
헤더는 `Content-Type: application/json`과 `X-Api-Key: <EXTERNAL_DATA_API_KEY>`입니다.
Manager의 `EXTERNAL_DATA_API_KEY` 기본값은 `external-data-service`이며 배포 시 변경할 수 있습니다.

요청 본문:

```json
{
  "dataCategory": "BILLING",
  "periodType": "MONTHLY",
  "payload": {
    "resourceType": "ELECTRICITY",
    "yearMonth": "2026-09",
    "metrics": {
      "contractPower": { "value": 950, "unit": "kW" },
      "appliedPower": { "value": 649, "unit": "kW" },
      "usageKwh": { "value": 161092, "unit": "kWh" },
      "usageDays": { "value": 30, "unit": "day" },
      "laggingPowerFactor": { "value": 84, "unit": "%" },
      "leadingPowerFactor": { "value": 100, "unit": "%" },
      "billingAmount": { "value": 38207650, "unit": "KRW" }
    }
  }
}
```

저장 결과의 `data`에는 생성된 `id`, `dataCategory`, `periodType`, 저장한 `payload`, Manager 수신 시각 `createDt`가 들어갑니다. 동일 업무 기간을 재전송하면 새 행으로 이력을 남깁니다.

다른 자료도 같은 경로를 사용합니다.

| 자료 | dataCategory | periodType | payload의 주요 필드 |
| --- | --- | --- | --- |
| 일별 전기 사용량 | CONSUMPTION | DAILY | `resourceType: "ELECTRICITY"`, `usageDate: "2026-09-21"`, `metrics.usageKwh`, `metrics.prevMonthSameDayKwh`, `metrics.prevYearSameDayKwh` |
| 시간별 전기요금 | BILLING | HOURLY | `resourceType: "ELECTRICITY"`, `usageDateTime: "2026-09-21T14:10:23"`, `metrics.billingAmount` |
| 월별 가스요금 | BILLING | MONTHLY | `resourceType: "GAS"`, `yearMonth`, `metrics.billingAmount` |
| 실시간 전기 단가 | RATE | REALTIME | `resourceType: "ELECTRICITY"`, 업무 기준 시각 필드, `metrics.unitPrice` |

각 `metrics` 항목은 `{ "value": 숫자, "unit": "단위" }` 형식으로 보냅니다. 월별 전기요금과 일별 전기 사용량에서 기존 조회 형식을 유지하려면 위 표의 기존 필드명을 그대로 사용해야 합니다. `usageDateTime`은 현지 시각의 ISO 형식이며, 시간별 최신값 조회에서는 초를 버리고 같은 분의 가장 최근 수신 건을 사용합니다.

일별 전기 사용량 요청 예시:

```json
{
  "dataCategory": "CONSUMPTION",
  "periodType": "DAILY",
  "payload": {
    "resourceType": "ELECTRICITY",
    "usageDate": "2026-09-21",
    "metrics": {
      "usageKwh": { "value": 1234.5, "unit": "kWh" },
      "prevMonthSameDayKwh": { "value": 1200, "unit": "kWh" },
      "prevYearSameDayKwh": { "value": 1100, "unit": "kWh" }
    }
  }
}
```

시간별 전기요금 요청 예시:

```json
{
  "dataCategory": "BILLING",
  "periodType": "HOURLY",
  "payload": {
    "resourceType": "ELECTRICITY",
    "usageDateTime": "2026-09-21T14:10:23",
    "metrics": {
      "billingAmount": { "value": 52340, "unit": "KRW" }
    }
  }
}
```

## 조회

- 원본 이력: `GET /api/internal/external-data?dataCategory=BILLING&periodType=MONTHLY&page=1&size=50`. `receivedFrom`, `receivedTo`에는 UTC 오프셋을 포함한 ISO 시각을 사용할 수 있습니다. `page`는 1부터 시작합니다.
- 단건: `GET /api/internal/external-data/{id}`.
- 기존 월별 전기요금: `GET /api/manager/electricity-bill/monthly`, `GET /api/manager/electricity-bill/monthly/chart`.
- 기존 시간별 전기요금: `GET /api/manager/electricity-bill/hourly/latest`.
- 기존 일별 전기 사용량: `GET /api/manager/power-usage/daily/yesterday`, `GET /api/manager/power-usage/daily/chart?month=2026-09`.

기존 조회 API는 `resourceType=ELECTRICITY`인 자료만 사용하고, 같은 업무 기간의 수신 이력이 여러 개면 마지막 수신 건만 결과에 반영합니다. 기존 조회 경로는 유지되지만, 기존 월별·시간별·일별 `POST` 경로는 제공하지 않습니다. RPA 송신 프로그램도 새 공통 저장 경로와 API 키로 전환해야 합니다.
