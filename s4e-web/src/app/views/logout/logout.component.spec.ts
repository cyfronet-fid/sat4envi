import {HttpClientTestingModule} from '@angular/common/http/testing';
import {SessionService} from '../../state/session/session.service';
import {RouterTestingModule} from '@angular/router/testing';
import {LogoutModule} from './logout.module';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {LogoutComponent} from './logout.component';

describe('LogoutComponent', () => {
  let component: LogoutComponent;
  let fixture: ComponentFixture<LogoutComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        LogoutModule,
        RouterTestingModule,
        HttpClientTestingModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LogoutComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('Should logout and run notification', () => {
    const sessionService = TestBed.get(SessionService);
    const spySession = spyOn(sessionService, 'logout');

    fixture.detectChanges();

    expect(spySession).toHaveBeenCalled();
  });
});
