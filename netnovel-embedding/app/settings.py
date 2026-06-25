from functools import lru_cache
from pydantic import Field
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    model_name: str = Field(default="intfloat/multilingual-e5-small", alias="EMBEDDING_MODEL")
    device: str = Field(default="cpu", alias="EMBEDDING_DEVICE")
    preload: bool = Field(default=True, alias="EMBEDDING_PRELOAD")
    max_batch_size: int = Field(default=16, alias="EMBEDDING_MAX_BATCH_SIZE")
    max_text_length: int = Field(default=2048, alias="EMBEDDING_MAX_TEXT_LENGTH")

    class Config:
        populate_by_name = True


@lru_cache
def get_settings() -> Settings:
    return Settings()
