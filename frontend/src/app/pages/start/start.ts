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

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private study: StudyService
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.error = 'No study link. Please use the link from the pre-questionnaire (LimeSurvey).';
      this.loading = false;
      return;
    }
    this.study.register(token).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/study']);
      },
      error: (err) => {
        this.error = err?.message || 'Could not start the study. Please try again.';
        this.loading = false;
      }
    });
  }
}
