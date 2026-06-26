/**
 * BeVietnam API Client
 * Fetch wrapper for FastAPI backend endpoints
 */

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000/api/v1';

interface ApiOptions extends RequestInit {
  params?: Record<string, string>;
}

interface ApiResponse<T> {
  data: T | null;
  error: string | null;
  status: number;
}

async function apiRequest<T>(
  endpoint: string,
  options: ApiOptions = {}
): Promise<ApiResponse<T>> {
  const { params, ...fetchOptions } = options;

  let url = `${API_BASE_URL}${endpoint}`;

  if (params) {
    const searchParams = new URLSearchParams(params);
    url += `?${searchParams.toString()}`;
  }

  const defaultHeaders: Record<string, string> = {
    'Content-Type': 'application/json',
  };

  // Add auth token if available
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('bevietnam-token');
    if (token) {
      defaultHeaders['Authorization'] = `Bearer ${token}`;
    }
  }

  try {
    const response = await fetch(url, {
      ...fetchOptions,
      headers: {
        ...defaultHeaders,
        ...fetchOptions.headers,
      },
    });

    const data = response.ok ? await response.json() : null;
    const error = response.ok ? null : `HTTP ${response.status}: ${response.statusText}`;

    return {
      data: data as T,
      error,
      status: response.status,
    };
  } catch (err) {
    return {
      data: null,
      error: err instanceof Error ? err.message : 'Network error',
      status: 0,
    };
  }
}

/* ==========================================
   API Endpoints - Ready for FastAPI integration
   ========================================== */

// Feed
export const feedApi = {
  getFeed: (params?: Record<string, string>) =>
    apiRequest('/feed', { params }),
};

// Places
export const placesApi = {
  getPlaces: (params?: Record<string, string>) =>
    apiRequest('/places', { params }),
  getPlace: (id: string) =>
    apiRequest(`/place/${id}`),
};

// Events
export const eventsApi = {
  getEvents: (params?: Record<string, string>) =>
    apiRequest('/events', { params }),
  getEvent: (id: string) =>
    apiRequest(`/event/${id}`),
};

