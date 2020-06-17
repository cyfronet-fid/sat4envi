import { GroupsModule } from './../groups.module';
import { By } from '@angular/platform-browser';
import { GROUP_FORM_MODAL_ID, GroupFormModal } from './group-form-modal.model';
import { PersonService } from './../../people/state/person.service';
import { GroupService } from './../state/group.service';
import { InstitutionService } from './../../state/institution/institution.service';
import { async, ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import {GroupFormComponent} from './group-form.component';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {TestingConfigProvider} from '../../../../app.configuration.spec';
import {SettingsModule} from '../../settings.module';
import { Subject, ReplaySubject, of } from 'rxjs';
import { ParamMap, convertToParamMap, ActivatedRoute } from '@angular/router';
import { Group } from '../state/group.model';
import { createModal } from 'src/app/modal/state/modal.model';
import { MODAL_DEF } from 'src/app/modal/modal.providers';
import { DebugElement } from '@angular/core';

class ActivatedRouteStub {
  queryParamMap: Subject<ParamMap> = new ReplaySubject(1);

  constructor() {
    this.queryParamMap.next(convertToParamMap({}));
  }
}

describe('GroupFormComponent', () => {
  let component: GroupFormComponent;
  let fixture: ComponentFixture<GroupFormComponent>;
  let route: ActivatedRouteStub;
  let institutionService: InstitutionService;
  let groupService: GroupService;
  let personService: PersonService;
  let de: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        GroupsModule,
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        TestingConfigProvider,
        {provide: ActivatedRoute, useClass: ActivatedRouteStub},
        {provide: MODAL_DEF, useValue: createModal({id: GROUP_FORM_MODAL_ID, size: 'lg'} as GroupFormModal)}
      ]
    })
      .compileComponents();

    groupService = TestBed.get(GroupService);
    institutionService = TestBed.get(InstitutionService);
    personService = TestBed.get(PersonService);
    route = <ActivatedRouteStub>TestBed.get(ActivatedRoute);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GroupFormComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });
  it('should save group', fakeAsync(() => {
    const institutionSlug = 'institution#1';
    spyOn(institutionService, 'connectInstitutionToQuery$').and.returnValue(of(institutionSlug));
    spyOn(personService, 'fetchAll').and.returnValue(of({}));

    const spyCreation = spyOn(groupService, 'create$').and.returnValue(of(1));

    tick();
    fixture.detectChanges();

    const group = {
      id: 'test#1',
      name: 'Test #1',
      slug: 'New test #1'
    } as Group;
    component.form.patchValue(group);
    fixture.detectChanges();

    const submitBtn = de.query(By.css('button[data-test="submit"]'));
    submitBtn.nativeElement.click();
    tick();

    expect(spyCreation).toHaveBeenCalledWith(institutionSlug, component.form.value);
  }));
  it('should update group', fakeAsync(() => {
    const institutionSlug = 'institution#1';
    spyOn(institutionService, 'connectInstitutionToQuery$').and.returnValue(of(institutionSlug));
    spyOn(personService, 'fetchAll').and.returnValue(of({}));

    const spyUpdate = spyOn(groupService, 'update$').and.returnValue(of(1));

    const group = {
      id: 'test#1',
      name: 'Test #1',
      slug: 'New test #1'
    } as Group;
    component.group = group;

    tick();
    fixture.detectChanges();

    const submitBtn = de.query(By.css('button[data-test="submit"]'));
    submitBtn.nativeElement.click();
    tick();

    expect(spyUpdate).toHaveBeenCalledWith(institutionSlug, group.slug, component.form.value);
  }));
});
