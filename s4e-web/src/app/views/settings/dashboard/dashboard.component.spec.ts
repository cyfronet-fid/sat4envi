import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TileComponent } from './../../../components/tiles-dashboard/tile/tile.component';
import { DashboardModule } from './dashboard.module';
import { async, ComponentFixture, TestBed, tick, fakeAsync } from '@angular/core/testing';

import { DashboardComponent } from './dashboard.component';
import {RouterTestingModule} from '@angular/router/testing';
import { DebugElement } from '@angular/core';
import { Router, convertToParamMap, ParamMap, ActivatedRoute, ActivatedRouteSnapshot } from '@angular/router';
import { By } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { Subject, ReplaySubject } from 'rxjs';
import { S4eConfig } from 'src/app/utils/initializer/config.service';

class ActivatedRouteStub {
  queryParamMap: Subject<ParamMap> = new ReplaySubject(1);

  constructor() {
    this.queryParamMap.next(convertToParamMap({}));
  }
}

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let de: DebugElement;
  let router: Router;
  let spy: any;
  let activatedRoute: ActivatedRouteStub;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        CommonModule,
        RouterTestingModule.withRoutes([]),
        DashboardModule,
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        {provide: ActivatedRoute, useClass: ActivatedRouteStub},
        S4eConfig
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;
    router = TestBed.get(Router);
    activatedRoute = <ActivatedRouteStub>TestBed.get(ActivatedRoute);
    spy = spyOn(router, 'navigate').and.stub();

    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should enable buttons on known institution', fakeAsync( () => {
    activatedRoute.queryParamMap.next(convertToParamMap({ institution: 'test '}));
    tick();
    fixture.detectChanges();

    const tiles = de.queryAll(By.directive(TileComponent));
    for (let tile of tiles) {
      const groupsBtn = tile.query(By.css('.panel__footer .button'));
      if (groupsBtn) {
        expect(groupsBtn.nativeElement.disabled).toBeFalsy();
      }
    }
  }));

  it('should disable buttons when institution param is not known', fakeAsync( () => {
    activatedRoute.queryParamMap.next(convertToParamMap({}));
    tick();
    fixture.detectChanges();

    const tiles = de.queryAll(By.directive(TileComponent));
    for (let tile of tiles) {
      const groupsBtn = tile.query(By.css('.panel__footer .button'));
      if (groupsBtn) {
        expect(groupsBtn.nativeElement.disabled).toBeTruthy();
      }
    }
  }));
});
