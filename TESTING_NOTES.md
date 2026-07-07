# NetNovel Unit Testing Notes

This file tracks the current unit-test setup and the next safe places to expand coverage.

## Current Test Commands

Run server tests:

```bash
cd netnovel-server
mvn.cmd test
```

Run crawler tests:

```bash
cd netnovel-crawler
mvn.cmd test
```

Run frontend tests:

```bash
cd netnovel-client
npm.cmd run test:run
```

Note: the frontend `test:run` script uses `--pool=threads --maxWorkers=1` to keep Vitest stable on this Windows workspace. Direct `vitest run` can hit fork-worker startup timeouts on heavier runs.

## Server

Current focus: fast unit tests that do not connect to PostgreSQL, RabbitMQ, Elasticsearch, AWS, or Cloudinary.

Covered areas:

- Controller contracts with standalone MockMvc
  - `AuthController`: login, register, logout, current user
  - `ChapterController`: all-chapters list, chapter content, create, update, delete
  - `NovelController`: list pagination, search pagination, latest-updates pagination, completed pagination, updated date-range pagination, detail, create, update, delete
  - `BookmarkController`: my-bookmark detail, bookmark existence check, generic bookmark create, chapter bookmark create, novel bookmark delete
  - `CommentController`: replies, context chain, create novel comment, create reply, update comment, user delete, moderation delete
  - Note: standalone tests stay focused on direct route/body/status contracts; selected `Page<T>` JSON routes are covered in MVC slice tests below.
- MVC slice tests with `@WebMvcTest`
  - `CommentController`: novel root comments and chapter root comments with real Spring MVC `Pageable` binding and `Page<T>` JSON serialization
  - `BookmarkController`: all bookmarks and chapter bookmarks with real Spring MVC `Pageable` binding and `Page<T>` JSON serialization
  - Note: security filters are disabled in this slice so the tests stay focused on endpoint mapping/Jackson/Page behavior. JWT dependencies are mocked only so the MVC context can start.
- Security MVC slice tests with `@WebMvcTest` + real `SecurityConfig`
  - public `GET /api/novels/{novelId}` is accessible without authentication
  - anonymous `POST /api/novels` is rejected before reaching `NovelService`
  - authenticated users can post to `POST /api/novels`
  - public `GET /api/comments/{commentId}/replies` is accessible without authentication
  - regular `USER` role is rejected by moderation delete
  - `MANAGER` role can call moderation delete
  - anonymous users are rejected from bookmark list APIs before reaching `BookmarkService`
  - authenticated users can call bookmark list APIs
  - regular `USER` role is rejected from tag catalog APIs
  - `MANAGER` role can call tag catalog APIs
  - regular `USER` role is rejected from crawl task APIs
  - `MANAGER` role can call crawl task APIs
  - `MANAGER` role is rejected from crawl chapter record APIs
  - `ADMIN` role can call crawl chapter record APIs
  - regular `USER` role is rejected from admin user APIs
  - `ADMIN` role can call admin user APIs
  - `MANAGER` role can call user-event data reports
  - `MANAGER` role is rejected from user-novel interaction rebuild
  - `ADMIN` role can rebuild user-novel interactions
  - `MANAGER` role is rejected from advanced search reindex
  - `ADMIN` role can call advanced search reindex
  - `MANAGER` role is rejected from manual user notifications
  - `ADMIN` role can send manual user notifications
  - Note: JWT parsing is not the target of these tests. `JwtService` and `CustomUserDetailsService` are mocked so the real JWT filter can be constructed while `@WithMockUser` supplies authentication.
- `JwtAuthenticationFilter`
  - Bearer token from `Authorization` header authenticates and continues the filter chain
  - notification stream can authenticate from `access_token` query parameter
  - invalid token clears `SecurityContext` and still continues the filter chain
  - missing token skips JWT lookup and continues the filter chain
- `AuthService`
  - register success path
  - duplicate email rejection
  - password login success path
  - wrong password rejection
  - Google account rejection for password login
  - refresh token rotation
  - access-token rejection in refresh-token flow
- `ChapterService`
  - create chapter success path
  - missing novel rejection on create
  - duplicate chapter number rejection on create
  - update chapter success path
  - same-number update without duplicate lookup
  - duplicate chapter number rejection on update
  - delete chapter with bookmark counter adjustment
- `NovelService`
  - create novel success with genre/tag resolution
  - duplicate title rejection before relationship lookup
  - unknown genre rejection
  - invalid status rejection
  - update novel success with relationship replacement
  - missing novel rejection on update
  - delete novel follower notifications before deletion
- `BookmarkService`
  - bookmark request target validation
  - create novel bookmark success
  - duplicate novel bookmark rejection
  - create chapter bookmark success
  - delete novel bookmark with counter decrement
  - missing chapter bookmark deletion rejection
- `CommentService`
  - create novel comment with content trimming
  - create chapter comment with chapter/novel link
  - create reply with parent reply-count increment
  - reply notification for parent owner
  - non-owner update rejection
  - blank update rejection
  - owned comment soft delete
- `TextUtils`
  - null and blank input handling
  - whitespace normalization
  - title-case behavior
- `DateTimeUtils`
  - day, week, month, and year boundaries
- `TokenHashUtils`
  - SHA-256 digest behavior
  - deterministic and input-sensitive hashing
