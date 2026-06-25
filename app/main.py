from fastapi import FastAPI

from app.api.runtime_api import router as runtime_router
from app.runtime.runtime_service import RuntimeService
from app.storage.runtime_store import RuntimeStore

app = FastAPI(title="ClinMindRuntime", version="0.1.0")


@app.on_event("startup")
def startup() -> None:
    store = RuntimeStore()
    app.state.runtime_store = store
    app.state.runtime_service = RuntimeService(store)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


app.include_router(runtime_router)
