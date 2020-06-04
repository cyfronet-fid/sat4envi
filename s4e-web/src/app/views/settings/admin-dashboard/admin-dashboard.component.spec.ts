import { SearchComponent } from './../../../components/search/search.component';
import { By } from '@angular/platform-browser';
import { of, Subject, ReplaySubject } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AdminDashboardModule } from './admin-dashboard.module';
import { async, ComponentFixture, TestBed, tick, fakeAsync } from '@angular/core/testing';

import { AdminDashboardComponent } from './admin-dashboard.component';
import {RouterTestingModule} from '@angular/router/testing';
import { DebugElement } from '@angular/core';
import { Router, ActivatedRoute, convertToParamMap, ParamMap } from '@angular/router';
import { CommonModule } from '@angular/common';
import { S4eConfig } from 'src/app/utils/initializer/config.service';
import { Institution } from '../state/institution/institution.model';
import { TileComponent } from 'src/app/components/tiles-dashboard/tile/tile.component';

class ActivatedRouteStub {
  queryParamMap: Subject<ParamMap> = new ReplaySubject(1);

  constructor() {
    this.queryParamMap.next(convertToParamMap({}));
  }
}

describe('AdminDashboardComponent', () => {
  let component: AdminDashboardComponent;
  let fixture: ComponentFixture<AdminDashboardComponent>;
  let de: DebugElement;
  const router = {
    navigate: jasmine.createSpy('navigate')
  };
  let activatedRoute: ActivatedRouteStub;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        CommonModule,
        RouterTestingModule.withRoutes([]),
        AdminDashboardModule,
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        S4eConfig,
        {provide: ActivatedRoute, useClass: ActivatedRouteStub},
        {provide: Router, useValue: router}
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminDashboardComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;
    activatedRoute = <ActivatedRouteStub>TestBed.get(ActivatedRoute);

    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should add institution query param to url on institution selection and enable buttons', () => {
    const institution = {
      name: 'test#2',
      slug: 'test-2'
    } as Institution;
    component.selectInstitution(institution);
    fixture.detectChanges();
    fixture.whenStable()
      .then(() => {
        expect(router.navigate).toHaveBeenCalled();

        const groupsBtn = de.query(By.css('.panel__footer .button')).nativeElement;
        expect(groupsBtn.disabled).toBeFalsy();
      });
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
