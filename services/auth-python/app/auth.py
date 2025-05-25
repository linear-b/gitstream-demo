import uuid
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel

router = APIRouter()

USER_DB = {
    "alice": "password123",
    "bob": "hunter2"
}

SESSIONS = {}

class LoginPayload(BaseModel):
    username: str
    password: str

@router.post("/login")
async def login(payload: LoginPayload):
    real_password = USER_DB.get(payload.username)
    if real_password and real_password == payload.password:
        token = str(uuid.uuid4())
        SESSIONS[token] = payload.username
        return {"token": token}
    raise HTTPException(status_code=401, detail="Invalid credentials")

@router.get("/validate")
async def validate_token(token: str):
    username = SESSIONS.get(token)
    if not username:
        raise HTTPException(status_code=401, detail="Invalid token")
    return {"username": username}
