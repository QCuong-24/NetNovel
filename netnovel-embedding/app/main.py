from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException

from app.embedding_service import EmbeddingService
from app.schemas import CurrentModelResponse, EmbeddingRequest, EmbeddingResponse, HealthResponse
from app.settings import get_settings


settings = get_settings()
embedding_service = EmbeddingService(settings)


@asynccontextmanager
async def lifespan(app: FastAPI):
    if settings.preload:
        embedding_service.load()
    yield


app = FastAPI(
    title="NetNovel Embedding Service",
    version="0.1.0",
    lifespan=lifespan,
)


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(
        status="ok",
        model=settings.model_name,
        loaded=embedding_service.loaded,
    )


@app.get("/v1/models/current", response_model=CurrentModelResponse)
def current_model() -> CurrentModelResponse:
    return CurrentModelResponse(
        model=settings.model_name,
        dimension=embedding_service.dimension,
        device=settings.device,
        loaded=embedding_service.loaded,
        maxBatchSize=settings.max_batch_size,
        maxTextLength=settings.max_text_length,
    )


@app.post("/v1/embeddings", response_model=EmbeddingResponse)
def embeddings(request: EmbeddingRequest) -> EmbeddingResponse:
    try:
        texts = embedding_service.validate_batch(request.texts)
        vectors = embedding_service.embed(texts, request.inputType)
    except ValueError as exception:
        raise HTTPException(status_code=400, detail=str(exception)) from exception

    return EmbeddingResponse(
        model=settings.model_name,
        dimension=embedding_service.dimension or 0,
        embeddings=vectors,
    )
