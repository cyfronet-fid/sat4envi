import { Person } from './../state/person.model';
import { PERSON_FORM_MODAL_ID, isPersonFormModal, PersonFormModal } from './person-form-modal.model';
import { ModalQuery } from 'src/app/modal/state/modal.query';
import { ModalService } from 'src/app/modal/state/modal.service';
import {Component, Inject} from '@angular/core';
import {FormControl, FormGroup} from '@ng-stack/forms';
import {PersonForm} from '../state/person.model';
import {Observable} from 'rxjs';
import {InstitutionService} from '../../state/institution/institution.service';
import {InstitutionQuery} from '../../state/institution/institution.query';
import {ActivatedRoute, Router} from '@angular/router';
import {GroupService} from '../../groups/state/group.service';
import {GroupQuery} from '../../groups/state/group.query';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {map} from 'rxjs/operators';
import {PersonQuery} from '../state/person.query';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../../../state/form/form.model';
import {CheckBoxEntry} from '../../../../form/check-box-list/check-box-list.model';
import {PersonService} from '../state/person.service';
import { FormModalComponent } from 'src/app/modal/utils/modal/modal.component';
import { assertModalType } from 'src/app/modal/utils/modal/misc';
import { MODAL_DEF } from 'src/app/modal/modal.providers';

@Component({
  selector: 's4e-person-form',
  templateUrl: './person-form.component.html',
  styleUrls: ['./person-form.component.scss']
})
export class PersonFormComponent extends FormModalComponent<'addPerson'> {
  form: FormGroup<PersonForm>;
  groups$: Observable<CheckBoxEntry<string>[]>;
  groupsLoading$: Observable<boolean>;
  institutionSlug: string|null = null;
  person: Person | null;
  modalId = PERSON_FORM_MODAL_ID;

  constructor(
    fm: AkitaNgFormsManager<FormState>,
    private institutionService: InstitutionService,
    private institutionQuery: InstitutionQuery,
    private personQuery: PersonQuery,
    private personService: PersonService,
    private groupService: GroupService,
    private groupQuery: GroupQuery,
    private router: Router, private route: ActivatedRoute,
    private _modalService: ModalService,
    private _modalQuery: ModalQuery,
    @Inject(MODAL_DEF) modal: PersonFormModal
  ) {
    super(fm, _modalService, _modalQuery, PERSON_FORM_MODAL_ID, 'addPerson');
    assertModalType(isPersonFormModal, modal);

    this.person = modal.person;
  }

  makeForm(): FormGroup<FormState['addPerson']> {
    return new FormGroup<PersonForm>({
      email: new FormControl<string>({
        value: !!this.person && this.person.email || null,
        disabled: !!this.person
      }),
      name: new FormControl<string>({
        value: !!this.person && this.person.name || null,
        disabled: !!this.person
      }),
      surname: new FormControl<string>({
        value: !!this.person && this.person.surname || null,
        disabled: !!this.person
      }),
      groupSlugs: new FormControl<{_: string[]}>({
        value: {_: []},
        disabled: false
      }),
    });
  }

  ngOnInit() {
    this.groups$ = this.groupQuery.selectAllWithoutDefault()
      .pipe(
        map(groups => groups.map(gr => ({value: gr.slug, caption: gr.name})))
      );
    this.groupsLoading$ = this.groupQuery.selectLoading();

    this.institutionService.connectInstitutionToQuery$(this.route)
      .pipe(untilDestroyed(this))
      .subscribe(
        institutionSlug => {
          this.groupService.fetchAll(institutionSlug);
          this.institutionSlug = institutionSlug;
        }
      );

    super.ngOnInit();

    if (!!this.person) {
      this.form.patchValue(this.person);
    }
  }

  save() {
    (
      !!this.person
        ? this.personService.update$(this.institutionSlug, {...this.form.value, ...this.person})
        : this.personService.create$(this.institutionSlug, this.form.value)
    )
      .pipe(untilDestroyed(this))
      .subscribe(() => this.dismiss());
  }
}
