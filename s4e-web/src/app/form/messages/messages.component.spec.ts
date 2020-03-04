import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MessagesComponent} from './messages.component';
import {TestingConfigProvider} from '../../app.configuration.spec';
import {S4EFormsModule} from '../form.module';

describe('MessagesComponent', () => {
  let component: MessagesComponent;
  let fixture: ComponentFixture<MessagesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [S4EFormsModule],
      providers: [TestingConfigProvider]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MessagesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
