import { PeopleModule } from './../people.module';
import { RouterTestingModule } from '@angular/router/testing';
import { ComponentFixture, TestBed, async, fakeAsync, tick } from '@angular/core/testing';
import { ActivatedRoute, ParamMap, convertToParamMap } from '@angular/router';
import { InstitutionsSearchResultsQuery } from '../../state/institutions-search/institutions-search-results.query';
import { InvitationFormComponent } from './invitation-form.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MODAL_DEF } from 'src/app/modal/modal.providers';
import { INVITATION_FORM_MODAL_ID, InvitationFormModal } from './invitation-form-modal.model';
import { Subject, ReplaySubject, of } from 'rxjs';
import { InvitationService } from '../state/invitation/invitation.service';

class ActivatedRouteStub {
  queryParamMap: Subject<ParamMap>;

  constructor() {
    this.queryParamMap = new ReplaySubject(1);
    this.queryParamMap.next(convertToParamMap({}));
  }
}

describe('InvitationFormComponent', () => {
  let component: InvitationFormComponent;
  let fixture: ComponentFixture<InvitationFormComponent>;
  let invitationService: InvitationService;
  let route: ActivatedRouteStub;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        PeopleModule,
        HttpClientTestingModule,
        RouterTestingModule,
      ],
      providers: [
        { provide: ActivatedRoute, useClass: ActivatedRouteStub },
        {
          provide: MODAL_DEF,
          useValue: {
              id: INVITATION_FORM_MODAL_ID,
              size: 'lg',
              institution: {name: 'test', slug: 'test'}
            } as InvitationFormModal
        }
      ]
    });
    invitationService = TestBed.get(InvitationService);
    fixture = TestBed.createComponent(InvitationFormComponent);
    component = fixture.componentInstance;
    route = <ActivatedRouteStub>TestBed.get(ActivatedRoute);
  }));

  it('can load instance', () => {
    expect(component).toBeTruthy();
  });
  it('should not call invitation create', () => {
    const name = 'test';
    const slug = 'test';
    component.institution = {name, slug} as any;

    fixture.detectChanges();

    spyOn(invitationService, 'send').and.callThrough();
    component.send();
    expect(invitationService.send).not.toHaveBeenCalled();
  });
  it('should call invitation send', () => {
    const name = 'test';
    const slug = 'test';
    component.institution = {name, slug} as any;

    fixture.detectChanges();

    const email = 'test@mail.pl';
    component.form.patchValue({email});

    spyOn(invitationService, 'send').and.callThrough();
    component.send();
    expect(invitationService.send).toHaveBeenCalledWith(slug, email, false);
  });
  it('should call invitation resend', () => {
    const name = 'test';
    const slug = 'test';
    component.institution = {name, slug} as any;

    const email = 'test@mail.pl';
    const status = 'waiting';
    component.invitation = {email, status} as any;

    fixture.detectChanges();
    component.form.patchValue({email});

    spyOn(invitationService, 'resend').and.callThrough();
    component.send();
    expect(invitationService.resend).toHaveBeenCalledWith({forAdmin: false, newEmail: email, oldEmail: email}, component.institution);
  });
});
