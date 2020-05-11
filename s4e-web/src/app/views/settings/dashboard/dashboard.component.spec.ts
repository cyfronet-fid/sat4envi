import { DashboardModule } from './dashboard.module';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardComponent } from './dashboard.component';
import {RouterTestingModule} from '@angular/router/testing';
import { DebugElement } from '@angular/core';
import { Router } from '@angular/router';
import { By } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let de: DebugElement;
  let router: Router;
  let spy: any;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        CommonModule,
        RouterTestingModule.withRoutes([]),
        DashboardModule
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;
    router = TestBed.get(Router);
    spy = spyOn(router, 'navigate').and.stub();

    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
