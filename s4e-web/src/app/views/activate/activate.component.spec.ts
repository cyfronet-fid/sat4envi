import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {ActivateComponent} from './activate.component';
import {ActivateService} from './state/activate.service';
import {ActivateModule} from './activate.module';
import {RouterTestingModule} from '@angular/router/testing';

describe('ActivateComponent', () => {
  let component: ActivateComponent;
  let fixture: ComponentFixture<ActivateComponent>;
  let activateService: ActivateService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ActivateModule, RouterTestingModule],
      declarations: []
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ActivateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    activateService = TestBed.get(ActivateService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
