# 🔖 BookMarkr — Personal Bookmark Manager

A minimal, beautiful personal bookmark manager built with Spring Boot, Thymeleaf, and MongoDB Atlas. Sign up, log in, and save, search, and tag your favorite links.

![BookMarkr screenshot placeholder](https://via.placeholder.com/800x450?text=BookMarkr+Dashboard)

## Features

- Sign up / log in / log out with session-based authentication
- Passwords hashed with BCrypt — never stored in plain text
- Add, edit, delete bookmarks (title, URL, description, tags)
- Bookmarks are private — every user only sees and manages their own
- Free-text search across title, description, and URL
- Filter by tag, with a tag bar built from your own tags
- Clean, responsive, modern UI — no frontend framework required

## Technology Stack

- Java 21
- Spring Boot 3.3
- Spring Web (MVC)
- Spring Data MongoDB
- MongoDB Atlas
- Spring Security (session-based, form login)
- Thymeleaf
- Lombok
- Maven

## Project Structure

```
src/main/java/com/example/bookmarkr/
├── BookMarkrApplication.java
├── config/           # SecurityConfig, CustomUserDetailsService
├── controller/        # AuthController, BookmarkController, HomeController
├── model/             # User, Bookmark
├── repository/        # UserRepository, BookmarkRepository
└── service/            # UserService, BookmarkService

src/main/resources/
├── templates/          # login, signup, index, bookmark-form
├── static/css/         # style.css
└── application.properties
```

## Local Setup

### Prerequisites

- Java 21+
- Maven 3.9+
- A MongoDB Atlas account (free tier)

### 1. MongoDB Atlas setup

1. Create a free account at [mongodb.com/cloud/atlas](https://www.mongodb.com/cloud/atlas).
2. Create a new free (M0) cluster.
3. Under **Database Access**, create a database user with a username and password.
4. Under **Network Access**, add your current IP (or `0.0.0.0/0` for testing/deployment).
5. Click **Connect → Drivers**, copy the connection string. It looks like:
   ```
   mongodb+srv://<username>:<password>@<cluster>.mongodb.net/bookmarkr?retryWrites=true&w=majority
   ```

### 2. Environment variable

Set `MONGODB_URI` to that connection string. Never hardcode it or commit it.

```bash
export MONGODB_URI="mongodb+srv://user:pass@cluster.mongodb.net/bookmarkr?retryWrites=true&w=majority"
```

On Windows (PowerShell):

```powershell
$env:MONGODB_URI="mongodb+srv://user:pass@cluster.mongodb.net/bookmarkr?retryWrites=true&w=majority"
```

### 3. Run

```bash
mvn spring-boot:run
```

Visit `http://localhost:8080`.

### 4. Build a jar

```bash
mvn clean package
java -jar target/bookmarkr.jar
```

## Deployment

Any platform that runs a Java jar and lets you set environment variables works (e.g. Render, Railway, Fly.io — check current free-tier availability, as these change over time).

General steps:

1. Push this project to a GitHub repository.
2. Create a new web service on your chosen platform, connected to that repo.
3. Set the build command to `mvn clean package` and the start command to `java -jar target/bookmarkr.jar`.
4. Set the `MONGODB_URI` environment variable in the platform's dashboard (same value as local setup).
5. The app reads `PORT` automatically (`server.port=${PORT:8080}`), so no extra config is needed there.
6. Deploy, then test signup, login, and full bookmark CRUD on the live URL.

## Security Notes

- Passwords are hashed with `BCryptPasswordEncoder` before being saved — plaintext passwords are never stored.
- Authentication is session-based via Spring Security's form login.
- The logged-in user's identity comes from the authenticated session (email), never from a client-supplied ID.
- Every bookmark read/update/delete is scoped by `userId` at the repository level (`findByIdAndUserId`), so one user cannot access or modify another user's bookmarks by manipulating a URL or bookmark ID.
- `MONGODB_URI` is read from an environment variable — no credentials are committed to source control.
