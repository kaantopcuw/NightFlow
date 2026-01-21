---
trigger: manual
---

You are an expert in Svelte 5, SvelteKit, TypeScript, and modern web development.

## Key Principles
- Write concise, technical code with accurate Svelte 5 and SvelteKit examples.
- Leverage SvelteKit's server-side rendering (SSR) and static site generation (SSG) capabilities.
- Prioritize performance optimization and minimal JavaScript for optimal user experience.
- Use descriptive variable names and follow Svelte and SvelteKit conventions.
- Organize files using SvelteKit's file-based routing system.

## Code Style and Structure
- Write concise, technical TypeScript or JavaScript code with accurate examples.
- Use functional and declarative programming patterns; avoid unnecessary classes except for state machines.
- Prefer iteration and modularization over code duplication.
- Structure files: component logic, markup, styles, helpers, types.
- Follow Svelte's official documentation for setup and configuration: https://svelte.dev/docs

## Naming Conventions
- Use lowercase with hyphens for component files (e.g., `components/auth-form.svelte`).
- Use PascalCase for component names in imports and usage.
- Use camelCase for variables, functions, and props.

## TypeScript Usage
- Use TypeScript for all code; prefer interfaces over types.
- Avoid enums; use const objects instead.
- Use functional components with TypeScript interfaces for props.
- Enable strict mode in TypeScript for better type safety.

## Svelte 5 Runes

### `$state`: Declare reactive state
```typescript
let count = $state(0);
let user = $state({ name: 'John', age: 25 });
```

### `$derived`: Compute derived values
```typescript
let count = $state(0);
let doubled = $derived(count * 2);
let isEven = $derived(count % 2 === 0);
```

### `$effect`: Manage side effects and lifecycle
```typescript
$effect(() => {
  console.log(`Count is now ${count}`);
  
  // Cleanup function
  return () => {
    console.log('Cleaning up...');
  };
});
```

### `$effect.pre`: Runs before DOM update
```typescript
$effect.pre(() => {
  // Runs before DOM updates
});
```

### `$effect.tracking`: Optimize effect dependencies
```typescript
$effect(() => {
  if ($effect.tracking()) {
    // Only runs when tracking
  }
});
```

### `$props`: Declare component props
```typescript
interface Props {
  requiredProp: string;
  optionalProp?: number;
}

let { optionalProp = 42, requiredProp }: Props = $props();
```

### `$bindable`: Create two-way bindable props
```typescript
let { bindableProp = $bindable('default value') } = $props();
```

### `$inspect`: Debug reactive state (development only)
```typescript
$inspect(count); // Logs count when it changes
$inspect(count, user); // Multiple values
```

## Event Handling (IMPORTANT CHANGE!)

### Svelte 5 Event Syntax
```svelte
<!-- SVELTE 5 SYNTAX (onclick, onchange, etc.) -->
<button onclick={() => count++}>Increment</button>
<input oninput={(e) => name = e.currentTarget.value} />
<form onsubmit={handleSubmit}>

<!-- Modifiers -->
<button onclick|preventDefault={() => save()}>Save</button>
<div onclick|stopPropagation={() => handleClick()}>Click</div>
<input onkeydown|enter={() => submit()}>

<!-- OLD SYNTAX (Svelte 4) - DON'T USE -->
<button on:click={() => count++}>Increment</button>
```

### Common Events
```svelte
<script lang="ts">
let value = $state('');

function handleClick() {
  console.log('Clicked');
}

function handleInput(e: Event) {
  const target = e.currentTarget as HTMLInputElement;
  value = target.value;
}
</script>

<button onclick={handleClick}>Click</button>
<input oninput={handleInput} value={value} />
<form onsubmit|preventDefault={handleSubmit}>
  <button type="submit">Submit</button>
</form>
```

## UI and Styling
- Use Tailwind CSS for utility-first styling approach.
- Leverage Shadcn components for pre-built, customizable UI elements.
- Import Shadcn components from `$lib/components/ui`.
- Organize Tailwind classes using the `cn()` utility from `$lib/utils`.
- Use Svelte's built-in transition and animation features.

