// src/app/services/task.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TaskService {

  private apiUrl = 'http://localhost:8080/api';  // URL of the backend

  constructor(private http: HttpClient) { }
/*
  getTask(taskId: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/task/${taskId}`);
  }


  submitResponse(response: UserResponse): Observable<any> {
    return this.http.post(`${this.apiUrl}/submit`, response);
  }

 */
}
