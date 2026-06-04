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

// Storyline
export const storylineApi = {
  getQuestChain: (userId = 'demo-user', questId = 'quest-hue-imperial') =>
    apiRequest<QuestChainResponse>('/storyline/quest', {
      params: { user_id: userId, quest_id: questId },
    }),
  getNextTask: (userId = 'demo-user') =>
    apiRequest<StorylineNextTaskResponse>('/storyline/next-task', {
      params: { user_id: userId },
    }),
};

export default apiRequest;
