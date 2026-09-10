import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateHighlightModalComponent } from './create-highlight-modal.component';

describe('CreateHighlightModalComponent', () => {
  let component: CreateHighlightModalComponent;
  let fixture: ComponentFixture<CreateHighlightModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateHighlightModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateHighlightModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
