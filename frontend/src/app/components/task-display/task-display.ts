import { Component, Input } from '@angular/core';
import { DomSanitizer, SafeHtml, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-task-display',
  standalone: true,
  templateUrl: './task-display.html',
  styleUrl: './task-display.css'
})
export class TaskDisplay {
  @Input() payload = '';
  @Input() kind: string = 'email';

  constructor(private sanitizer: DomSanitizer) {}

  get safeHtml(): SafeHtml | null {
    if (this.kind === 'email' || this.kind === 'post' || this.kind === 'sms' || this.kind === 'site') {
      return this.sanitizer.bypassSecurityTrustHtml(this.payload || '');
    }
    return null;
  }

  get safeUrl(): SafeResourceUrl | null {
    if (this.kind === 'site' && this.payload) {
      return this.sanitizer.bypassSecurityTrustResourceUrl(this.payload);
    }
    return null;
  }

  get isEmailOrPost(): boolean {
    return this.kind === 'email' || this.kind === 'post' || this.kind === 'sms' || this.kind === 'site';
  }
}
