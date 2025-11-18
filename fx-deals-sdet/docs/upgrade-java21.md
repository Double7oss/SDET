## Java 21 Upgrade Notes

This project has been upgraded from Java 17 to Java 21 (LTS).

### Changes Applied

1. `pom.xml` property `java.version` updated to `21`.
2. Added `maven-enforcer-plugin` rule to require Java 21 at build time.
3. Updated `Dockerfile` base images to Eclipse Temurin Java 21 variants.
4. Retained Spring Boot 3.3.4 (compatible with Java 21) and existing test plugins.

### Local Build

Ensure a JDK 21 is installed and active:

```bash
export JAVA_HOME=$(dirname "$(dirname "$(/usr/libexec/java_home -v 21)"")")
export PATH="$JAVA_HOME/bin:$PATH"
mvn -version
mvn clean verify
```

### Considerations

- JaCoCo 0.8.12 supports Java 21.
- Testcontainers and Rest-Assured versions are compatible; upgrade only if needed for new features.
- If running in environments with older JDK, builds will now fail fast due to the enforcer rule.

### Next Steps

- Monitor runtime performance; Java 21 introduces Virtual Threads (Project Loom) if later adopted.
- Optionally introduce a Maven Toolchains file for stricter JDK resolution.