- `HtmlSanitizer`
  - `plainText` null handling, tag removal, and trimming
  - `basicContent` safe formatting preservation for tags such as `p`, `strong`, and safe `https` links
  - unsafe markup removal for `script`, `javascript:` links, `class`, and `style`
  - `safeUrlLikeText` rejection for dangerous schemes: `javascript:`, `data:`, `vbscript:`
  - safe URL/text trimming
- Existing chatbot tests
  - language detection
  - rule-based intent matching

Important note:

- `NetnovelServerApplicationTests` intentionally avoids `@SpringBootTest` for now. The previous full-context smoke test tried to connect to the real configured database and failed when local PostgreSQL credentials were unavailable. Keep full integration tests separate from unit tests.

Suggested next server tests:

- `HtmlSanitizer`
  - mixed-case dangerous schemes such as `JaVaScRiPt:`
  - blank input behavior for URL-like text
  - event-handler attributes such as `onclick`
  - image/error-handler payloads such as `<img src=x onerror=alert(1)>`
- `NovelService`
  - create novel success
  - reject duplicate title/source combinations if applicable
  - validate manager/admin-only flows
- `ChapterService`
  - preview-only chapter list behavior for normal users
  - preview-only unrestricted behavior for manager/admin users
  - get chapter not-found behavior for locked non-preview chapters
- `CommentService`
  - create comment success
  - reject missing novel/chapter
  - delete/edit authorization
- Controller slice tests with `@WebMvcTest`
  - start with paged `NovelController`, `ChapterController`, `BookmarkController`, and `CommentController` endpoints when Page/Jackson behavior needs verification

## Crawler

Current focus: dispatching and text cleaning without network, browser, or database access.

Covered areas:

- `TextCleaner`
  - null input
  - inline whitespace and NBSP cleanup
  - content line-ending normalization
  - excessive blank-line collapse
- `CrawlerAdapterDispatcher`
  - routes JSOUP sources to `JsoupCrawlerAdapter`
  - routes PLAYWRIGHT sources to `PlaywrightCrawlerAdapter`
- `JsoupCrawlerAdapter`
  - delegates `wuxiaworld` sources to `WuxiaworldJsoupCrawler`
  - rejects unsupported JSOUP sources

Suggested next crawler tests:

- Add HTML fixtures under `netnovel-crawler/src/test/resources/fixtures/wuxiaworld`.
- Extract private parsing helpers from `WuxiaworldJsoupCrawler` into a package-private parser class if needed.
- Test Wuxiaworld parsing from fixture HTML:
  - title
  - author
  - description
  - total chapters
  - genres/tags
  - cover image URL
  - chapter content cleanup
- Keep Playwright tests mocked unless browser-level integration tests are intentionally added later.

## Frontend

Current focus: Vitest-based unit tests using jsdom and Testing Library.

Configured dependencies:

- `vitest`
- `jsdom`
- `@testing-library/react`
- `@testing-library/jest-dom`
- `@testing-library/user-event`

Covered areas:

- `cn`
  - class joining
  - Tailwind conflict merging
- `readStorage` / `writeStorage`
  - fallback behavior
  - invalid JSON fallback
  - JSON write/read behavior
- reader settings
  - default settings
  - class-map compatibility with defaults
  - storage key
- novel permissions
  - admin/manager access
  - regular/missing user rejection
- auth storage
  - save/read tokens
  - require both access and refresh token
  - clear tokens
- `ProtectedRoute`
  - guest redirect
  - current-user error redirect
  - loading state
  - authenticated nested-route rendering
- `ChapterForm`
  - create submit payload
  - invalid form validation errors
  - edit-mode initial values
  - edit submit payload
  - cancel callback
  - disabled/saving state
- `NovelForm`
  - create submit payload with selected genres/tags
  - invalid form validation errors
  - edit-mode initial values and loaded tags
  - edit submit payload with updated tag selection
  - loading copy for genre/tag metadata
  - cancel callback and disabled/saving state
  - create cover guidance vs edit upload controls
- API clients
  - `auth-api`: login, register, Google login, current user, logout
  - `chapter-api`: get, list by novel, create, update, delete
  - `comment-api`: target routing, pagination query strings, replies/context, create/update/delete/moderation delete
  - `novel-api`: detail, all/newest/completed list routes, hot/genre search-backed list routes, similar/semantic/hybrid recommendation routes, create/update/delete, view/follow/like/bookmark interactions, tags/genres
- React Query hooks
  - `use-auth`: current-user query enablement, login cache/token side effects, logout cleanup
  - `use-chapters`: query enablement, chapter fetches, create invalidation, update cache write/invalidation
  - `use-comments`: target query enablement, replies/context query enablement, create/reply/update/delete/moderation mutation API calls, comments-cache invalidation
  - `use-novels`: query enablement for detail/tags/genres/similar/interaction, auth-token guard, interaction counter cache merging, create/update/delete cache invalidation/removal

Suggested next frontend tests:

- More React Query hooks
  - `use-collection`
- API clients
  - `bookmark-api` if bookmark client helpers are added
  - `recommendation-api` if recommendation calls move out of `novel-api`
- Page-level smoke tests
  - `novel-create-page`
  - `chapter-create-page`
  - `login-page`

## Current Verification Snapshot

Last verified:

- server: `133` tests passed
- crawler: `9` tests passed
- frontend: `74` tests passed

Known follow-up:

- `npm install` reported 2 dependency vulnerabilities. Review with `npm audit` before deciding whether to run `npm audit fix`.
- Mockito prints a dynamic-agent warning on Java 21. Tests pass, but a future cleanup can configure Mockito as a Java agent if the warning becomes noisy in CI.
