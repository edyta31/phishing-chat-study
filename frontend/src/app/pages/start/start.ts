import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { StudyService } from '../../services/study.service';

@Component({
  selector: 'app-start',
  standalone: true,
  templateUrl: './start.html',
  styleUrl: './start.css'
})
export class StartComponent implements OnInit {
  error = '';
  loading = true;
  uid: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private study: StudyService
  ) {}

  ngOnInit(): void {
    const skipPre = this.route.snapshot.queryParamMap.get('skipPre');
    const wantsSkipPre = skipPre === '1' || skipPre === 'true';

    if (wantsSkipPre) {
      // Test path: skip LimeSurvey only if the server allows it (see study.allow-skip-pre-questionnaire).
      this.loading = true;
      this.study.getStudyConfig().subscribe({
        next: (cfg) => {
          if (!cfg.allowSkipPreQuestionnaire) {
            this.error =
              'Skipping the pre-questionnaire is not enabled on this server. For production studies, complete the questionnaire first.';
            this.loading = false;
            return;
          }
          const storedUid = sessionStorage.getItem('studyUid');
          const uid = storedUid ?? this.generateUid();
          sessionStorage.setItem('studyUid', uid);
          this.study.register(uid).subscribe({
            next: () => {
              this.loading = false;
              this.router.navigate(['/study'], { replaceUrl: true });
            },
            error: (err) => {
              this.error = err?.message || 'Could not start the study. Please try again.';
              this.loading = false;
            }
          });
        },
        error: () => {
          this.error = 'Could not reach the server to verify study settings. Please try again.';
          this.loading = false;
        }
      });
      return;
    }

    const uidFromUrl = this.route.snapshot.queryParamMap.get('uid');
    if (uidFromUrl) {
      // Returning from LimeSurvey: register under the same uid and start the task flow.
      this.study.register(uidFromUrl).subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/study']);
        },
        error: (err) => {
          this.error = err?.message || 'Could not start the study. Please try again.';
          this.loading = false;
        }
      });
      return;
    }

    // First visit: generate uid and send the participant to LimeSurvey pre-questionnaire.
    const storedUid = sessionStorage.getItem('studyUid');
    this.uid = storedUid ?? this.generateUid();
    sessionStorage.setItem('studyUid', this.uid);
    this.loading = false;
  }

  goToPreQuestionnaire(): void {
    if (!this.uid) {
      this.error = 'Could not create a study id. Please reload the page.';
      return;
    }
    this.loading = true;
    this.study.getPreQuestionnaireRedirect(this.uid).subscribe({
      next: (res) => {
        window.location.href = res.redirect;
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.message || 'Could not open the pre-questionnaire.';
      }
    });
  }

  private generateUid(): string {
    // Modern browsers
    if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
      return crypto.randomUUID();
    }
    // Fallback: sufficiently unique for study testing.
    return 'uid-' + Math.random().toString(36).slice(2) + '-' + Date.now().toString(36);
  }
}
