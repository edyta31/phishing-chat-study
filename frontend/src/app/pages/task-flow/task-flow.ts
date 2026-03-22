import { Component, OnInit, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { StudyService, TaskPayload } from '../../services/study.service';
import { TaskDisplay } from '../../components/task-display/task-display';
import { Chatbot } from '../../components/chatbot/chatbot';

type Step = 'task' | 'after';

@Component({
  selector: 'app-task-flow',
  standalone: true,
  imports: [TaskDisplay, Chatbot],
  templateUrl: './task-flow.html',
  styleUrl: './task-flow.css'
})
export class TaskFlow implements OnInit {
  step = signal<Step>('task');
  task = signal<TaskPayload | null>(null);
  currentIndex = signal(0);
  totalTasks = signal(0);
  loading = signal(true);
  error = signal('');
  usedChatbot = signal(false);
  decision = signal<'phish' | 'legit' | null>(null);
  confidence = signal(3);
  trustInBot = signal(3);
  afterUsedChatbot = signal<boolean | null>(null);
  submitting = signal(false);
  /** Shown when mock mode finishes instead of an external redirect. */
  localPreviewComplete = signal(false);

  isLastTask = computed(() => {
    const idx = this.currentIndex();
    const total = this.totalTasks();
    return total > 0 && idx >= total - 1;
  });

  constructor(
    private router: Router,
    private study: StudyService
  ) {}

  ngOnInit(): void {
    if (!this.study.getStoredToken()) {
      this.router.navigate(['/start']);
      return;
    }
    this.fetchNext();
  }

  fetchNext(): void {
    this.loading.set(true);
    this.error.set('');
    this.step.set('task');
    this.decision.set(null);
    this.usedChatbot.set(false);
    this.afterUsedChatbot.set(null);
    this.study.getNextTask().subscribe({
      next: (res) => {
        this.loading.set(false);
        if (res.done && !res.task) {
          this.redirectToPostQuestionnaire();
          return;
        }
        this.task.set(res.task);
        this.currentIndex.set(res.currentIndex);
        this.totalTasks.set(res.totalTasks);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.message || 'Failed to load task');
      }
    });
  }

  onChatUsed(): void {
    this.usedChatbot.set(true);
  }

  continueToAfter(): void {
    if (!this.decision()) return;
    this.step.set('after');
  }

  submitAndNext(): void {
    const t = this.task();
    if (!t) return;
    const token = this.study.getStoredToken();
    if (!token) return;
    this.submitting.set(true);
    this.study.submitDecision({
      trialId: t.trialId,
      token,
      decision: this.decision()!,
      confidence: this.confidence(),
      usedChatbot: this.afterUsedChatbot() ?? this.usedChatbot(),
      trustInBot: this.trustInBot()
    }).subscribe({
      next: (res) => {
        this.submitting.set(false);
        if (res.done) this.redirectToPostQuestionnaire();
        else this.fetchNext();
      },
      error: (err) => {
        this.submitting.set(false);
        this.error.set(err?.message || 'Failed to submit');
      }
    });
  }

  setDecision(d: 'phish' | 'legit'): void {
    this.decision.set(d);
  }

  private redirectToPostQuestionnaire(): void {
    this.study.getCompleteRedirect().subscribe({
      next: (res) => {
        if (!res.redirect) {
          this.localPreviewComplete.set(true);
          return;
        }
        window.location.href = res.redirect;
      },
      error: () => {
        this.error.set('Study complete. Thank you! You can close this tab.');
      }
    });
  }
}
