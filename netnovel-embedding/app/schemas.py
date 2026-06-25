from enum import Enum
from typing import List

from pydantic import BaseModel, Field, field_validator


class EmbeddingInputType(str, Enum):
    query = "query"
    passage = "passage"
    raw = "raw"


class EmbeddingRequest(BaseModel):
    texts: List[str] = Field(min_length=1)
    inputType: EmbeddingInputType = EmbeddingInputType.query

    @field_validator("texts")
    @classmethod
    def validate_texts(cls, texts: List[str]) -> List[str]:
        cleaned = [text.strip() for text in texts if text and text.strip()]
        if not cleaned:
            raise ValueError("At least one non-empty text is required")
        return cleaned


class EmbeddingResponse(BaseModel):
    model: str
    dimension: int
    embeddings: List[List[float]]


class CurrentModelResponse(BaseModel):
    model: str
    dimension: int | None
    device: str
    loaded: bool
    maxBatchSize: int
    maxTextLength: int


class HealthResponse(BaseModel):
    status: str
    model: str
    loaded: bool
