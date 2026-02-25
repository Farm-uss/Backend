# AI Model Service

FastAPI-based model inference service for Farm-US.

## Run (local)

```bash
cd BE/AI
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8001
```

## Endpoints

- `GET /health`: health check
- `POST /predict`: image inference (`multipart/form-data`)
  - `image` (required)
  - `task_type` (optional, default: `DISEASE_CLASSIFICATION`)
  - `capture_id` (optional)

## Environment

Copy `.env.example` to `.env` and update values.

## DB alignment

- ERD-based SQL draft: `sql/vision_inference_tables.sql`
- Mapping notes: `docs/db-contract.md`
