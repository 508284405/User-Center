# Qwen Code Customization

This file customizes interactions with Qwen Code for this specific project.

## Project Overview

- **Name**: User Center Website
- **Technology Stack**: Vue 3, Vite, TypeScript, JavaScript
- **Build Tool**: Vite
- **Package Manager**: Yarn
- **Main Framework**: Vue 3 (Composition API)
- **Styling**: SCSS
- **Routing**: Vue Router
- **State Management**: Not explicitly mentioned, likely component-based or using provide/inject
- **API Communication**: Axios (configured in `src/utils/request.ts`)
- **Authentication**: JWT-based (handled in `src/api/usercenter/auth.ts`)
- **UI Components**: Custom components and potentially some third-party components

## Key Directories

- `src/`: Main source code
  - `api/`: API request definitions
  - `components/`: Reusable Vue components
  - `views/`: Page-level components
  - `router/`: Routing configuration
  - `utils/`: Utility functions
  - `types/`: TypeScript type definitions
  - `composables/`: Vue 3 composables for logic reuse
  - `assets/`: Static assets (images, icons)
  - `styles/`: Global styles and variables
- `doc/`: API documentation (Markdown files)
- `public/`: Static files served directly

## Conventions

- **File Naming**: Kebab-case for component files (e.g., `UserProfile.vue`)
- **Component Structure**: Vue 3 Single File Components (SFCs)
- **API Handling**: Defined in `src/api/` with Axios instances
- **Routing**: Defined in `src/router/index.js`
- **State Management**: Component-scoped or passed via props/events for simple cases
- **Styling**: Scoped styles preferred, global variables in `src/styles/variables.scss`
- **Type Safety**: TypeScript used where possible
- **Code Style**: Follows Vue 3 Composition API and general JavaScript/TypeScript best practices

## Key Files

- `src/main.js`: App entry point
- `src/App.vue`: Root Vue component
- `src/router/index.js`: Vue Router setup
- `src/utils/request.ts`: Axios configuration and interceptors
- `src/api/config.ts`: API base configuration
- `src/components/layout/MainLayout.vue`: Main application layout
- `src/views/Login.vue`: Login page
- `src/views/Register.vue`: Registration page
- `src/api/usercenter/auth.ts`: Authentication API calls

## Testing

No specific testing framework mentioned in the provided structure. Tests might be in a separate directory or integrated with the component files.

## Deployment

Built using `yarn build`, output to `dist/` directory. Likely deployed as a static site.

## Notes

- Heavy use of Vue 3 Composition API
- Axios for HTTP requests
- Custom components for UI
- Project appears to be a large-scale admin/dashboard application with multiple modules (after-sales, customer service, knowledge, logistics, order, points, product, user management, etc.)
- Authentication and permissions are handled centrally