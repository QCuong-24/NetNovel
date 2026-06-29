# NetNovel

NetNovel is a full-stack web application for reading, uploading, crawling, searching, and managing novels. The project is organized as a small service-oriented workspace with a React client, a Spring Boot API, a background crawler worker, and a local embedding service for semantic search/chatbot features.

## Features

- Novel reading, upload, and management workflows
- User authentication and role-based API access
- Novel metadata, genres, tags, follows, likes, bookmarks, and reading history
- Preview-only access status support for restricted novels
- Background crawl jobs through RabbitMQ
- Optional Elasticsearch-backed search
- Optional semantic novel recommendations and chatbot retrieval through pgvector embeddings
- Optional audio generation/storage configuration with local storage or AWS Polly
- Cloudinary configuration for media assets
- English/Vietnamese client internationalization

## Project Structure

```text
NetNovel/
  netnovel-client/      React + TypeScript + Vite frontend
  netnovel-server/      Spring Boot API server
  netnovel-crawler/     Spring Boot background crawler worker
  netnovel-embedding/   FastAPI sentence-transformers embedding service
  document/             Project notes and supporting documents
  tools/                Utility scripts and local tooling
  docker-compose.yml    Main local infrastructure and backend stack
  docker-compose.dev.yml
  .env.example          Root Docker Compose environment template
```

## Tech Stack

- Frontend: React 19, TypeScript, Vite, Tailwind CSS, TanStack Query, Axios, React Router
- Backend: Java 21, Spring Boot 4, Spring Security, Spring Data JPA, Spring AMQP
- Database: PostgreSQL 16 with pgvector
- Queue: RabbitMQ
- Search: Elasticsearch 8, optional
- Embeddings: FastAPI, sentence-transformers, CPU PyTorch
- Storage/integrations: Cloudinary, AWS Polly, local audio storage
- Containers: Docker Compose

## Prerequisites

For the Docker setup:

- Docker Desktop or Docker Engine with Docker Compose

For local development without Docker:

- Node.js 20+ and npm
- Java 21
- PostgreSQL 16, ideally with pgvector
- RabbitMQ
- Python 3.11+ for the embedding service
- Elasticsearch 8 if search is enabled

## Quick Start With Docker

1. Copy the root environment template:

```powershell
Copy-Item .env.example .env
```

On macOS/Linux:

```bash
cp .env.example .env
```

2. Edit `.env`.

At minimum, change these values before sharing or deploying the environment:

```env
POSTGRES_PASSWORD=change-me
RABBITMQ_PASSWORD=change-me
APP_JWT_SECRET=change-this-secret-before-deploying-netnovel
```

Optional integrations can be left empty or disabled while developing:

```env
CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
ELASTIC_SEARCH_ENABLED=false
NOVEL_EMBEDDING_ENABLED=false
CHATBOT_EMBEDDING_ENABLED=false
```

3. Start the backend stack:

```powershell
docker compose up --build
```

This starts:

- PostgreSQL on `localhost:5432`
- RabbitMQ on `localhost:5672`
- RabbitMQ Management UI on `http://localhost:15672`
- Elasticsearch on `http://localhost:9200`
- Embedding service inside the Compose network
- API server on `http://localhost:8080`
- Crawler worker

4. Start the client in another terminal:

```powershell
cd netnovel-client
npm install
npm run dev
```

The frontend runs on `http://localhost:5173` by default.

## Development Ports

| Service | Default URL |
| --- | --- |
| Client | `http://localhost:5173` |
| API server | `http://localhost:8080` |
| PostgreSQL | `localhost:5432` |
| RabbitMQ | `localhost:5672` |
| RabbitMQ Management | `http://localhost:15672` |
| Elasticsearch | `http://localhost:9200` |
| Embedding service | `http://localhost:8000` when exposed with `docker-compose.dev.yml` |

To expose infrastructure ports without binding them only to `127.0.0.1`, include the development override:

