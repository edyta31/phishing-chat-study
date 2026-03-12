import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'start', pathMatch: 'full' },
  { path: 'start', loadComponent: () => import('./pages/start/start').then(m => m.StartComponent) },
  { path: 'study', loadComponent: () => import('./pages/task-flow/task-flow').then(m => m.TaskFlow) }
];
