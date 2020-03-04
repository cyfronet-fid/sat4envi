import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {GroupFormComponent} from './group-form.component';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {TestingConfigProvider} from '../../../../app.configuration.spec';
import {SettingsModule} from '../../settings.module';

describe('GroupFormComponent', () => {
  let component: GroupFormComponent;
  let fixture: ComponentFixture<GroupFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        SettingsModule,
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        TestingConfigProvider
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GroupFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