## Shadcn Color Conventions
- Use `background` and `foreground` convention for colors.
- Define CSS variables without color space function:
  ```css
  --primary: 222.2 47.4% 11.2%;
  --primary-foreground: 210 40% 98%;
  ```
- Usage example:
  ```svelte
  <div class="bg-primary text-primary-foreground">Hello</div>
  ```

### Key Color Variables
- `--background`, `--foreground`: Default body colors
- `--muted`, `--muted-foreground`: Muted backgrounds
- `--card`, `--card-foreground`: Card backgrounds
- `--popover`, `--popover-foreground`: Popover backgrounds
- `--border`: Default border color
- `--input`: Input border color
- `--primary`, `--primary-foreground`: Primary button colors
- `--secondary`, `--secondary-foreground`: Secondary button colors
- `--accent`, `--accent-foreground`: Accent colors
- `--destructive`, `--destructive-foreground`: Destructive action colors
- `--ring`: Focus ring color
- `--radius`: Border radius for components

## SvelteKit Project Structure
```
- src/
  - lib/
    - components/
    - utils/
  - routes/
    - api/
    - +page.svelte
    - +layout.svelte
  - app.html
- static/
- svelte.config.js
- vite.config.js
```

## Component Development
- Create `.svelte` files for Svelte components.
- Use `.svelte.ts` files for component logic and state machines.
- Implement proper component composition and reusability.
- Use Svelte's props for data passing.
- Leverage Svelte's reactive declarations for local state management.

## State Management

### Simple State (Inside Component)
```typescript
<script lang="ts">
let count = $state(0);
let doubled = $derived(count * 2);
</script>

<button onclick={() => count++}>
  Count: {count}, Doubled: {doubled}
</button>
```

### Complex State (State Machines)
```typescript
// counter.svelte.ts
class Counter {
  count = $state(0);
  incrementor = $state(1);
  
  increment() {
    this.count += this.incrementor;
  }
  
  resetCount() {
    this.count = 0;
  }
  
  resetIncrementor() {
    this.incrementor = 1;
  }
}

export const counter = new Counter();
```

### Use in Components
```svelte
<script lang="ts">
import { counter } from './counter.svelte.ts';
</script>

<button onclick={() => counter.increment()}>
  Count: {counter.count}
</button>
<button onclick={() => counter.resetCount()}>Reset</button>
```

## Routing and Pages
- Utilize SvelteKit's file-based routing system in the `src/routes/` directory.
- Implement dynamic routes using `[slug]` syntax.
- Use load functions for server-side data fetching and pre-rendering.
- Implement proper error handling with `+error.svelte` pages.

### Example Route Structure
```
src/routes/
├── +page.svelte              # Home page (/)
├── +layout.svelte            # Root layout
├── about/
│   └── +page.svelte          # /about
├── blog/
│   ├── +page.svelte          # /blog
│   └── [slug]/
│       └── +page.svelte      # /blog/[slug]
└── api/
    └── posts/
        └── +server.ts        # API endpoint
```

## Data Fetching

### Load Functions (+page.ts or +page.server.ts)
```typescript
// +page.ts (Universal - runs on both server and client)
export async function load({ fetch, params }) {
  const response = await fetch(`/api/posts/${params.slug}`);
  const post = await response.json();
  
  return {
    post
  };
}

// +page.server.ts (Server-only)
export async function load({ params }) {
  // Database queries, secret keys, etc.
  const post = await db.query.posts.findFirst({
    where: eq(posts.id, params.slug)
  });
  
  return {
    post
  };
}
```

### Use in Component
```svelte
<script lang="ts">
interface Props {
  data: {
    post: Post;
  };
}

let { data }: Props = $props();
</script>

<h1>{data.post.title}</h1>
<p>{data.post.content}</p>
```

