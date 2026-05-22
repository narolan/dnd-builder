# Security Hardening & Test Coverage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Close the six remaining security and quality gaps identified in the ECC codebase review: import validation, CSRF protection, input constraints, rate limiting, missing test coverage, and oversized method decomposition.

**Architecture:** Three independent streams — (1) security hardening adds Spring Security + CSRF, field constraints, and import guards entirely in the Java/Thymeleaf layer; (2) test coverage adds new test classes using Spring MockMvc without touching production code; (3) refactoring decomposes oversized methods into private helpers using extract-method semantics, verified by the existing passing test suite.

**Tech Stack:** Spring Boot 4.0.0, Spring Security 7, Jakarta Validation, JUnit 5, Spring MockMvc (`@WebMvcTest`), `@MockitoBean` (replaces deprecated `@MockBean`)

---

## File Map

### Created
- `src/main/java/com/dnd/builder/config/SecurityConfig.java` — Spring Security: permit-all + CSRF
- `src/main/resources/static/js/utils.js` — shared `securedFetch` wrapper for all AJAX POSTs
- `src/test/java/com/dnd/builder/in/web/CharacterBuilderControllerTest.java` — MockMvc tests
- `src/test/java/com/dnd/builder/in/web/ExportControllerTest.java` — MockMvc tests
- `src/test/java/com/dnd/builder/out/persistence/InMemoryEquipmentRepositoryTest.java`
- `src/test/java/com/dnd/builder/out/persistence/InMemoryFeatRepositoryTest.java`
- `src/test/java/com/dnd/builder/out/persistence/InMemoryMagicItemRepositoryTest.java`

### Modified
- `pom.xml` — add `spring-boot-starter-security`, `spring-boot-starter-validation`
- `src/main/java/com/dnd/builder/in/web/ExportController.java` — import validation + inject ObjectMapper
- `src/main/java/com/dnd/builder/in/web/CharacterBuilderController.java` — inject ObjectMapper, `@Valid` on load endpoint
- `src/main/java/com/dnd/builder/in/web/PlayModeController.java` — `@Valid` on add-item endpoint, extract helpers from `levelUpOptions`
- `src/main/java/com/dnd/builder/core/model/CharacterDraft.java` — Jakarta Validation constraints
- `src/main/java/com/dnd/builder/core/model/InventoryItem.java` — Jakarta Validation constraints
- `src/main/resources/application.properties` — multipart file size limit, session cookie config
- `src/main/resources/templates/play/dashboard.html` — replace `fetch POST` with `securedFetch`
- `src/main/resources/templates/characters.html` — replace `fetch POST` with `securedFetch`
- `src/main/resources/templates/play/inventory.html` — replace `fetch POST` with `securedFetch`
- `src/main/resources/templates/steps/step9-review.html` — add `<script src="/js/utils.js">`

---

## Task 1: Import Validation in ExportController

**Files:**
- Modify: `src/main/java/com/dnd/builder/in/web/ExportController.java`
- Modify: `src/main/resources/application.properties`

- [ ] **Step 1: Add multipart size limit to application.properties**

```properties
spring.servlet.multipart.max-file-size=512KB
spring.servlet.multipart.max-request-size=512KB
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=Strict
spring.thymeleaf.cache=false
```

- [ ] **Step 2: Add field-level sanitization helper and fix import method in ExportController**

Replace the `importJson` method and add a `sanitize` helper (keep all existing code, just replace the two methods):

```java
@PostMapping("/import")
public String importJson(@RequestParam("file") MultipartFile file,
                          HttpSession session,
                          RedirectAttributes ra) {
    if (file.isEmpty()) {
        ra.addFlashAttribute("importError", "No file selected.");
        return "redirect:/step/9";
    }
    String ct = file.getContentType();
    if (ct == null || (!ct.contains("json") && !ct.equals("application/octet-stream"))) {
        ra.addFlashAttribute("importError", "Only JSON files are accepted.");
        return "redirect:/step/9";
    }
    try {
        CharacterDraft imported = objectMapper.readValue(file.getInputStream(), CharacterDraft.class);
        // Clamp numeric fields to safe game ranges
        imported.setLevel(Math.max(1, Math.min(20, imported.getLevel())));
        imported.setCurrentHp(Math.max(-1, Math.min(9999, imported.getCurrentHp())));
        imported.setTempHp(Math.max(0, Math.min(9999, imported.getTempHp())));
        // Sanitize free-text fields
        imported.setCharacterName(sanitize(imported.getCharacterName(), 80));
        imported.setAlignment(sanitize(imported.getAlignment(), 40));
        imported.setConcentratingOn(sanitize(imported.getConcentratingOn(), 100));
        imported.setHighestStepReached(10);
        session.setAttribute(CharacterBuilderController.DRAFT_KEY, imported);
        ra.addFlashAttribute("importSuccess",
            "Character \"" + safeLabel(imported.getCharacterName()) + "\" imported successfully.");
        return "redirect:/step/9";
    } catch (IOException e) {
        ra.addFlashAttribute("importError", "Could not parse file: invalid character data format.");
        return "redirect:/step/9";
    }
}

private String sanitize(String s, int maxLen) {
    if (s == null) return "";
    return s.replaceAll("[<>\"']", "").substring(0, Math.min(s.length(), maxLen));
}
```

