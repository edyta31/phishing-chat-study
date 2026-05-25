import { Component, HostListener, Input, OnChanges } from '@angular/core';
import { DomSanitizer, SafeHtml, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-task-display',
  standalone: true,
  templateUrl: './task-display.html',
  styleUrl: './task-display.css'
})
export class TaskDisplay implements OnChanges {
  @Input() payload = '';
  @Input() kind: string = 'email';

  /**
   * Cached once per payload/kind change to avoid re-sanitizing on every change detection cycle
   */
  safeHtml: SafeHtml | null = null;
  safeUrl: SafeResourceUrl | null = null;

  /** Lightbox: enlarged image src when user clicks a task image. */
  zoomedSrc: string | null = null;
  zoomedAlt = '';

  constructor(private sanitizer: DomSanitizer) {}

  ngOnChanges(): void {
    if (
      this.kind === 'email' ||
      this.kind === 'post' ||
      this.kind === 'sms' ||
      this.kind === 'site' ||
      this.kind === 'website'
    ) {
      this.safeHtml = this.sanitizer.bypassSecurityTrustHtml(this.payload || '');
    } else {
      this.safeHtml = null;
    }
    if (this.kind === 'site' && this.payload) {
      this.safeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.payload);
    } else {
      this.safeUrl = null;
    }
  }

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

  get isEmailOrPost(): boolean {
    return this.kind === 'email' || this.kind === 'post' || this.kind === 'sms' || this.kind === 'site' || this.kind === 'website';
  }
}
