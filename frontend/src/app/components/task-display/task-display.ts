import { Component, HostListener, Input } from '@angular/core';
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

  /** Lightbox: enlarged image src when user clicks a task image. */
  zoomedSrc: string | null = null;
  zoomedAlt = '';

  constructor(private sanitizer: DomSanitizer) {}

  @HostListener('document:keydown', ['$event'])
  onDocumentKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape' && this.zoomedSrc) {
      event.preventDefault();
      this.closeZoom();
    }
  }

  onContentClick(event: MouseEvent): void {
    const el = event.target;
    if (el instanceof HTMLImageElement) {
      event.preventDefault();
      const src = el.currentSrc || el.src;
      if (src) {
        this.zoomedSrc = src;
        this.zoomedAlt = el.alt || '';
      }
    }
  }

  closeZoom(): void {
    this.zoomedSrc = null;
    this.zoomedAlt = '';
  }

  get safeHtml(): SafeHtml | null {
    if (this.kind === 'email' || this.kind === 'post' || this.kind === 'sms' || this.kind === 'site' || this.kind === 'website') {
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
    return this.kind === 'email' || this.kind === 'post' || this.kind === 'sms' || this.kind === 'site' || this.kind === 'website';
  }
}
