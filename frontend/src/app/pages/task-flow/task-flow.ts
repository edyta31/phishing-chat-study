import { Component, OnInit, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { StudyService, TaskPayload } from '../../services/study.service';
import { TaskDisplay } from '../../components/task-display/task-display';
import { Chatbot } from '../../components/chatbot/chatbot';

type Step = 'task' | 'after';

/** Range input value before a 1–5 choice (leftmost step on the slider). */
const SLIDER_UNSELECTED = 0;

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
  confidence = signal<number | null>(null);
  trustInBot = signal<number | null>(null);
  confidenceTouched = signal(false);
  trustInBotTouched = signal(false);
  readonly sliderUnselected = SLIDER_UNSELECTED;
  readonly likertSteps = [0, 1, 2, 3, 4, 5] as const;
  afterUsedChatbot = signal<boolean | null>(null);
  submitting = signal(false);
  /** Effective "did they use the assistant" for gating the trust rating. */
  didUseAssistant = computed(() => this.afterUsedChatbot() ?? this.usedChatbot());
  /** Trust rating is only required when the assistant was used. */
  trustRequired = computed(() => this.didUseAssistant() === true);
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

  private resetTaskForm(): void {
    this.step.set('task');
    this.decision.set(null);
    this.confidence.set(null);
    this.trustInBot.set(null);
    this.confidenceTouched.set(false);
    this.trustInBotTouched.set(false);
    this.usedChatbot.set(false);
    this.afterUsedChatbot.set(null);
  }

  fetchNext(): void {
    this.loading.set(true);
    this.error.set('');
    this.resetTaskForm();
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
    if (!this.decision() || !this.confidenceTouched()) return;
    this.step.set('after');
  }

  submitAndNext(): void {
    const t = this.task();
    if (!t) return;
    const token = this.study.getStoredToken();
    if (!token) return;
    const mustRateTrust = this.trustRequired();
    if (mustRateTrust && !this.trustInBotTouched()) return;
    this.submitting.set(true);
    this.study.submitDecision({
      trialId: t.trialId,
      token,
      decision: this.decision()!,
      confidence: this.confidence()!,
      usedChatbot: this.afterUsedChatbot() ?? this.usedChatbot(),
      trustInBot: mustRateTrust ? this.trustInBot()! : undefined
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

  onConfidenceInput(value: number): void {
    if (value === SLIDER_UNSELECTED) {
      this.confidenceTouched.set(false);
      this.confidence.set(null);
      return;
    }
    this.confidenceTouched.set(true);
    this.confidence.set(value);
  }

  onTrustInBotInput(value: number): void {
    if (value === SLIDER_UNSELECTED) {
      this.trustInBotTouched.set(false);
      this.trustInBot.set(null);
      return;
    }
    this.trustInBotTouched.set(true);
    this.trustInBot.set(value);
  }

  setAfterUsedChatbot(value: boolean): void {
    this.afterUsedChatbot.set(value);
    // If the assistant wasn't used, we must not collect a trust rating.
    if (!value) {
      this.trustInBotTouched.set(false);
      this.trustInBot.set(null);
    }
  }

  /** Align tick labels with native range thumb centers (0–5 steps). */
  tickLeft(step: number): string {
    return `calc(0.5rem + ${step / 5} * (100% - 1rem))`;
  }

  tickLabel(step: number): string {
    return step === 0 ? 'n/a' : String(step);
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
