import { Modal } from './../../../../modal/state/modal.model';
import { GROUP_FORM_MODAL_ID, isGroupFormModal, GroupFormModal } from './group-form-modal.model';
import { ModalQuery } from 'src/app/modal/state/modal.query';
import { ModalService } from 'src/app/modal/state/modal.service';
import { FormModalComponent } from 'src/app/modal/utils/modal/modal.component';
import { Component, Inject } from '@angular/core';
import {GenericFormComponent} from '../../../../utils/miscellaneous/generic-form.component';
import {PersonQuery} from '../../people/state/person.query';
import {FormControl, FormGroup} from '@ng-stack/forms';
import {Observable} from 'rxjs';
import {CheckBoxEntry} from '../../../../form/check-box-list/check-box-list.model';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../../../state/form/form.model';
import {InstitutionService} from '../../state/institution/institution.service';
import {InstitutionQuery} from '../../state/institution/institution.query';
import {PersonService} from '../../people/state/person.service';
import {GroupService} from '../state/group.service';
import {GroupQuery} from '../state/group.query';
import {ActivatedRoute, Router} from '@angular/router';
import { distinctUntilChanged, filter, map, finalize } from 'rxjs/operators';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {GroupForm, Group} from '../state/group.model';
import { MODAL_DEF } from 'src/app/modal/modal.providers';
import { assertModalType } from 'src/app/modal/utils/modal/misc';

@Component({
  selector: 's4e-group-form',
  templateUrl: './group-form.component.html',
  styleUrls: ['./group-form.component.scss']
})
export class GroupFormComponent extends FormModalComponent<'addGroup'> {
  form: FormGroup<GroupForm>;
  users$: Observable<CheckBoxEntry<string>[]>;
  groupsLoading$: Observable<boolean>;
  instSlug: string | null = null;
  group: Group | null = null;
  modalId = GROUP_FORM_MODAL_ID;

  constructor(
    fm: AkitaNgFormsManager<FormState>,
    private institutionService: InstitutionService,
    private institutionQuery: InstitutionQuery,
    groupQuery: GroupQuery,
    private personService: PersonService,
    private personQuery: PersonQuery,
    private groupService: GroupService,
    private router: Router,
    private route: ActivatedRoute,
    private _modalService: ModalService,
    private _modalQuery: ModalQuery,
    @Inject(MODAL_DEF) modal: GroupFormModal
  ) {
    super(fm, _modalService, _modalQuery, GROUP_FORM_MODAL_ID, 'addGroup');
    assertModalType(isGroupFormModal, modal);

    this.group = modal.group;
  }

  makeForm(): FormGroup<FormState['addGroup']> {
    return new FormGroup<GroupForm>({
      name: new FormControl<string>(),
      membersEmails: new FormControl({value: {_: []}, disabled: false}),
      description: new FormControl()
    });
  }

  ngOnInit() {
    this.users$ = this.personQuery.selectAll()
      .pipe(map(ppl => ppl.map(p => ({
        value: p.email,
        caption: `${p.surname} ${p.name} <${p.email}>`
      }))));

    this.institutionService.connectInstitutionToQuery$(this.route)
      .pipe(untilDestroyed(this))
      .subscribe(
        instSlug => {
          this.personService.fetchAll(instSlug);
          this.instSlug = instSlug;
        }
      );

    super.ngOnInit();

    if (!!this.group) {
      this.form.patchValue(this.group);
      (this.form.get('membersEmails').valueChanges as Observable<any>)
        .pipe(
          untilDestroyed(this),
          distinctUntilChanged()
        )
        .subscribe();
      }
  }

  submit() {
    (
      !!this.group
        ? this.groupService.update$(this.instSlug, this.group.slug, this.form.value)
        : this.groupService.create$(this.instSlug, this.form.value)
    )
      .pipe(untilDestroyed(this))
      .subscribe(() => this.dismiss());
  }
}
