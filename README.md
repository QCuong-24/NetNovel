# netnovel-crawler

Background worker for NetNovel crawl jobs.

It consumes `crawl.novel.request` messages from RabbitMQ, resolves the URL against configured sources, and updates `crawl_tasks` in the shared database.

Supported sources are configured with:

```text
APP_CRAWLER_SUPPORTED_SOURCES=sourceName|example.com|JSOUP;sourceName2|dynamic.example|PLAYWRIGHT
```