// Community
export const communityApi = {
  submitContribution: (data: Record<string, unknown>) =>
    apiRequest('/community', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
};

// Auth
export const authApi = {
  login: (email: string, password: string) =>
    apiRequest('/auth', {
      method: 'POST',
      body: JSON.stringify({ email, password, action: 'login' }),
    }),
  register: (name: string, email: string, password: string) =>
    apiRequest('/auth', {
      method: 'POST',
      body: JSON.stringify({ name, email, password, action: 'register' }),
    }),
};

// Storyline types
export interface QuestTask {
  quest_id: string;
  task_id: string;
  step_index: number;
  title: string;
  description: string;
  cultural_explanation: string;
  completion_requirement: string;
  difficulty: string;
  status: 'locked' | 'active' | 'completed';
  next_task_hint?: string;
  reason_codes?: string[];
}

export interface QuestChainResponse {
  quest_id: string;
  place_name: string;
  total_tasks: number;
  current_step: number;
  tasks: QuestTask[];
}

export interface StorylineTaskDetail {
  task_id: string;
  title: string;
  description: string;
  cultural_explanation: string;
  difficulty: string;
  completion_requirement: string;
}

export interface StorylineNextTaskResponse {
  task: StorylineTaskDetail;
  ai_generated: boolean;
  fallback: boolean;
}

export interface StorylineNextTaskParams {
  latitude?: number;
  longitude?: number;
  timeOfDay?: string;
}

export interface QuestionPoolItem {
  question_id: string;
  title: string;
  question_text: string;
  cultural_explanation: string;
  source_text?: string;
  source?: string;
  place_name?: string;
  latitude?: number | null;
  longitude?: number | null;
  radius_meters: number;
  categories: string[];
  difficulty: string;
  required_media: string;
  indoor_outdoor: string;
  weather_tags: string[];
  time_tags: string[];
  estimated_duration_minutes: number;
  language: string;
  metadata: Record<string, unknown>;
}

export interface RuntimeContext {
  latitude: number;
  longitude: number;
  weather: string;
  time_of_day: string;
  formatted_address: string;
  place_name: string;
  source: string;
  resolved_at: string;
}

export interface SelectedQuestion {
  question: QuestionPoolItem;
  score: number;
  reasons: string[];
  distance_meters?: number | null;
}

export interface QuestionPoolSelectionResponse {
  context: RuntimeContext;
  selected: SelectedQuestion[];
  total_pool_size: number;
  fallback: boolean;
}

export interface QuestionPoolSelectRequest {
  user_id: string;
  latitude: number;
  longitude: number;
  weather?: string;
  time_of_day?: string;
  interests?: string[];
  completed_question_ids?: string[];
  limit?: number;
}

export interface QuestionPoolListResponse {
  total: number;
  items: QuestionPoolItem[];
  source_path?: string;
  fallback?: boolean;
}

export const questionPoolApi = {
  list: () => apiRequest<QuestionPoolListResponse>('/question-pool'),
  select: (body: QuestionPoolSelectRequest) =>
    apiRequest<QuestionPoolSelectionResponse>('/question-pool/select', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
};

export type WeatherCondition = 'sunny' | 'cloudy' | 'rainy' | 'hot' | 'any';

export interface WeatherResponse {
  condition: WeatherCondition;
  temp: number | null;
  source: string;
  uvi: number | null;       // estimated UV index
  rain_mm: number | null;   // rain volume last 1h/3h
  clouds: number | null;    // cloud cover %
}

export interface WeatherCoord {
  lat: number;
  lng: number;
}

export interface WeatherBatchResult extends WeatherCoord {
  condition: WeatherCondition;
  temp: number | null;
}

export interface WeatherBatchResponse {
  results: WeatherBatchResult[];
}

export const weatherApi = {
  getWeather: (lat: number, lng: number) =>
    apiRequest<WeatherResponse>('/weather', {
      params: { lat: String(lat), lng: String(lng) },
    }),
  getBatch: (coordinates: WeatherCoord[]) =>
    apiRequest<WeatherBatchResponse>('/weather/batch', {
      method: 'POST',
      body: JSON.stringify({ coordinates }),
    }),
};

// Nearby live POIs (Foursquare proxy) — refreshed as the map viewport moves.
export type NearbyCategory = 'food' | 'lodging' | 'culture' | 'history' | 'nature' | 'place';

export interface NearbyPlace {
  id: string;
  name: string;
  latitude: number;
  longitude: number;
  category: NearbyCategory;
  category_label: string;
  address: string | null;
  distance_meters: number | null;
  rating: number | null;       // 0..10, Foursquare Premium only
  popularity: number | null;   // 0..1, Foursquare Premium only
}

export interface NearbyResponse {
  total: number;
  items: NearbyPlace[];
}

export const nearbyApi = {
  search: (lat: number, lng: number, radius = 2000, limit = 30) =>
    apiRequest<NearbyResponse>('/places/nearby', {
      params: {
        lat: String(lat),
        lng: String(lng),
        radius: String(radius),
        limit: String(limit),
      },
    }),
};

// Map config — backend serves the MapLibre/Goong style URL so the API key
// is never exposed to the client.
export interface MapConfigResponse {
  enabled: boolean;
  style_url: string | null;
}

export const mapApi = {
  getConfig: () => apiRequest<MapConfigResponse>('/maps/config'),
};

// Capture verification (AI Capture Judge via backend proxy)
export interface VerifyCaptureBody {
  user_id: string;
  task: Record<string, unknown>;
  capture: {
    media_url: string; // data URL (base64) in demo, or a hosted image URL
    note?: string;
    place_id?: string;
    latitude?: number;
    longitude?: number;
  };
}

export interface VerifyCaptureResponse {
  approved: boolean;
  status: string; // approved | rejected | needs_review | error
  reason: string;
  confidence: number;
}

// Storyline
export const storylineApi = {
  getQuestChain: (userId = 'demo-user', questId = 'quest-hue-imperial') =>
    apiRequest<QuestChainResponse>('/storyline/quest', {
      params: { user_id: userId, quest_id: questId },
    }),
  getNextTask: (context: StorylineNextTaskParams = {}, userId = 'demo-user') => {
    const params: Record<string, string> = { user_id: userId };
    if (context.latitude !== undefined) params.latitude = String(context.latitude);
    if (context.longitude !== undefined) params.longitude = String(context.longitude);
    if (context.timeOfDay) params.time_of_day = context.timeOfDay;
    return apiRequest<StorylineNextTaskResponse>('/storyline/next-task', { params });
  },
  verifyCapture: (body: VerifyCaptureBody) =>
    apiRequest<VerifyCaptureResponse>('/storyline/verify-capture', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
};

export default apiRequest;