```powershell
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

## Local Development

### Client

```powershell
cd netnovel-client
Copy-Item .env.example .env
npm install
npm run dev
```

Client environment:

```env
VITE_APP_NAME=NetNovel
VITE_API_BASE_URL=http://localhost:8080/api
VITE_GOOGLE_CLIENT_ID=your_google_client_id.apps.googleusercontent.com
```

Useful commands:

```powershell
npm run build
npm run lint
npm run preview
```

### Server

The server can run locally against Docker-managed PostgreSQL, RabbitMQ, and optional Elasticsearch. The Spring defaults point to `localhost` for local development, so a local `.env` file is mainly a reference unless your IDE or shell loads it into environment variables.

Start infrastructure first:

```powershell
docker compose up postgres rabbitmq elasticsearch embedding
```

Then run the server:

```powershell
cd netnovel-server
.\mvnw.cmd spring-boot:run
```

On macOS/Linux:

```bash
cd netnovel-server
./mvnw spring-boot:run
```

If you use custom database, RabbitMQ, JWT, Cloudinary, AWS, search, or embedding settings, set the corresponding environment variables in your shell/IDE before running Spring Boot.

Useful commands:

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```

### Crawler

The crawler consumes crawl requests from RabbitMQ and writes crawl task updates to the shared PostgreSQL database.

```powershell
cd netnovel-crawler
mvn spring-boot:run
```

Like the server, the crawler defaults to local PostgreSQL and RabbitMQ. Set environment variables only when your local ports, credentials, or supported source list differ from the defaults.

Configure supported sources with:

```env
APP_CRAWLER_SUPPORTED_SOURCES=sourceName|example.com|JSOUP;sourceName2|dynamic.example|PLAYWRIGHT
```

### Embedding Service

The embedding service provides local CPU embeddings for semantic features.

Default model:

```text
intfloat/multilingual-e5-small
```

Run with Docker through the root compose file, or run locally:

```powershell
cd netnovel-embedding
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Health check:

```powershell
curl http://localhost:8000/health
```

Embedding request example:

```bash
curl -X POST http://localhost:8000/v1/embeddings \
  -H "Content-Type: application/json" \
  -d '{"texts":["how to bookmark a novel"],"inputType":"query"}'
```

## Environment Notes

The root `.env.example` is intended for Docker Compose. Module-level `.env.example` files are intended for running services directly during development.

Important environment groups:

- `POSTGRES_*`: database name, user, password, and local port
- `APP_JWT_*`: JWT secret and token lifetimes
- `APP_CORS_ALLOWED_ORIGINS`: allowed browser origins for the API
- `CLOUDINARY_*`: image/media upload integration
- `AWS_*` and `AUDIO_*`: text-to-speech and audio storage settings
- `RABBITMQ_*` and `APP_CRAWL_RABBIT_*`: queue settings for crawler jobs
- `ELASTIC_SEARCH_*`: optional Elasticsearch search
- `EMBEDDING_*`, `NOVEL_EMBEDDING_*`, `CHATBOT_EMBEDDING_*`: embedding service and semantic feature toggles

## Common Workflows

Run everything except the frontend:

```powershell
docker compose up --build
```

Run only infrastructure for local Java development:

```powershell
docker compose up postgres rabbitmq elasticsearch embedding
```

Rebuild one service:

```powershell
docker compose build server
docker compose up server
```

View logs:

```powershell
docker compose logs -f server
docker compose logs -f crawler
```

Stop services:

```powershell
docker compose down
```

Stop services and remove named volumes:

```powershell
docker compose down -v
```

Use the last command carefully because it removes local database, RabbitMQ, Elasticsearch, and model cache data.

## Verification

Backend:

```powershell
cd netnovel-server
.\mvnw.cmd test
```

Frontend:

```powershell
cd netnovel-client
npm run lint
npm run build
```

Crawler:

```powershell
cd netnovel-crawler
mvn test
```

Embedding service:

```powershell
curl http://localhost:8000/health
```

## Module Documentation

Each service also has its own README:

- `netnovel-client/README.md`
- `netnovel-server/README.md`
- `netnovel-crawler/README.md`
- `netnovel-embedding/README.md`
