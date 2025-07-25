# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Primary Commands
- **Development**: `npm run dev` - Start Vite development server
- **Build**: `npm run build` - Build for production
- **Preview**: `npm run preview` - Preview production build

### Testing
No specific test commands are configured. Check with the user for testing requirements.

## Project Architecture

### Technology Stack
- **Framework**: Vue 3 with Composition API and `<script setup>` SFC
- **Build Tool**: Vite 6.x
- **TypeScript**: Configured with strict mode
- **UI Library**: Element Plus with Ant Design Vue components
- **HTTP Client**: Axios with custom interceptors
- **Router**: Vue Router 4 with permission guards
- **State Management**: Reactive state with Vue 3 composition API

### Core Architecture Patterns

#### Multi-Service Backend Integration
The application connects to multiple backend services:
- **User Center**: `http://115.190.36.226:8080/` (main service)
- **SmartCS**: `http://localhost:8082` (AI chat/knowledge service)

Request routing is handled in `src/api/config.ts` based on URL patterns.

#### Permission System
Comprehensive role-based access control:
- **Permission Guard**: `src/utils/permission.ts` handles route-level permissions
- **Permission Directive**: `v-permission` for component-level access control
- **Permission State**: Reactive permission state management
- **Admin Override**: Admin roles bypass all permission checks

#### Route Structure
- **Authentication**: `/login`, `/register`, `/google-callback`
- **Dashboard**: Nested routes under `/dashboard` with permission requirements
- **Specialized Modules**: 
  - Knowledge management (`/dashboard/knowledge/*`)
  - Customer service (`/customer-service/*`)
  - Logistics (`/logistics/*`)

### Key Components & Patterns

#### API Layer Organization
APIs are organized by service domain:
- `src/api/usercenter/` - User management, auth, roles, permissions
- `src/api/smartcs/` - AI chat, knowledge base, content management  
- `src/api/client-web/` - E-commerce functionality (orders, products, logistics)

#### Component Structure
- **Layout Components**: `src/components/layout/` - Main application shell
- **Feature Components**: Domain-specific components in `src/components/`
- **Views**: Page-level components in `src/views/` organized by feature
- **Composables**: Reusable logic in `src/composables/`

#### WebSocket & Real-time Features
- STOMP.js integration for real-time chat
- SSE (Server-Sent Events) for streaming responses
- Custom WebSocket utilities in `src/utils/`

### Configuration Notes

#### API Configuration
- Base URLs are dynamically set in request interceptors
- JWT token handling in localStorage
- Automatic error handling with Element Plus messages
- CORS disabled (`withCredentials: false`)

#### Build Configuration
- Vite with `@` alias pointing to `src/`
- Sass embedded for styling
- ES2015 target with ESNext modules
- Base path set to `./` for flexible deployment

#### Permission Configuration
**IMPORTANT**: All permission checks are currently disabled (line 161 in `src/utils/permission.ts`). All routes are accessible without permission validation.

### Development Guidelines

#### API Interface Design
Follow the established pattern in `.cursor/rules/api-interface-design.mdc`:
- Admin endpoints use `/aftersale/admin/` prefix
- Consistent request/response format with `success`, `errCode`, `errMessage`, `data` fields
- Pagination uses `pageIndex` (1-based) and `pageSize`
- Use camelCase for request parameters

#### File Organization
- Components organized by feature domain
- API files mirror backend service structure
- TypeScript types in `src/types/`
- Utilities in `src/utils/`
- Consistent naming: camelCase for files, PascalCase for Vue components

### Common Development Patterns

#### Adding New Features
1. Create API interface in appropriate `src/api/` subdirectory
2. Define TypeScript interfaces in `src/types/`
3. Implement Vue component with Element Plus UI
4. Add route with proper permission metadata
5. Update permission system if needed

#### Error Handling
All API errors are handled globally through Axios interceptors. Components should focus on business logic rather than error handling.

#### State Management
Use Vue 3's reactive state pattern rather than Vuex. See `userPermissionState` in `src/utils/permission.ts` for reference implementation.