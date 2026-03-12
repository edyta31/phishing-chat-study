import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TaskFlow } from './task-flow';

describe('TaskFlow', () => {
  let component: TaskFlow;
  let fixture: ComponentFixture<TaskFlow>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskFlow]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TaskFlow);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
