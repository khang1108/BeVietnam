/**
 * BeVietnam API Client — centralized fetch wrapper.
 *
 * All API calls go through `apiRequest<T>()`.
 * Token is automatically attached to requests when available.
 * NEVER log passwords or tokens to the console.
 */

import type {
  ApiResponse,
  TokenResponse,
  UserRegisterRequest,
  UserLoginRequest,
  FeedResponse,
  PlacesResponse,
  UserResponse,
} from '@/lib/types';

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000/api/v1';

const TOKEN_KEY = 'bevietnam-token';

/* ── Token Manager ────────────────────────────────────────────────────────── */

export const tokenManager = {
  getToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem(TOKEN_KEY);
  },

  setToken(token: string): void {
    if (typeof window === 'undefined') return;
    localStorage.setItem(TOKEN_KEY, token);
  },

  removeToken(): void {
    if (typeof window === 'undefined') return;
    localStorage.removeItem(TOKEN_KEY);
  },
};

/* ── Core Request ─────────────────────────────────────────────────────────── */

interface ApiOptions extends RequestInit {
  params?: Record<string, string>;
}

async function apiRequest<T>(
  endpoint: string,
  options: ApiOptions = {},
): Promise<ApiResponse<T>> {
  const { params, ...fetchOptions } = options;

  let url = `${API_BASE_URL}${endpoint}`;
  if (params) {
    url += `?${new URLSearchParams(params).toString()}`;
  }

  const defaultHeaders: Record<string, string> = {
    'Content-Type': 'application/json',
  };

  const token = tokenManager.getToken();
  if (token) {
    defaultHeaders['Authorization'] = `Bearer ${token}`;
  }

  try {
    const response = await fetch(url, {
      ...fetchOptions,
      headers: { ...defaultHeaders, ...fetchOptions.headers },
    });

    if (response.ok) {
      const data = (await response.json()) as T;
      return { data, error: null, status: response.status };
    }

    const errorMessage = await parseErrorMessage(response);
    return { data: null, error: errorMessage, status: response.status };
  } catch {
    return { data: null, error: 'Không thể kết nối đến server', status: 0 };
  }
}

/** Parse error message from FastAPI HTTPException response body. */
async function parseErrorMessage(response: Response): Promise<string> {
  try {
    const body = await response.json();
    if (typeof body?.detail === 'string') return body.detail;
    if (typeof body?.message === 'string') return body.message;
  } catch {
    /* response body is not JSON — fall through */
  }
  return `HTTP ${response.status}: ${response.statusText}`;
}

/* ── Auth API ─────────────────────────────────────────────────────────────── */

export const authApi = {
  register(body: UserRegisterRequest) {
    return apiRequest<TokenResponse>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(body),
    });
  },

  login(body: UserLoginRequest) {
    return apiRequest<TokenResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(body),
    });
  },

  me() {
    return apiRequest<UserResponse>('/auth/me');
  },
};

/* ── Feed API ─────────────────────────────────────────────────────────────── */

export const feedApi = {
  getFeed(params?: Record<string, string>) {
    return apiRequest<FeedResponse>('/feed', { params });
  },
};

/* ── Places API ───────────────────────────────────────────────────────────── */

export const placesApi = {
  getPlaces(params?: Record<string, string>) {
    return apiRequest<PlacesResponse>('/places', { params });
  },

  getPlace(id: string) {
    return apiRequest<unknown>(`/place/${id}`);
  },
};

/* ── Events API ───────────────────────────────────────────────────────────── */

export const eventsApi = {
  getEvents(params?: Record<string, string>) {
    return apiRequest<unknown>('/events', { params });
  },

  getEvent(id: string) {
    return apiRequest<unknown>(`/event/${id}`);
  },
};

/* ── Community API ────────────────────────────────────────────────────────── */

export const communityApi = {
  submitContribution(data: Record<string, unknown>) {
    return apiRequest<unknown>('/community', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },
};

export default apiRequest;