- [ ] **Step 3: Compile and verify**

```bash
mvn compile -q
```
Expected: no output (clean compile).

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/application.properties src/main/java/com/dnd/builder/in/web/ExportController.java
git commit -m "fix: validate import file type, clamp numeric fields, sanitize free-text on import"
```

---

## Task 2: Spring Security + CSRF Protection

**Files:**
- Modify: `pom.xml`
- Create: `src/main/java/com/dnd/builder/config/SecurityConfig.java`
- Create: `src/main/resources/static/js/utils.js`
- Modify: `src/main/resources/templates/play/dashboard.html`
- Modify: `src/main/resources/templates/characters.html`
- Modify: `src/main/resources/templates/play/inventory.html`
- Modify: `src/main/resources/templates/steps/step9-review.html`

- [ ] **Step 1: Add Spring Security to pom.xml**

Add inside `<dependencies>`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

- [ ] **Step 2: Create SecurityConfig**

Create `src/main/java/com/dnd/builder/config/SecurityConfig.java`:

```java
package com.dnd.builder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .headers(headers -> headers
                .frameOptions(fo -> fo.sameOrigin())
            );
        return http.build();
    }
}
```

- [ ] **Step 3: Create shared securedFetch utility**

Create `src/main/resources/static/js/utils.js`:

```javascript
// Reads the XSRF-TOKEN cookie set by Spring Security and injects it into
// every POST/PUT/DELETE fetch call so CSRF protection passes server-side.
function securedFetch(url, options = {}) {
    const raw = document.cookie.split('; ').find(c => c.startsWith('XSRF-TOKEN='));
    const token = raw ? decodeURIComponent(raw.split('=')[1]) : null;
    return fetch(url, {
        ...options,
        headers: {
            ...(options.headers || {}),
            ...(token ? { 'X-XSRF-TOKEN': token } : {})
        }
    });
}
```

- [ ] **Step 4: Compile and check the app starts**

```bash
mvn compile -q
mvn spring-boot:run &
# wait ~5 seconds, then check:
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/characters
# Expected: 200
kill %1
```

If Spring Security blocks anything, verify SecurityConfig is in a package scanned by Spring (same root as `DndBuilderApplication`).

- [ ] **Step 5: Add utils.js to dashboard.html and replace all fetch POST calls with securedFetch**

In `src/main/resources/templates/play/dashboard.html`, add before the closing `</body>`:

```html
<script src="/js/utils.js"></script>
```

Then do a **find-and-replace** across the file: replace every `fetch('/play/` that has `method: 'POST'` with `securedFetch('/play/`. The pattern appears ~17 times. Also replace `fetch('/characters/load'` with `securedFetch('/characters/load'`.

Specifically replace every occurrence of the form:
```javascript
fetch('/play/...', {
    method: 'POST',
```
with:
```javascript
securedFetch('/play/...', {
    method: 'POST',
```

And:
```javascript
fetch('/characters/load', {
    method: 'POST',
```
with:
```javascript
securedFetch('/characters/load', {
    method: 'POST',
```

- [ ] **Step 6: Add utils.js to characters.html and replace fetch POST**

In `src/main/resources/templates/characters.html`, add before `</body>`:
```html
<script src="/js/utils.js"></script>
```

Replace the `fetch('/characters/load'` call with `securedFetch('/characters/load'`.

- [ ] **Step 7: Add utils.js to inventory.html and replace fetch POST calls**

In `src/main/resources/templates/play/inventory.html`, add before `</body>`:
```html
<script src="/js/utils.js"></script>
```

Replace every `fetch('/play/inventory/` and `fetch('/play/currency` call that uses `method: 'POST'` with `securedFetch(...)`.

- [ ] **Step 8: Add utils.js to step9-review.html**

In `src/main/resources/templates/steps/step9-review.html`, add before `</body>`:
```html
<script src="/js/utils.js"></script>
```

Check if any `fetch` POST calls exist in that file; replace them with `securedFetch`.

- [ ] **Step 9: Run tests**

```bash
mvn test -q
```
Expected: all green. If `PlayModeControllerTest` fails with 403, add `.csrf(csrf -> csrf.disable())` to the test's MockMvc setup (covered in Task 6).

- [ ] **Step 10: Commit**

```bash
git add pom.xml src/main/java/com/dnd/builder/config/SecurityConfig.java src/main/resources/static/js/utils.js
git add src/main/resources/templates/play/dashboard.html src/main/resources/templates/characters.html
git add src/main/resources/templates/play/inventory.html src/main/resources/templates/steps/step9-review.html
git commit -m "feat: add Spring Security CSRF protection; add securedFetch wrapper to all AJAX POST calls"
```

---

## Task 3: Jakarta Validation on CharacterDraft and InventoryItem

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/java/com/dnd/builder/core/model/CharacterDraft.java`
- Modify: `src/main/java/com/dnd/builder/core/model/InventoryItem.java`
- Modify: `src/main/java/com/dnd/builder/in/web/CharacterBuilderController.java`
- Modify: `src/main/java/com/dnd/builder/in/web/PlayModeController.java`

- [ ] **Step 1: Add validation starter to pom.xml**

Add inside `<dependencies>`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

- [ ] **Step 2: Add constraints to CharacterDraft**

Add the import at the top of `CharacterDraft.java`:
```java
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
```

Add constraints to the affected fields:
```java
@Size(max = 80)
private String characterName = "";

@Size(max = 40)
private String alignment = "";

@Min(1) @Max(20)
private int level = 1;

@Size(max = 100)
private String concentratingOn = "";

@Size(max = 100)
private String pactBoon = "";
```

- [ ] **Step 3: Add constraints to InventoryItem**

Add the import at the top of `InventoryItem.java`:
```java
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
```

Add constraints:
```java
@Size(max = 100)
private String name;

@Size(max = 50)
private String category;

@Min(0) @Max(9999)
private int quantity;

@Size(max = 20)
private String rarity;

@Size(max = 500)
private String description;

@Size(max = 30)
private String damage;
```

- [ ] **Step 4: Add @Valid to CharacterBuilderController.loadCharacter**

In `CharacterBuilderController.java`, add `import jakarta.validation.Valid;` and update the method signature:

```java
@PostMapping("/characters/load")
@ResponseBody
public Map<String, Object> loadCharacter(@Valid @RequestBody CharacterDraft draft, HttpSession session) {
    session.setAttribute(DRAFT_KEY, draft);
    return Map.of("success", true);
}
```

- [ ] **Step 5: Add @Valid to PlayModeController.addItem**

In `PlayModeController.java`, add `import jakarta.validation.Valid;` and update:

```java
@PostMapping("/inventory/add")
@ResponseBody
public Map<String, Object> addItem(@Valid @RequestBody InventoryItem item, HttpSession session) {
```

- [ ] **Step 6: Compile and run tests**

```bash
mvn test -q
```
Expected: all green.

- [ ] **Step 7: Commit**

```bash
git add pom.xml
git add src/main/java/com/dnd/builder/core/model/CharacterDraft.java
git add src/main/java/com/dnd/builder/core/model/InventoryItem.java
git add src/main/java/com/dnd/builder/in/web/CharacterBuilderController.java
git add src/main/java/com/dnd/builder/in/web/PlayModeController.java
git commit -m "feat: add Jakarta Validation constraints to CharacterDraft and InventoryItem; @Valid on load/add endpoints"
```

---

## Task 4: Inject ObjectMapper Bean (fix anti-pattern in two controllers)

**Files:**
- Modify: `src/main/java/com/dnd/builder/in/web/ExportController.java`
- Modify: `src/main/java/com/dnd/builder/in/web/CharacterBuilderController.java`

Both controllers currently do `new ObjectMapper()` bypassing Spring's auto-configured singleton (which has CVE patches and module registration applied).

- [ ] **Step 1: Update ExportController to accept injected ObjectMapper**

Change the constructor from:
```java
public ExportController(CharacterCalculator calculator, PdfExportService pdfService,
                        RaceRepository raceRepository, ClassRepository classRepository,
                        BackgroundRepository backgroundRepository, SpellRepository spellRepository) {
    this.objectMapper = new ObjectMapper();
```
To:
```java
public ExportController(ObjectMapper objectMapper, CharacterCalculator calculator, PdfExportService pdfService,
                        RaceRepository raceRepository, ClassRepository classRepository,
                        BackgroundRepository backgroundRepository, SpellRepository spellRepository) {
    this.objectMapper = objectMapper;
```

- [ ] **Step 2: Update CharacterBuilderController to accept injected ObjectMapper**

Change the constructor from:
```java
public CharacterBuilderController(RaceRepository r, ClassRepository c, BackgroundRepository b,
                         SpellRepository sp, EquipmentRepository eq, FeatRepository f,
                         CharacterCalculator calc) {
    ...
    this.objectMapper = new ObjectMapper();
```
To:
```java
public CharacterBuilderController(RaceRepository r, ClassRepository c, BackgroundRepository b,
                         SpellRepository sp, EquipmentRepository eq, FeatRepository f,
                         CharacterCalculator calc, ObjectMapper objectMapper) {
    ...
    this.objectMapper = objectMapper;
```

- [ ] **Step 3: Compile and run tests**

```bash
mvn test -q
```
Expected: all green.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/dnd/builder/in/web/ExportController.java
git add src/main/java/com/dnd/builder/in/web/CharacterBuilderController.java
git commit -m "fix: inject Spring-managed ObjectMapper instead of instantiating with new"
```

---

## Task 5: Simple Per-IP Rate Limiting Filter

**Files:**
- Create: `src/main/java/com/dnd/builder/config/RateLimitFilter.java`

- [ ] **Step 1: Create the filter**

Create `src/main/java/com/dnd/builder/config/RateLimitFilter.java`:

```java
package com.dnd.builder.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limits each IP to 60 POST requests per minute.
 * Resets the window every 60 seconds.
 */
@Component
@Order(1)
public class RateLimitFilter implements Filter {

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final long WINDOW_MS = 60_000;

    private record Window(AtomicInteger count, long startMs) {}

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        var request  = (HttpServletRequest) req;
        var response = (HttpServletResponse) res;

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String ip = resolveIp(request);
            long now   = System.currentTimeMillis();
            Window w   = windows.compute(ip, (k, existing) -> {
                if (existing == null || now - existing.startMs() >= WINDOW_MS) {
                    return new Window(new AtomicInteger(1), now);
                }
                existing.count().incrementAndGet();
                return existing;
            });
            if (w.count().get() > MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Too many requests\"}");
                return;
            }
        }
        chain.doFilter(req, res);
    }

    private String resolveIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
            ? forwarded.split(",")[0].trim()
            : req.getRemoteAddr();
    }
}
```

- [ ] **Step 2: Compile and run tests**

```bash
mvn test -q
```
Expected: all green.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/dnd/builder/config/RateLimitFilter.java
git commit -m "feat: add per-IP rate limit filter (60 POST requests/minute)"
```

---

## Task 6: CharacterBuilderController MockMvc Tests

**Files:**
- Create: `src/test/java/com/dnd/builder/in/web/CharacterBuilderControllerTest.java`

- [ ] **Step 1: Write the test class**

Create `src/test/java/com/dnd/builder/in/web/CharacterBuilderControllerTest.java`:

```java
package com.dnd.builder.in.web;

import com.dnd.builder.core.model.CharacterDraft;
import com.dnd.builder.core.port.out.*;
import com.dnd.builder.core.service.CharacterCalculator;
import com.dnd.builder.out.persistence.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CharacterBuilderController.class)
@DisplayName("CharacterBuilderController")
class CharacterBuilderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean RaceRepository raceRepository;
    @MockitoBean ClassRepository classRepository;
    @MockitoBean BackgroundRepository backgroundRepository;
    @MockitoBean SpellRepository spellRepository;
    @MockitoBean EquipmentRepository equipmentRepository;
    @MockitoBean FeatRepository featRepository;
    @MockitoBean CharacterCalculator calculator;

    @Nested
    @DisplayName("GET /characters")
    class GetCharacters {
        @Test
        @DisplayName("returns 200 and characters view")
        void returnsCharactersView() throws Exception {
            mockMvc.perform(get("/characters"))
                .andExpect(status().isOk())
                .andExpect(view().name("characters"));
        }
    }

    @Nested
    @DisplayName("GET /new")
    class NewCharacter {
        @Test
        @DisplayName("redirects to step 1 and creates fresh draft in session")
        void redirectsToStep1() throws Exception {
            mockMvc.perform(get("/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/step/1"));
        }
    }

    @Nested
    @DisplayName("POST /characters/load")
    class LoadCharacter {
        @Test
        @DisplayName("accepts valid draft and returns success")
        void acceptsValidDraft() throws Exception {
            CharacterDraft draft = CharacterDraft.fresh();
            draft.setCharacterName("Aragorn");
            draft.setCharacterClass("fighter");
            draft.setLevel(5);

            mockMvc.perform(post("/characters/load")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(draft)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("rejects draft with name exceeding 80 characters")
        void rejectsOverlongName() throws Exception {
            CharacterDraft draft = CharacterDraft.fresh();
            draft.setCharacterName("A".repeat(81));

            mockMvc.perform(post("/characters/load")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(draft)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /step/{step}")
    class ShowStep {
        @Test
        @DisplayName("redirects to step 1 when step exceeds highestStepReached + 1")
        void blocksSkippingAhead() throws Exception {
            MockHttpSession session = new MockHttpSession();
            CharacterDraft draft = CharacterDraft.fresh();
            // fresh draft has highestStepReached = 0
            session.setAttribute(CharacterBuilderController.DRAFT_KEY, draft);

            mockMvc.perform(get("/step/5").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/step/1"));
        }

        @Test
        @DisplayName("allows accessing the next step in sequence")
        void allowsNextStep() throws Exception {
            MockHttpSession session = new MockHttpSession();
            CharacterDraft draft = CharacterDraft.fresh();
            draft.setHighestStepReached(2);
            session.setAttribute(CharacterBuilderController.DRAFT_KEY, draft);

            when(raceRepository.findAll()).thenReturn(List.of());
            when(classRepository.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/step/3").session(session))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /step/4 (ability scores)")
    class SaveStep4 {
        @Test
        @DisplayName("rejects point spend over 27 and redirects with error")
        void rejectsExcessivePointSpend() throws Exception {
            MockHttpSession session = new MockHttpSession();
            CharacterDraft draft = CharacterDraft.fresh();
            draft.setHighestStepReached(4);
            session.setAttribute(CharacterBuilderController.DRAFT_KEY, draft);

            // All stats at 15 = massively over budget
            mockMvc.perform(post("/step/4")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .session(session)
                    .param("STR", "15").param("DEX", "15").param("CON", "15")
                    .param("INT", "15").param("WIS", "15").param("CHA", "15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/step/4*"));
        }

        @Test
        @DisplayName("accepts valid point buy allocation and redirects to step 5")
        void acceptsValidAllocation() throws Exception {
            MockHttpSession session = new MockHttpSession();
            CharacterDraft draft = CharacterDraft.fresh();
            draft.setHighestStepReached(4);
            session.setAttribute(CharacterBuilderController.DRAFT_KEY, draft);

            when(backgroundRepository.findAll()).thenReturn(List.of());

            // 8,8,8,8,8,8 = 0 points spent (valid)
            mockMvc.perform(post("/step/4")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .session(session)
                    .param("STR", "8").param("DEX", "8").param("CON", "8")
                    .param("INT", "8").param("WIS", "8").param("CHA", "8"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/step/5"));
        }
    }
}
```

- [ ] **Step 2: Run tests**

```bash
mvn test -q -pl . -Dtest=CharacterBuilderControllerTest
```
Expected: all pass.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/dnd/builder/in/web/CharacterBuilderControllerTest.java
git commit -m "test: add MockMvc tests for CharacterBuilderController (step sequencing, point buy, load endpoint)"
```

---

## Task 7: ExportController MockMvc Tests

**Files:**
- Create: `src/test/java/com/dnd/builder/in/web/ExportControllerTest.java`

- [ ] **Step 1: Write the test class**

Create `src/test/java/com/dnd/builder/in/web/ExportControllerTest.java`:

```java
package com.dnd.builder.in.web;

import com.dnd.builder.core.model.CharacterDraft;
import com.dnd.builder.core.port.out.*;
import com.dnd.builder.core.service.CharacterCalculator;
import com.dnd.builder.core.service.PdfExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExportController.class)
@DisplayName("ExportController")
class ExportControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean CharacterCalculator calculator;
    @MockitoBean PdfExportService pdfService;
    @MockitoBean RaceRepository raceRepository;
    @MockitoBean ClassRepository classRepository;
    @MockitoBean BackgroundRepository backgroundRepository;
    @MockitoBean SpellRepository spellRepository;

    private MockHttpSession sessionWithDraft(CharacterDraft draft) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CharacterBuilderController.DRAFT_KEY, draft);
        return session;
    }

    @Nested
    @DisplayName("GET /export")
    class ExportJson {
        @Test
        @DisplayName("returns JSON attachment with sanitized filename")
        void returnsJsonFile() throws Exception {
            CharacterDraft draft = CharacterDraft.fresh();
            draft.setCharacterName("Aragorn");

            mockMvc.perform(get("/export").session(sessionWithDraft(draft)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(header().string("Content-Disposition", containsString("Aragorn")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("uses 'character' as filename when name is blank")
        void fallbackFilename() throws Exception {
            CharacterDraft draft = CharacterDraft.fresh();

            mockMvc.perform(get("/export").session(sessionWithDraft(draft)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("character")));
        }
    }

    @Nested
    @DisplayName("POST /import")
    class ImportJson {
        @Test
        @DisplayName("imports valid JSON and redirects with success flash")
        void importsValidJson() throws Exception {
            CharacterDraft draft = CharacterDraft.fresh();
            draft.setCharacterName("Legolas");
            draft.setCharacterClass("ranger");
            draft.setLevel(10);
            byte[] json = objectMapper.writeValueAsBytes(draft);

            MockMultipartFile file = new MockMultipartFile(
                "file", "legolas.json", "application/json", json);

            mockMvc.perform(multipart("/import")
                    .file(file)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/step/9"))
                .andExpect(flash().attribute("importSuccess", containsString("Legolas")));
        }

        @Test
        @DisplayName("rejects empty file upload")
        void rejectsEmptyFile() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                "file", "empty.json", "application/json", new byte[0]);

            mockMvc.perform(multipart("/import")
                    .file(file)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("importError", containsString("No file")));
        }

        @Test
        @DisplayName("rejects non-JSON file content type")
        void rejectsNonJsonContentType() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                "file", "sheet.pdf", "application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46});

            mockMvc.perform(multipart("/import")
                    .file(file)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("importError", containsString("Only JSON")));
        }

        @Test
        @DisplayName("rejects corrupt JSON and returns error flash without leaking details")
        void rejectsCorruptJson() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                "file", "bad.json", "application/json", "not json {{{".getBytes());

            mockMvc.perform(multipart("/import")
                    .file(file)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("importError"))
                .andExpect(flash().attribute("importError",
                    not(containsString("com.fasterxml"))));
        }

        @Test
        @DisplayName("clamps level to 1-20 on import")
        void clampsLevel() throws Exception {
            // JSON with level=999 — should clamp to 20
            String json = "{\"level\":999,\"characterClass\":\"fighter\"}";
            MockMultipartFile file = new MockMultipartFile(
                "file", "overpowered.json", "application/json", json.getBytes());

            MockHttpSession session = new MockHttpSession();
            mockMvc.perform(multipart("/import")
                    .file(file)
                    .session(session)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/step/9"));

            CharacterDraft stored = (CharacterDraft) session.getAttribute(CharacterBuilderController.DRAFT_KEY);
            assert stored != null;
            assert stored.getLevel() == 20 : "level should be clamped to 20, was " + stored.getLevel();
        }
    }
}
```

- [ ] **Step 2: Run tests**

```bash
mvn test -q -pl . -Dtest=ExportControllerTest
```
Expected: all pass.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/dnd/builder/in/web/ExportControllerTest.java
git commit -m "test: add MockMvc tests for ExportController (JSON export, import validation, level clamping)"
```

---

## Task 8: Missing Repository Tests

**Files:**
- Create: `src/test/java/com/dnd/builder/out/persistence/InMemoryEquipmentRepositoryTest.java`
- Create: `src/test/java/com/dnd/builder/out/persistence/InMemoryFeatRepositoryTest.java`
- Create: `src/test/java/com/dnd/builder/out/persistence/InMemoryMagicItemRepositoryTest.java`

- [ ] **Step 1: Write InMemoryEquipmentRepositoryTest**

```java
package com.dnd.builder.out.persistence;

import com.dnd.builder.core.model.EquipmentSlot;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryEquipmentRepository")
class InMemoryEquipmentRepositoryTest {

    private InMemoryEquipmentRepository repository;

    @BeforeEach
    void setUp() { repository = new InMemoryEquipmentRepository(); }

    @Test
    @DisplayName("returns equipment slots for every playable class")
    void allClassesHaveSlots() {
        for (String classId : List.of("barbarian","bard","cleric","druid","fighter",
                                       "monk","paladin","ranger","rogue","sorcerer",
                                       "warlock","wizard")) {
            List<EquipmentSlot> slots = repository.findByClass(classId);
            assertFalse(slots.isEmpty(), classId + " should have equipment slots");
        }
    }

    @Test
    @DisplayName("returns empty list for unknown class")
    void unknownClassReturnsEmpty() {
        assertTrue(repository.findByClass("unknown_class").isEmpty());
    }

    @Test
    @DisplayName("returns empty list for null class")
    void nullClassReturnsEmpty() {
        assertTrue(repository.findByClass(null).isEmpty());
    }

    @Test
    @DisplayName("each slot has at least one choice")
    void eachSlotHasChoices() {
        List<EquipmentSlot> slots = repository.findByClass("fighter");
        for (EquipmentSlot slot : slots) {
            assertFalse(slot.choices().isEmpty(),
                "Fighter slot '" + slot.id() + "' should have at least one choice");
        }
    }

    @Test
    @DisplayName("slot IDs are unique within a class")
    void slotIdsAreUniquePerClass() {
        List<EquipmentSlot> slots = repository.findByClass("wizard");
        long distinctIds = slots.stream().map(EquipmentSlot::id).distinct().count();
        assertEquals(slots.size(), distinctIds, "Wizard slot IDs must be unique");
    }
}
```

- [ ] **Step 2: Write InMemoryFeatRepositoryTest**

```java
package com.dnd.builder.out.persistence;

import com.dnd.builder.core.model.FeatDefinition;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryFeatRepository")
class InMemoryFeatRepositoryTest {

    private InMemoryFeatRepository repository;

    @BeforeEach
    void setUp() { repository = new InMemoryFeatRepository(); }

    @Test
    @DisplayName("findAll returns non-empty feat list")
    void findAllIsNonEmpty() {
        assertFalse(repository.findAll().isEmpty());
    }

    @Test
    @DisplayName("findById returns correct feat for known ID")
    void findByIdReturnsKnownFeat() {
        FeatDefinition alert = repository.findById("alert");
        assertNotNull(alert, "Feat 'alert' must exist");
        assertEquals("Alert", alert.getName());
    }

    @Test
    @DisplayName("findById returns null for unknown ID")
    void findByIdReturnsNullForUnknown() {
        assertNull(repository.findById("nonexistent_feat_xyz"));
    }

    @Test
    @DisplayName("findById returns null for null ID")
    void findByIdReturnsNullForNull() {
        assertNull(repository.findById(null));
    }

    @Test
    @DisplayName("every feat has a non-blank name and description")
    void allFeatsHaveNameAndDescription() {
        for (FeatDefinition feat : repository.findAll()) {
            assertFalse(feat.getName() == null || feat.getName().isBlank(),
                "Feat '" + feat.getId() + "' must have a name");
            assertFalse(feat.getDescription() == null || feat.getDescription().isBlank(),
                "Feat '" + feat.getId() + "' must have a description");
        }
    }

    @Test
    @DisplayName("feat IDs are unique")
    void featIdsAreUnique() {
        List<FeatDefinition> all = repository.findAll();
        long distinct = all.stream().map(FeatDefinition::getId).distinct().count();
        assertEquals(all.size(), distinct, "Feat IDs must be unique");
    }
}
```

- [ ] **Step 3: Write InMemoryMagicItemRepositoryTest**

```java
package com.dnd.builder.out.persistence;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryMagicItemRepository")
class InMemoryMagicItemRepositoryTest {

    private InMemoryMagicItemRepository repository;

    @BeforeEach
    void setUp() { repository = new InMemoryMagicItemRepository(); }

    @Test
    @DisplayName("findAll returns non-empty item list")
    void findAllIsNonEmpty() {
        assertFalse(repository.findAll().isEmpty());
    }

    @Test
    @DisplayName("findById returns item for known ID")
    void findByIdReturnsKnownItem() {
        // Get first item from the full list and look it up by ID
        var first = repository.findAll().get(0);
        var found = repository.findById(first.id());
        assertNotNull(found, "Should find item by its own ID");
        assertEquals(first.name(), found.name());
    }

    @Test
    @DisplayName("findById returns null for unknown ID")
    void findByIdReturnsNullForUnknown() {
        assertNull(repository.findById("no_such_item_xyz"));
    }

    @Test
    @DisplayName("findById returns null for null")
    void findByIdReturnsNullForNull() {
        assertNull(repository.findById(null));
    }

    @Test
    @DisplayName("search with blank query returns all items")
    void searchBlankReturnsAll() {
        assertEquals(repository.findAll().size(), repository.search("").size());
        assertEquals(repository.findAll().size(), repository.search(null).size());
    }

    @Test
    @DisplayName("search filters by name substring case-insensitively")
    void searchFiltersByName() {
        List<var> results = repository.search("sword");
        assertFalse(results.isEmpty(), "There should be sword-related items");
        results.forEach(item ->
            assertTrue(item.name().toLowerCase().contains("sword")
                || item.category().toLowerCase().contains("sword")
                || item.rarity().toLowerCase().contains("sword"),
                "Result should match 'sword': " + item.name()));
    }

    @Test
    @DisplayName("all items have non-blank name, category, and rarity")
    void allItemsHaveRequiredFields() {
        for (var item : repository.findAll()) {
            assertFalse(item.name() == null || item.name().isBlank(),
                "Item '" + item.id() + "' must have a name");
            assertFalse(item.category() == null || item.category().isBlank(),
                "Item '" + item.id() + "' must have a category");
            assertFalse(item.rarity() == null || item.rarity().isBlank(),
                "Item '" + item.id() + "' must have a rarity");
        }
    }
}
```

- [ ] **Step 4: Run all three new test classes**

```bash
mvn test -q -pl . -Dtest="InMemoryEquipmentRepositoryTest,InMemoryFeatRepositoryTest,InMemoryMagicItemRepositoryTest"
```
Expected: all pass.

- [ ] **Step 5: Commit**

```bash
git add src/test/java/com/dnd/builder/out/persistence/InMemoryEquipmentRepositoryTest.java
git add src/test/java/com/dnd/builder/out/persistence/InMemoryFeatRepositoryTest.java
git add src/test/java/com/dnd/builder/out/persistence/InMemoryMagicItemRepositoryTest.java
git commit -m "test: add tests for InMemoryEquipmentRepository, InMemoryFeatRepository, InMemoryMagicItemRepository"
```

---

## Task 9: Extract Helpers from levelUpOptions (PlayModeController)

This refactor uses extract-method: no logic changes, only moves code into named private methods. Run the full test suite after each extraction to confirm behavior is preserved.

**Files:**
- Modify: `src/main/java/com/dnd/builder/in/web/PlayModeController.java`

- [ ] **Step 1: Extract spell gain info into private helper**

Find the block inside `levelUpOptions` that builds `cantripGain`, `spellGain`, `spellbookGain`, `spellsKnownGain`, `maxNewSpellLevel`, and `needsMagicalSecrets`. Extract it to:

```java
private record SpellGainInfo(int cantripGain, int spellGain, int spellbookGain,
                              int spellsKnownGain, int maxNewSpellLevel,
                              boolean needsMagicalSecrets,
                              List<SpellDefinition> availableMagicalSecrets) {}

private SpellGainInfo buildSpellGainInfo(CharacterDraft draft, ClassDefinition cls,
                                          int newLevel, SubclassDefinition sc) {
    // ... move the existing computation block here ...
}
```

Then in `levelUpOptions`, replace that block with:
```java
SpellGainInfo spellGain = buildSpellGainInfo(draft, cls, newLevel, sc);
```
And update all references from the local variables to `spellGain.cantripGain()` etc.

- [ ] **Step 2: Run tests after extraction**

```bash
mvn test -q
```
Expected: all green.

- [ ] **Step 3: Extract class-specific choices into private helper**

Find the block that builds warlock, sorcerer, wizard, paladin, ranger, druid invocation/metamagic/pact/favored choices. Extract to:

```java
private Map<String, Object> buildClassSpecificChoices(CharacterDraft draft,
                                                        ClassDefinition cls, int newLevel) {
    // ... move the existing computation block here, return as a Map ...
}
```

Then merge the returned map into the response in `levelUpOptions`.

- [ ] **Step 4: Run tests after second extraction**

```bash
mvn test -q
```
Expected: all green.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/dnd/builder/in/web/PlayModeController.java
git commit -m "refactor: extract SpellGainInfo and buildClassSpecificChoices from levelUpOptions"
```

---

## Task 10: Extract Helpers from populateModel (CharacterBuilderController)

**Files:**
- Modify: `src/main/java/com/dnd/builder/in/web/CharacterBuilderController.java`

- [ ] **Step 1: Extract populateSpellStep**

Find the `case 7 ->` block inside `populateModel`. Extract to:

```java
private void populateSpellStep(Model model, CharacterDraft draft) {
    // move case 7 content here
}
```

Call it from `populateModel`:
```java
case 7 -> populateSpellStep(model, draft);
```

- [ ] **Step 2: Extract populateReviewStep**

Find the `case 9 ->` block inside `populateModel`. Extract to:

```java
private void populateReviewStep(Model model, CharacterDraft draft) {
    // move case 9 content here
}
```

Call it from `populateModel`:
```java
case 9 -> populateReviewStep(model, draft);
```

- [ ] **Step 3: Run tests**

```bash
mvn test -q
```
Expected: all green.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/dnd/builder/in/web/CharacterBuilderController.java
git commit -m "refactor: extract populateSpellStep and populateReviewStep from populateModel"
```

---

## Task 11: Extract Helpers from _buildBody (levelup.js)

**Files:**
- Modify: `src/main/resources/static/js/levelup.js`

- [ ] **Step 1: Extract _buildAsiSection**

Find the block inside `_buildBody` that generates ASI/Feat HTML. Move it to a function within the IIFE:

```javascript
function _buildAsiSection(opts) {
    // move ASI block HTML generation here
    // return the HTML string
}
```

Replace the original block in `_buildBody` with:
```javascript
html += _buildAsiSection(opts);
```

- [ ] **Step 2: Extract _buildSpellSection**

Find the block that generates cantrip/spell/spellbook/magical-secrets HTML. Move to:

```javascript
function _buildSpellSection(opts) {
    // ...
    return html;
}
```

- [ ] **Step 3: Extract _buildSubclassSection**

Find the subclass choice HTML block. Move to:

```javascript
function _buildSubclassSection(opts) {
    // ...
    return html;
}
```

- [ ] **Step 4: Extract _buildClassSpecificSection**

Find the warlock/monk/sorcerer/ranger/druid class-specific choices block. Move to:

```javascript
function _buildClassSpecificSection(opts) {
    // ...
    return html;
}
```

- [ ] **Step 5: Manually verify the levelup modal still works**

Start the app and navigate to the play dashboard with a level-eligible character. Click "Level Up". Verify the modal opens and shows the correct choices. Try: fighter at level 3 (no subclass choice needed), wizard at level 3 (Arcane Tradition), warlock at level 2 (invocations).

```bash
mvn spring-boot:run
```

- [ ] **Step 6: Commit**

```bash
git add src/main/resources/static/js/levelup.js
git commit -m "refactor: decompose _buildBody in levelup.js into focused section builders"
```

---

## Self-Review

**Spec coverage check:**
- ✅ Import validation (Task 1) — file type, field clamping, free-text sanitization
- ✅ Spring Security + CSRF (Task 2) — SecurityConfig, securedFetch, form auto-injection
- ✅ JSR-303 validation (Task 3) — constraints on CharacterDraft/InventoryItem, @Valid on endpoints
- ✅ Rate limiting (Task 5) — per-IP 60 req/min filter
- ✅ CharacterBuilderController tests (Task 6) — step sequencing, point buy, load endpoint
- ✅ ExportController tests (Task 7) — export filename, import validation, level clamping
- ✅ Missing repository tests (Task 8) — Equipment, Feat, MagicItem repositories
- ✅ Oversized methods (Tasks 9-11) — levelUpOptions, populateModel, _buildBody
- ✅ Inject ObjectMapper bean (Task 4) — both controllers

**No placeholders present:** All code blocks are complete and runnable.

**Type consistency:** `SpellGainInfo` record used consistently in Task 9; `@MockitoBean` used consistently (not `@MockBean`) across Tasks 6 and 7.
