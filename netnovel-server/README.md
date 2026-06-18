# netnovel-server
A project to build an own website for reading, uploading and managing novels/documents.

## Run with Docker

Copy `.env.example` to `.env` and update the password before deploying.

```powershell
docker compose up --build
```

The API runs on `http://localhost:8080` by default. PostgreSQL is exposed on `localhost:5432`.
