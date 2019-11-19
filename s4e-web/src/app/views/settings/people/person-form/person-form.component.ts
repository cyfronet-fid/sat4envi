import {Component} from '@angular/core';
import {FormControl, FormGroup} from '@ng-stack/forms';
import {PersonForm} from '../state/person.model';
import {Observable} from 'rxjs';
import {InstitutionService} from '../../state/institution.service';
import {InstitutionQuery} from '../../state/institution.query';
import {ActivatedRoute, Router} from '@angular/router';
import {GroupService} from '../../groups/state/group.service';
import {GroupQuery} from '../../groups/state/group.query';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {map} from 'rxjs/operators';
import {GenericFormComponent} from '../../../../utils/miscellaneous/generic-form.component';
import {PersonQuery} from '../state/person.query';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../../../state/form/form.model';
import {CheckBoxEntry} from '../../../../utils/s4e-forms/check-box-list/check-box-list.model';
import {PersonService} from '../state/person.service';

@Component({
  selector: 's4e-person-form',
  templateUrl: './person-form.component.html',
  styleUrls: ['./person-form.component.scss']
})
export class PersonFormComponent extends GenericFormComponent<PersonQuery, PersonForm> {
  form: FormGroup<PersonForm>;
  groups$: Observable<CheckBoxEntry<string>[]>;
  groupsLoading$: Observable<boolean>;
  instSlug: string|null = null;

  constructor(fm: AkitaNgFormsManager<FormState>,
              private institutionService: InstitutionService,
              private institutionQuery: InstitutionQuery,
              personQuery: PersonQuery,
              private personService: PersonService,
              private groupService: GroupService,
              private groupQuery: GroupQuery,
              router: Router, private route: ActivatedRoute) {
    super(fm, router, personQuery, 'addPerson')
  }

  ngOnInit() {
    this.form = new FormGroup<PersonForm>({
      email: new FormControl<string>(),
      name: new FormControl<string>(),
      surname: new FormControl<string>(),
      groupSlugs: new FormControl<{_: string[]}>({value: {_: []}, disabled: false}),
    });

    this.groups$ = this.groupQuery.selectAllWithoutDefault().pipe(map(groups => groups.map(gr => ({value: gr.slug, caption: gr.name}))));
    this.groupsLoading$ = this.groupQuery.selectLoading();

    this.institutionService.connectIntitutionToQuery$(this.route).pipe(untilDestroyed(this)).subscribe(
      instSlug => {
        this.groupService.fetchAll(instSlug);
        this.instSlug = instSlug;
      }
    );

    super.ngOnInit();
  }

  saveAndNext() {
    console.log(this.form.value);
    this.personService.create$(this.instSlug, this.form.value).subscribe(() => this.form.reset());
  }

  saveAndBack() {
    console.log(this.form.value);
    this.personService.create$(this.instSlug, this.form.value).subscribe(() =>
      this.router.navigate(['..'], {relativeTo: this.route, queryParamsHandling: 'preserve'})
    );
  }
}
