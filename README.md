# BFH Qualifier 1 – Spring Boot

This project automates the full flow required by the assignment:

- On startup, it calls the **generateWebhook** API with your name, regNo, email.
- It selects the correct SQL file based on the **last two digits** of your regNo (odd → Q1, even → Q2).
- It saves the final SQL query locally (H2).
- It POSTs the query to the **webhook URL** using the returned **JWT token** in the `Authorization` header.

> Update your details and SQL files, build the JAR, run it, and you're done.

## 1) Prereqs
- Java 17+
- Maven 3.8+

## 2) Configure your details
Edit `src/main/resources/application.yml`:

```yaml
app:
  participant:
    name: "Your Name"
    regNo: "REG12347"
    email: "you@example.com"
```

## 3) Put your final SQL
- For **odd** last two digits → put SQL into `src/main/resources/sql/question1.sql`
- For **even** last two digits → put SQL into `src/main/resources/sql/question2.sql`

> The app automatically chooses the correct file.

## 4) Build
```bash
mvn -q -DskipTests package
```

The JAR will be at `target/bfh-java-qualifier-0.0.1-SNAPSHOT.jar`.

## 5) Run
```bash
java -jar target/bfh-java-qualifier-0.0.1-SNAPSHOT.jar
```

## 6) What it does at startup
1. POST `/hiring/generateWebhook/JAVA` with your participant info.
2. Determine odd/even by the last two digits of `regNo`.
3. Read the corresponding SQL file and store it in H2.
4. Submit the payload `{ "finalQuery": "<YOUR_SQL>" }` to the provided webhook URL (or fallback `/hiring/testWebhook/JAVA`), with header `Authorization: <accessToken>`.

## 7) H2 Console (optional)
If you want to inspect the stored data, enable H2 console by adding to `application.yml`:

```yaml
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
```

Then visit `http://localhost:8080/h2-console` after starting the app. Use JDBC URL `jdbc:h2:mem:bfhdb`.

## 8) Repo checklist for submission
- Public GitHub repo containing:
  - Source code
  - Final JAR
  - A **raw** downloadable link to the JAR
- Include JAR direct link and repo URL in the form.

Good luck!
