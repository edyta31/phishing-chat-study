import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, tap } from 'rxjs';
import { environment } from '../../environments/environment.prod';
import { MOCK_STUDY_LOCAL, MOCK_NEXT_TASK_RESULT } from '../dev-mock-study';

//const API = '/api';
const API = environment.apiUrl;

export interface TaskPayload {
  trialId: number;
  taskId: number;
  kind: string;
  title: string;
  payload: string;
}

export interface RegisterResult {
  token: string;
  totalTasks: number;
}

export interface NextTaskResult {
  done: boolean;
  task: TaskPayload | null;
  currentIndex: number;
  totalTasks: number;
}

export interface DecidePayload {
  trialId: number;
  token: string;
  decision: 'phish' | 'legit';
  confidence?: number;
  usedChatbot?: boolean;
  trustInBot?: number;
}

export interface StudyPublicConfig {
  allowSkipPreQuestionnaire: boolean;
}

@Injectable({ providedIn: 'root' })
export class StudyService {
  private token: string | null = null;

  constructor(private http: HttpClient) {}

  /** True when local mock mode is on (see dev-mock-study.ts). */
  isMockStudy(): boolean {
    return MOCK_STUDY_LOCAL;
  }

  getStoredToken(): string | null {
    if (MOCK_STUDY_LOCAL) {
      return this.token ?? sessionStorage.getItem('studyToken') ?? 'mock-local';
    }
    return this.token ?? sessionStorage.getItem('studyToken');
  }

  setToken(t: string): void {
    this.token = t;
    sessionStorage.setItem('studyToken', t);
  }

  /** Whether this deployment allows skipping LimeSurvey (see /start?skipPre=1). */
  getStudyConfig(): Observable<StudyPublicConfig> {
    if (MOCK_STUDY_LOCAL) {
      return of({ allowSkipPreQuestionnaire: true });
    }
    return this.http.get<StudyPublicConfig>(`${API}/config`);
  }

  register(token: string): Observable<RegisterResult> {
    if (MOCK_STUDY_LOCAL) {
      return of({ token, totalTasks: MOCK_NEXT_TASK_RESULT.totalTasks }).pipe(
        tap(() => this.setToken(token))
      );
    }
    return this.http.post<RegisterResult>(`${API}/register`, { token }).pipe(
      tap(() => this.setToken(token))
    );
  }

  getNextTask(): Observable<NextTaskResult> {
    if (MOCK_STUDY_LOCAL) {
      return of(MOCK_NEXT_TASK_RESULT);
    }
    const t = this.getStoredToken();
    if (!t) throw new Error('No token');
    return this.http.get<NextTaskResult>(`${API}/next`, { params: { token: t } });
  }

  sendChat(trialId: number, userText: string): Observable<{ answer: string }> {
    if (MOCK_STUDY_LOCAL) {
      return of({
        answer:
          '(Local preview) This is a placeholder reply. The live study uses the server assistant.'
      });
    }
    const t = this.getStoredToken();
    if (!t) throw new Error('No token');
    return this.http.post<{ answer: string }>(`${API}/chat`, {
      trialId,
      token: t,
      userText
    });
  }

  submitDecision(payload: DecidePayload): Observable<{ done: boolean }> {
    if (MOCK_STUDY_LOCAL) {
      return of({ done: true });
    }
    return this.http.post<{ done: boolean }>(`${API}/decide`, payload);
  }

  getCompleteRedirect(): Observable<{ redirect: string }> {
    if (MOCK_STUDY_LOCAL) {
      return of({ redirect: '' });
    }
    const t = this.getStoredToken();
    if (!t) throw new Error('No token');
    return this.http.get<{ redirect: string }>(`${API}/complete`, { params: { token: t } });
  }

  getPreQuestionnaireRedirect(uid: string): Observable<{ redirect: string }> {
    if (MOCK_STUDY_LOCAL) {
      return of({ redirect: '/study' });
    }
    return this.http.get<{ redirect: string }>(`${API}/pre`, { params: { uid } });
  }
}
