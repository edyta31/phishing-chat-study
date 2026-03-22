import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { StudyService } from '../../services/study.service';

@Component({
  selector: 'app-chatbot',
  standalone: true,
  templateUrl: './chatbot.html',
  styleUrl: './chatbot.css'
})
export class Chatbot {
  /** Served from `public/examples/` (see angular.json assets). */
  readonly assistantAvatarSrc = '/examples/chatbotAvatar.svg';

  /** Shown in the peek bubble (collapsed) and as the first assistant turn when the thread is empty. */
  readonly assistantIntro =
    "Hi — I'm Edyta, your study assistant. If anything feels unclear or you want to think the example through together, just message me. Happy to help :)";

  @Input() trialId = 0;
  @Output() chatUsed = new EventEmitter<void>();

  open = signal(false);
  inputText = signal('');
  loading = signal(false);
  messages = signal<{ role: 'user' | 'bot'; text: string }[]>([]);

  constructor(private study: StudyService) {}

  toggle(): void {
    this.open.update(v => !v);
  }

  send(): void {
    const text = this.inputText().trim();
    if (!text || this.trialId <= 0 || this.loading()) return;
    this.chatUsed.emit();
    this.messages.update(m => [...m, { role: 'user', text }]);
    this.inputText.set('');
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
