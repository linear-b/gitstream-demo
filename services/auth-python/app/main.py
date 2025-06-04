from fastapi import FastAPI
from app.auth import router as auth_router

app = FastAPI(
    title="Auth Service",
    version="1.0.0",
    docs_url="/docs",       # Swagger UI
    redoc_url="/redoc"      # ReDoc
)
app.include_router(auth_router, prefix="/auth")

@app.get("/")
def healthcheck():
    return {"status": "ok"}
