from threading import Lock
from typing import Iterable, List

from sentence_transformers import SentenceTransformer

from app.schemas import EmbeddingInputType
from app.settings import Settings


class EmbeddingService:
    def __init__(self, settings: Settings):
        self.settings = settings
        self._model: SentenceTransformer | None = None
        self._dimension: int | None = None
        self._lock = Lock()

    @property
    def loaded(self) -> bool:
        return self._model is not None

    @property
    def dimension(self) -> int | None:
        return self._dimension

    def load(self) -> None:
        if self._model is not None:
            return

        with self._lock:
            if self._model is not None:
                return
            model = SentenceTransformer(self.settings.model_name, device=self.settings.device)
            self._dimension = model.get_sentence_embedding_dimension()
            self._model = model

    def embed(self, texts: List[str], input_type: EmbeddingInputType) -> List[List[float]]:
        self.load()
        assert self._model is not None

        prepared_texts = [self._prepare_text(text, input_type) for text in texts]
        embeddings = self._model.encode(
            prepared_texts,
            batch_size=min(len(prepared_texts), self.settings.max_batch_size),
            convert_to_numpy=True,
            normalize_embeddings=True,
            show_progress_bar=False,
        )

        return embeddings.astype(float).tolist()

    def validate_batch(self, texts: Iterable[str]) -> List[str]:
        cleaned = []
        for text in texts:
            normalized = text.strip()
            if len(normalized) > self.settings.max_text_length:
                normalized = normalized[: self.settings.max_text_length]
            cleaned.append(normalized)

        if len(cleaned) > self.settings.max_batch_size:
            raise ValueError(f"Batch size must be <= {self.settings.max_batch_size}")

        return cleaned

    def _prepare_text(self, text: str, input_type: EmbeddingInputType) -> str:
        if input_type == EmbeddingInputType.query:
            return f"query: {text}"
        if input_type == EmbeddingInputType.passage:
            return f"passage: {text}"
        return text
