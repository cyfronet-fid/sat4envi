import { InstitutionFactory } from './../state/institution/institution.factory.spec';
import { of } from 'rxjs';
import { InstitutionQuery } from './../state/institution/institution.query';
import { async, ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {SessionService} from '../../../state/session/session.service';
import {ProfileComponent} from './profile.component';
import {SessionQuery} from '../../../state/session/session.query';
import {SessionStore} from '../../../state/session/session.store';
import {ProfileModule} from './profile.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let sessionService: SessionService;
  let sessionQuery: SessionQuery;
  let sessionStore: SessionStore;
  let institutionQuery: InstitutionQuery;
  let de: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        ProfileModule,
        HttpClientTestingModule,
        RouterTestingModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;
    sessionService = TestBed.get(SessionService);
    sessionQuery = TestBed.get(SessionQuery);
    sessionStore = TestBed.get(SessionStore);

    institutionQuery = TestBed.get(InstitutionQuery);
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should display administrative and member institutions', () => {
    const institutions = InstitutionFactory.buildList(5);
    const administrativeInstitutions = institutions.splice(0, 3);
    component.administrationInstitutions$ = of(administrativeInstitutions);
    const memberInstitutions = institutions;
    component.memberInstitutions$ = of(memberInstitutions);

    fixture.detectChanges();

    const administrativeInstitutionsTile = de.query(By.css('s4e-tile[data-ut="administrative-institutions-tile"]'));
    const administrativeInstitutionsTileText = administrativeInstitutionsTile.nativeElement.textContent;
    administrativeInstitutions
      .forEach(institution => expect(administrativeInstitutionsTileText.indexOf(institution.name) > -1).toBeTruthy());

    const memberInstitutionsTile = de.query(By.css('s4e-tile[data-ut="member-institutions-tile"]'));
    const memberInstitutionsTileText = memberInstitutionsTile.nativeElement.textContent;
    memberInstitutions
        .forEach(institution => expect(memberInstitutionsTileText.indexOf(institution.name) > -1).toBeTruthy());
  });
  it('should display get more button on more than max institutions', () => {
    component.hasMoreAdministrationInstitutionsThanMax$ = of(true);

    fixture.detectChanges();

    const goToInstitutionsListBtn = de.queryAll(By.css('button[data-ut="go-to-institutions"]'));
    expect(goToInstitutionsListBtn).toBeTruthy();
  });
});
