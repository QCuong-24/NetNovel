# NetNovel Embedding Service

Local CPU embedding service for NetNovel chatbot/search semantic fallback.

Default model:

```text
intfloat/multilingual-e5-small
```

Endpoints:

- `GET /health`
- `GET /v1/models/current`
- `POST /v1/embeddings`

Example:

```bash
curl -X POST http://localhost:8000/v1/embeddings \
  -H "Content-Type: application/json" \
  -d '{"texts":["how to bookmark a novel"],"inputType":"query"}'
```

For E5 models, the service automatically prefixes inputs:

- `query: ...` for user queries
- `passage: ...` for FAQ/intent documents

