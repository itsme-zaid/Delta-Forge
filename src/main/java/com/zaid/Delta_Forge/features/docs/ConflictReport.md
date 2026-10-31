# Conflict Report Contract

## Purpose

One JSON shape for conflict reports, consumed by:
- The CLI (Day 13 style, extended)
- The REST API (Day 41)
- The UI (Day 45)
- The audit trail (Day 50)

## Shape

```json
{
  "mergeId": "m-abc123",
  "status": "UNRESOLVED",
  "conflicts": [
    {
      "conflictId": "c-7a3f",
      "path": "/service/timeout",
      "type": "BOTH_MODIFIED",
      "base": { "present": true, "value": 30 },
      "ours": { "present": true, "value": 60 },
      "theirs": { "present": true, "value": 90 },
      "resolution": null
    }
  ]
}