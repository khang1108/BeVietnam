/**
 * Shared API types — mirrors backend Pydantic schemas.
 *
 * Keep in sync with backend/app/schemas/*.py
 * Do NOT use `any` for API response types.
 */

/* ── Generic API response wrapper ─────────────────────────────────────────── */

export interface ApiResponse<T> {
  data: T | null;
  error: string | null;
  status: number;
}

/* ── Auth ──────────────────────────────────────────────────────────────────── */

/** POST /auth/register — request body */
export interface UserRegisterRequest {
  name: string;
  email: string;
  password: string;
}

/** POST /auth/login — request body */
export interface UserLoginRequest {
  email: string;
  password: string;
}

/** User object returned by auth endpoints */
export interface UserResponse {
  id: string;
  name: string;
  email: string;
  created_at: string;
}

/** POST /auth/login & /auth/register — response */
export interface TokenResponse {
  access_token: string;
  token_type: string;
  user: UserResponse;
}

/* ── Error ─────────────────────────────────────────────────────────────────── */

/** FastAPI HTTPException detail shape */
export interface ApiErrorDetail {
  detail: string;
}

/* ── Feed ──────────────────────────────────────────────────────────────────── */

export interface FeedItem {
  id: string;
  place_id: string;
  name: string;
  category: string;
  thumbnail_url?: string | null;
  score: number;
  explanation: string;
  created_at: string;
}

export interface FeedResponse {
  items: FeedItem[];
}

/* ── Places ────────────────────────────────────────────────────────────────── */

export interface PlaceSchema {
  id: string;
  name: string;
  description: string;
  category: string;
  latitude?: number;
  longitude?: number;
  image_url?: string;
}

export interface PlacesResponse {
  places: PlaceSchema[];
  total: number;
}
