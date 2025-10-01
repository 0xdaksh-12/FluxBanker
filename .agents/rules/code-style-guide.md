---
trigger: always_on
---

Here’s a cleaner, more structured version of your guidelines with better clarity and consistency:

---

## Project Guidelines

### 1. Framework Awareness

This project uses a version of the framework that may include **breaking changes**. APIs, conventions, and file structures might differ from prior knowledge.

- Always verify implementation details from:

  ```
  node_modules/next/dist/docs/
  ```

- Do not rely on assumptions or outdated patterns.
- Pay attention to deprecation warnings and migration notes before writing code.

---

### 2. Documentation & Context

- Always maintain context when making changes.
- Clearly document _why_ a change is made, not just _what_ was changed.
- Ensure updates are consistent with the latest framework conventions.

---

### 3. Code Commenting Style

Follow clean and minimal commenting practices:

```js
// Fetch user account details from API
const data = await fetchUser();
```

Avoid noisy or decorative comments:

```js
// ---- Fetch user account details ----  // Not recommended
```

- Use comments only where necessary.
- Focus on clarity and intent.

---

### 4. Code Documentation Standard

Use **Google-style documentation** for functions and modules:

```js
/**
 * Fetches user account details by ID.
 * @param {string} userId - Unique identifier of the user.
 * @returns {Promise<User>} Resolved user object.
 */
async function getUser(userId) {
  // Implementation
}
```

---

### 5. Commit Message Standard

Follow **Google-style commit messages**:

```
feat: add transaction history endpoint

fix: resolve login redirect issue

refactor: restructure API service layer

docs: update setup instructions for Next.js changes
```

- Use clear prefixes: `feat`, `fix`, `refactor`, `docs`, `chore`
- Keep messages concise but meaningful

---

### 6. General Principles

- Prefer readability over cleverness
- Follow consistent naming conventions
- Keep code modular and maintainable
- Avoid unnecessary abstractions unless justified
- use zsh -ilc
