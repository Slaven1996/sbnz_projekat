# Hydro Monitoring — Frontend

React + TypeScript SPA for the Hydro Monitoring Knowledge-Based System. It talks
to the Spring Boot backend in [`be/service`](../be/service) (default
`http://localhost:8090`).

## Tech stack

| Concern            | Library                                              |
| ------------------ | ---------------------------------------------------- |
| Framework          | React 18 + TypeScript + Vite                         |
| Server state / API | TanStack Query (React Query) + Axios                 |
| Client state       | Redux Toolkit (auth only: JWT, e-mail, role)         |
| Routing            | React Router DOM v6                                  |
| UI / styling       | Material-UI (MUI) v5 + icons                         |
| Tables             | TanStack Table (headless) rendered with MUI Table    |
| Forms / validation | React Hook Form + Zod                                |

## Getting started

```bash
cd fe
npm install
npm run dev      # http://localhost:5173
```

Configure the backend URL in [.env](.env):

```
VITE_API_BASE_URL=http://localhost:8090
```

Sign in with the seeded admin (see `be/service/.../data.sql`):

- **admin@hydro.local** / **admin123**

## Architecture

```
src/
  api/            Axios client, shared types, per-entity CRUD resources, auth & trend-data calls
  auth/           JWT decode helpers (client-side, no verification)
  store/          Redux store + authSlice + typed hooks
  hooks/          createCrudResource (RQ hook factory), useCrudController, useTableState
  components/     Reusable DataTable, FormDialog, ConfirmDialog, RHF fields, notifications
  layout/         MainLayout (sidebar + appbar), Sidebar, nav config
  routes/         ProtectedRoute (auth gate) + RoleRoute (role gate)
  pages/          Login, Profile, and one CRUD page per entity
```

### How the layers fit together

- **Auth** lives in Redux. On login we store the JWT (+ role) and persist it to
  `localStorage`; the e-mail and expiry are decoded from the token. Axios attaches
  `Authorization: Bearer <token>` to every request and a `401` interceptor logs out.
- **Data** is never kept in Redux. Every list/detail/mutation goes through React
  Query so caching and invalidation are automatic. `createCrudResource` builds a
  typed client + hooks (`useList`, `useOptions`, `useCreate`, `useUpdate`,
  `useRemove`) for each backend controller — see [src/api/resources.ts](src/api/resources.ts).
- **RBAC in the UI** is driven by the role in Redux:
  - The sidebar renders only the tabs allowed for the role
    ([navConfig](src/layout/navConfig.tsx)).
  - `RoleRoute` blocks direct-URL access to admin-only pages (Users).
  - Each page hides Add/Edit/Delete for non-admins (OPERATOR = read-only), which
    mirrors the backend (`GET` open to ADMIN+OPERATOR, writes ADMIN-only).
- **CRUD UX** is identical across entities: an "Add" button above a `DataTable`,
  a shared `FormDialog` (RHF + Zod) reused for create and edit, and a
  `ConfirmDialog` before deletes. `useCrudController` centralises the dialog/delete
  state, mutations and toasts.

## Entities & endpoints

| Page             | Endpoint                  | Notes                                            |
| ---------------- | ------------------------- | ------------------------------------------------ |
| Departments      | `/api/departments`        | full CRUD (admin)                                |
| Zones            | `/api/zones`              | full CRUD (admin); shows location count          |
| Locations        | `/api/locations`          | full CRUD; **weather is nested** in the form     |
| Sensors          | `/api/sensors`            | full CRUD; FK to location + tag unit             |
| Tag Units        | `/api/tag-units`          | full CRUD                                         |
| Threshold Configs| `/api/threshold-configs`  | full CRUD; enum-driven                           |
| Trend Data       | `/api/trend-data`         | **read-only**, filter by location/tag/date range |
| Users            | `/api/users`              | full CRUD, **ADMIN only**                        |

Pagination/sorting use Spring's `page` / `size` / `sort=field,dir` query params;
responses use the `PagedResponse` envelope.

## Notes / limitations

- **My Profile** shows e-mail, role and session expiry decoded from the JWT.
  There is no `/auth/me` endpoint, so name/department aren't fetched there
  (OPERATORs also can't read `/api/users`). Add a `/auth/me` endpoint to enrich it.
- Registration (`POST /auth/register`) exists on the backend and in
  [src/api/auth.ts](src/api/auth.ts) but is intentionally not surfaced in the UI;
  accounts are created from the admin **Users** page.
```
