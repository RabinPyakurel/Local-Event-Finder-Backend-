# Multipart File Upload Configuration Fix

## Problem
Tomcat 11 (used in Spring Boot 4.x) has strict limits on multipart requests:
- Default `maxFileCount`: 10 files
- Default `maxPartCount`: 10000 parts (but form fields count as parts too)

When creating events with many form fields (title, description, venue, dates, tags, etc.), you can exceed these limits.

## Solution

### Option 1: Start with JVM Arguments (Recommended)
Add these JVM arguments when starting your application:

```bash
java -Dtomcat.util.http.parser.HttpParser.maxFileCount=50 \
     -Dtomcat.util.http.parser.HttpParser.maxPartCount=100 \
     -jar backend-0.0.1-SNAPSHOT.jar
```

### Option 2: Set in IDE Run Configuration
If running from IntelliJ IDEA or Eclipse:
1. Edit Run Configuration
2. Add to VM options:
   ```
   -Dtomcat.util.http.parser.HttpParser.maxFileCount=50
   -Dtomcat.util.http.parser.HttpParser.maxPartCount=100
   ```

### Option 3: Set as Environment Variables (Windows)
```cmd
set JAVA_OPTS=-Dtomcat.util.http.parser.HttpParser.maxFileCount=50 -Dtomcat.util.http.parser.HttpParser.maxPartCount=100
mvn spring-boot:run
```

### Option 4: Reduce Form Fields
If you cannot set JVM arguments, reduce the number of tags sent in the create event request.

## Verification
The application startup logs will show:
```
✓ Tomcat multipart limits configured:
  - maxFileCount: 50
  - maxPartCount: 100
```

## Important Note
Make sure you're registered with the **ORGANIZER** role, not USER role, to access the create event endpoint.

Register with:
```json
{
  "fullName": "Test Organizer",
  "email": "organizer@test.com",
  "password": "password123",
  "dob": "1990-01-01",
  "interests": ["MUSIC", "SPORTS"],
  "role": "ORGANIZER"
}
```