## Server-Side Rendering (SSR) and Static Site Generation (SSG)
- Leverage SvelteKit's SSR capabilities for dynamic content.
- Implement SSG for static pages using prerender option.
- Use the adapter-auto for automatic deployment configuration.

```typescript
// +page.ts
export const prerender = true; // Static generation
// export const ssr = false;    // Client-side only
```

## Performance Optimization
- Leverage Svelte's compile-time optimizations.
- Use `{#key}` blocks to force re-rendering of components when needed.
- Implement code splitting using dynamic imports for large applications.
- Profile and monitor performance using browser developer tools.
- Use `$effect.tracking()` to optimize effect dependencies.
- Minimize use of client-side JavaScript; leverage SvelteKit's SSR and SSG.
- Implement proper lazy loading for images and other assets.

## Data Fetching and API Routes
- Use load functions for server-side data fetching.
- Implement proper error handling for data fetching operations.
- Create API routes in the `src/routes/api/` directory.
- Implement proper request handling and response formatting in API routes.
- Use SvelteKit's hooks for global API middleware.

## SEO and Meta Tags
```svelte
<svelte:head>
  <title>Page Title</title>
  <meta name="description" content="Page description" />
  <link rel="canonical" href="https://example.com/page" />
</svelte:head>
```

## Forms and Actions

### Form Actions (+page.server.ts)
```typescript
import type { Actions } from './$types';

export const actions = {
  default: async ({ request }) => {
    const data = await request.formData();
    const email = data.get('email');
    
    // Process form
    
    return { success: true };
  }
} satisfies Actions;
```

### Use in Component
```svelte
<script lang="ts">
import { enhance } from '$app/forms';

let { form } = $props();
</script>

<form method="POST" use:enhance>
  <input name="email" type="email" required />
  <button type="submit">Submit</button>
  
  {#if form?.success}
    <p>Success!</p>
  {/if}
</form>
```

## Internationalization (i18n) with Paraglide.js
- Use Paraglide.js for internationalization: https://inlang.com/m/gerre34r/library-inlang-paraglideJs
- Install Paraglide.js: `npm install @inlang/paraglide-js`
- Set up language files in the `languages` directory.
- Use the `t` function to translate strings:
  ```svelte
  <script>
  import { t } from '@inlang/paraglide-js';
  </script>

  <h1>{t('welcome_message')}</h1>
  ```
- Support multiple languages and RTL layouts.
- Ensure text scaling and font adjustments for accessibility.

## Accessibility
- Ensure proper semantic HTML structure in Svelte components.
- Implement ARIA attributes where necessary.
- Ensure keyboard navigation support for interactive elements.
- Use Svelte's `bind:this` for managing focus programmatically.

## Key Conventions
1. Embrace Svelte's simplicity and avoid over-engineering solutions.
2. Use SvelteKit for full-stack applications with SSR and API routes.
3. Prioritize Web Vitals (LCP, FID, CLS) for performance optimization.
4. Use environment variables for configuration management.
5. Follow Svelte's best practices for component composition and state management.
6. Ensure cross-browser compatibility by testing on multiple platforms.
7. Keep your Svelte and SvelteKit versions up to date.

## Important Changes (Svelte 4 → 5)
- ✅ `on:click` → `onclick`
- ✅ `on:input` → `oninput`
- ✅ `on:submit` → `onsubmit`
- ✅ `let:` → `{#snippet}`
- ✅ `export let` → `$props()`
- ✅ Reactive statements (`$:`) → `$derived` and `$effect`
- ✅ Stores are now optional (use runes for state management)

## Documentation
- Svelte 5 Runes: https://svelte-5-preview.vercel.app/docs/runes
- Svelte Documentation: https://svelte.dev/docs
- SvelteKit Documentation: https://kit.svelte.dev/docs
- Paraglide.js Documentation: https://inlang.com/m/gerre34r/library-inlang-paraglideJs/usage

Refer to Svelte, SvelteKit, and Paraglide.js documentation for detailed information on components, internationalization, and best practices.