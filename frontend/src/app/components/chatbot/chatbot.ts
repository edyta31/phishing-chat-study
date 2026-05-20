import {
  Component,
  Input,
  Output,
  EventEmitter,
  signal,
  OnChanges,
  SimpleChanges,
  viewChild,
  ElementRef,
  effect
} from '@angular/core';
import { StudyService } from '../../services/study.service';

/**
 * Fixed order (see DataSeeder): mail1, site1, mail3, message1, mail2 (ambivalent).
 * Short, deterministic “initial take” per task — same wording every time for the same example.
 */
const INITIAL_RECOMMENDATION_BY_TASK_INDEX: readonly string[] = [
  'At first glance, this looks risky to me — the sender setup and the request don’t quite match a normal internal flow.',
  'This looks like a normal UPS sign-in page to me — branding and the start of the flow look consistent.',
  'This reads like a routine mailbox/storage notice — I’d treat it as normal unless something feels off to you.',
  'This looks like a typical delivery update to me — nothing here jumps out as obviously wrong.',
  'I can see both sides — some wording is a bit generic, but the rest could fit a normal order update. If you’re unsure, it’s worth a quick double-check.'
];

/** Tap-to-send suggestions per task index (same order as DataSeeder). */
const QUICK_QUESTIONS_BY_TASK_INDEX: readonly string[][] = [
  ['Is it phishing?', 'Can I click the link in the email?'],
  ['Is this website legitimate?', 'Is it safe to enter my details here?'],
  ['Is it phishing?', 'Should I trust this sender?'],
  ['Is this phishing?', 'Should I tap the link in this message?'],
  ['Is it phishing?', 'Does this look like a real order email?']
];

@Component({
  selector: 'app-chatbot',
  standalone: true,
  templateUrl: './chatbot.html',
  styleUrl: './chatbot.css'
})
export class Chatbot implements OnChanges {
  private readonly messagesScroll = viewChild<ElementRef<HTMLElement>>('messagesScroll');

  /** Served from `public/examples/` (see angular.json assets). */
  readonly assistantAvatarSrc = '/examples/chatbotAvatar.svg';

  /** Shown in the peek bubble (collapsed) and as the first assistant turn when the thread is empty. */
  readonly assistantIntro =
    "Hi — I'm the study assistant. If anything feels unclear or you want to think the example through together, just message me. Happy to help :)";

  @Input() trialId = 0;
  /** 0-based position in the fixed study sequence (same example → same index → same initial recommendation). */
  @Input() taskIndex = 0;
  @Output() chatUsed = new EventEmitter<void>();

  /** Chat panel open by default so participants see the greeting and initial take without expanding. */
  open = signal(true);
  inputText = signal('');
  loading = signal(false);
  messages = signal<{ role: 'user' | 'bot'; text: string }[]>([]);

  constructor(private study: StudyService) {
    effect(() => {
      this.messages();
      this.loading();
      this.open();
      queueMicrotask(() => {
        requestAnimationFrame(() => {
          requestAnimationFrame(() => this.scrollMessagesToBottom());
        });
      });
    });
  }

  private scrollMessagesToBottom(): void {
    const el = this.messagesScroll()?.nativeElement;
    if (!el) return;
    el.scrollTop = el.scrollHeight;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['trialId'] || changes['taskIndex']) {
      this.messages.set([]);
    }
  }

  initialRecommendation(): string {
    const i = this.taskIndex;
    if (i >= 0 && i < INITIAL_RECOMMENDATION_BY_TASK_INDEX.length) {
      return INITIAL_RECOMMENDATION_BY_TASK_INDEX[i];
    }
    return 'If you want a quick second opinion, ask me what stands out to you in this example.';
  }

  /** Preset questions shown above the input; same task → same list. */
  quickQuestions(): string[] {
    const i = this.taskIndex;
    if (i >= 0 && i < QUICK_QUESTIONS_BY_TASK_INDEX.length) {
      return [...QUICK_QUESTIONS_BY_TASK_INDEX[i]];
    }
    return ['Is it phishing?', 'Is this legitimate?'];
  }

  toggle(): void {
    this.open.update(v => !v);
  }

  send(): void {
    const text = this.inputText().trim();
    if (!text || this.trialId <= 0 || this.loading()) return;
    this.inputText.set('');
    this.sendMessage(text);
  }

  /** Send a preset line (quick-reply chip) — same behaviour as typing and sending. */
  sendPreset(text: string): void {
    const t = text.trim();
    if (!t) return;
    this.sendMessage(t);
  }

  private sendMessage(text: string): void {
    if (!text || this.trialId <= 0 || this.loading()) return;
    this.chatUsed.emit();
    this.messages.update(m => [...m, { role: 'user', text }]);
    this.loading.set(true);
    this.study.sendChat(this.trialId, text).subscribe({
      next: (res) => {
        this.messages.update(m => [...m, { role: 'bot', text: res.answer }]);
        this.loading.set(false);
      },
      error: () => {
        this.messages.update(m => [...m, { role: 'bot', text: 'Sorry, something went wrong.' }]);
        this.loading.set(false);
      }
    });
  }
}
