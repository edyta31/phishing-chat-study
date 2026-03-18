import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import {environment} from '../../environments/environment.prod';

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

@Injectable({ providedIn: 'root' })
export class StudyService {
  private token: string | null = null;

  constructor(private http: HttpClient) {}

  getStoredToken(): string | null {
    return this.token ?? sessionStorage.getItem('studyToken');
  }

  setToken(t: string): void {
    this.token = t;
    sessionStorage.setItem('studyToken', t);
  }

  register(token: string): Observable<RegisterResult> {
    return this.http.post<RegisterResult>(`${API}/register`, { token }).pipe(
      tap(() => this.setToken(token))
    );
  }

  getNextTask(): Observable<NextTaskResult> {
    const t = this.getStoredToken();
    if (!t) throw new Error('No token');
    return this.http.get<NextTaskResult>(`${API}/next`, { params: { token: t } });
  }

  sendChat(trialId: number, userText: string): Observable<{ answer: string }> {
    const t = this.getStoredToken();
    if (!t) throw new Error('No token');
    return this.http.post<{ answer: string }>(`${API}/chat`, {
      trialId,
      token: t,
      userText
    });
  }

  submitDecision(payload: DecidePayload): Observable<{ done: boolean }> {
    return this.http.post<{ done: boolean }>(`${API}/decide`, payload);
  }

  getCompleteRedirect(): Observable<{ redirect: string }> {
    const t = this.getStoredToken();
    if (!t) throw new Error('No token');
    return this.http.get<{ redirect: string }>(`${API}/complete`, { params: { token: t } });
  }

  getPreQuestionnaireRedirect(uid: string): Observable<{ redirect: string }> {
    return this.http.get<{ redirect: string }>(`${API}/pre`, { params: { uid } });
  }
}
