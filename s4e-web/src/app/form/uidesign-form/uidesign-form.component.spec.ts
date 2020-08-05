import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UIDesignFormComponent } from './uidesign-form.component';
import {S4EFormsModule} from '../form.module';

describe('UIDesignFormComponent', () => {
  let component: UIDesignFormComponent;
  let fixture: ComponentFixture<UIDesignFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [S4EFormsModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UIDesignFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
