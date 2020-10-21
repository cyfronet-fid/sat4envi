import { NotificationService } from 'notifications';
import { ExpertHelpService } from './../state/expert-help.service';
import { EXPERT_HELP_MODAL_ID } from './expert-help-modal.model';
import { RouterTestingModule } from '@angular/router/testing';
import { MapModule } from './../../map.module';
import { async, ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';

import { ExpertHelpModalComponent } from './expert-help-modal.component';
import { MODAL_DEF } from 'src/app/modal/modal.providers';
import { of } from 'rxjs';

describe('ExpertHelpModalComponent', () => {
  let component: ExpertHelpModalComponent;
  let fixture: ComponentFixture<ExpertHelpModalComponent>;

  let expertHelpService: ExpertHelpService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        MapModule,
        RouterTestingModule
      ],
      providers: [
        {
          provide: MODAL_DEF, useValue: {
            id: EXPERT_HELP_MODAL_ID,
            size: 'lg'
          }
        }
      ]
    })
    .compileComponents();

    expertHelpService = TestBed.get(ExpertHelpService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExpertHelpModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should valid form', () => {
    const spyExpertHelpService = spyOn(expertHelpService, 'sendHelpRequest$')
      .and.returnValue(of());

    component.form.setValue({helpType: 'REMOTE', issueDescription: null});
    component.sendIssue$();

    expect(spyExpertHelpService).not.toHaveBeenCalled();

    component.form.setValue({helpType: null, issueDescription: 'test'});
    component.sendIssue$();

    expect(spyExpertHelpService).not.toHaveBeenCalled();

    component.form.setValue({helpType: 'test', issueDescription: null});
    component.sendIssue$();

    expect(spyExpertHelpService).not.toHaveBeenCalled();
  });
});
