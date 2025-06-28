// API 응답 기본 타입
export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
  errorCode?: string;
}

// 사용자 관련 타입
export interface User {
  id: number;
  email: string;
  nickname: string;
  loginType: "LOCAL" | "KAKAO" | "NAVER";
  createdAt: string;
  updatedAt: string;
  isVerified: boolean;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface UserRequest {
  email: string;
  nickname: string;
  password?: string;
}

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
}

// 알람 관련 타입
export interface AlarmResponse {
  alarmId: number;
  contractId: string;
  alarmType: string;
  note: string;
  extraInfo: string;
  isRead: boolean;
  alarmDate: string;
  createdAt: string;
}

export interface AlarmStats {
  userId: number;
  totalAlarms: number;
  unreadAlarms: number;
  urgentAlarms: number;
  todayAlarms: number;
  weekAlarms: number;
  monthAlarms: number;
}

export interface AlarmMessageResponse {
  message: string;
  processedCount: number;
  processedAt: string;
}

export interface AlarmRequest {
  userId: number;
  contractId: string;
  alarmType: string;
  extraInfo?: string;
  alarmDate: string;
}

// 문서 관련 타입
export interface DocumentResponse {
  id: number;
  originalFilename: string;
  storedFilename: string;
  fileSize: number;
  contentType: string;
  uploadDate: string;
  userId: number;
}

export interface DocumentCreateRequest {
  originalFilename: string;
  contentType: string;
  fileSize: number;
  userId: number;
}

// ML 분석 관련 타입
export interface MLAnalysisRequest {
  contractId: string;
  analysisType: "RISK" | "COMPLIANCE" | "COMPREHENSIVE";
  options?: Record<string, any>;
}

export interface MLAnalysisResult {
  analysisId: string;
  riskLevel: "LOW" | "MEDIUM" | "HIGH";
  score: number;
  details: Record<string, any>;
  createdAt: string;
}

// OCR 관련 타입
export interface OcrRequest {
  imageUrl: string;
  language?: string;
}

export interface OcrResponse {
  text: string;
  confidence: number;
  boundingBoxes: Array<{
    text: string;
    confidence: number;
    boundingBox: {
      x: number;
      y: number;
      width: number;
      height: number;
    };
  }>;
}

// 용어 검색 관련 타입
export interface WordsSearchRequest {
  keyword?: string;
  page?: number;
  pageSize?: number;
}

export interface WordsResponse {
  id: number;
  term: string;
  definition: string;
  category: string;
  searchCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface PopularKeywordsResponse {
  keywords: Array<{
    term: string;
    searchCount: number;
  }>;
}

// 검색 관련 타입
export interface SearchRequest {
  query: string;
  filters?: Record<string, any>;
}

export interface SearchResponse {
  results: Array<{
    id: string;
    title: string;
    content: string;
    relevance: number;
    source: string;
  }>;
  totalCount: number;
  processingTime: number;
}

// 챗봇 관련 타입
export interface ChatbotRequest {
  message: string;
  sessionId?: string;
  context?: Record<string, any>;
}

export interface ChatbotResponse {
  message: string;
  sessionId: string;
  timestamp: string;
  confidence: number;
}

export interface ChatSession {
  sessionId: string;
  title: string;
  createdAt: string;
  lastMessageAt: string;
  messageCount: number;
}

// 상태 관련 타입
export interface StatusResponse {
  status: "UP" | "DOWN" | "DEGRADED";
  timestamp: string;
  service: string;
  version: string;
  components?: Record<
    string,
    {
      status: string;
      details: string;
    }
  >;
}

// 헬스체크 관련 타입
export interface HealthResponse {
  status: string;
  timestamp: string;
  service: string;
  version: string;
  message: string;
  endpoints?: Record<string, string>;
}

// 에러 응답 타입
export interface ErrorResponse {
  success: false;
  message: string;
  errorCode: string;
  timestamp?: string;
}
