import { By } from '@angular/platform-browser';
import { of } from 'rxjs';
import { GroupQuery } from './../state/group.query';
import { async, ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import {GroupListComponent} from './group-list.component';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {TestingConfigProvider} from '../../../../app.configuration.spec';
import {SettingsModule} from '../../settings.module';
import { DebugElement } from '@angular/core';

describe('GroupListComponent', () => {
  let component: GroupListComponent;
  let fixture: ComponentFixture<GroupListComponent>;
  let groupQuery: GroupQuery;
  let de: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        SettingsModule,
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [TestingConfigProvider]
    })
      .compileComponents();

      groupQuery = TestBed.get(GroupQuery);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GroupListComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
