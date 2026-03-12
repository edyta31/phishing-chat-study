import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TaskDisplay } from './task-display';

describe('TaskDisplay', () => {
  let component: TaskDisplay;
  let fixture: ComponentFixture<TaskDisplay>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskDisplay]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TaskDisplay);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
