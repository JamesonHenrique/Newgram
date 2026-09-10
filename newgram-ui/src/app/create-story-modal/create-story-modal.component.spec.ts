import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateStoryModalComponent } from './create-story-modal.component';

describe('CreateStoryModalComponent', () => {
  let component: CreateStoryModalComponent;
  let fixture: ComponentFixture<CreateStoryModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateStoryModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateStoryModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
