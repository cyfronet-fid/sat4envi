import { Component, OnInit } from '@angular/core';
import {GenericFormComponent} from '../../../../utils/miscellaneous/generic-form.component';
import {PersonQuery} from '../../people/state/person.query';
import {PersonForm} from '../../people/state/person.model';
import {FormControl, FormGroup} from '@ng-stack/forms';
import {Observable} from 'rxjs';
import {CheckBoxEntry} from '../../../../utils/s4e-forms/check-box-list/check-box-list.model';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../../../state/form/form.model';
import {InstitutionService} from '../../state/institution.service';
import {InstitutionQuery} from '../../state/institution.query';
import {PersonService} from '../../people/state/person.service';
import {GroupService} from '../state/group.service';
import {GroupQuery} from '../state/group.query';
import {ActivatedRoute, Router} from '@angular/router';
import {map} from 'rxjs/operators';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {GroupForm} from '../state/group.model';

@Component({
  selector: 's4e-group-form',
  templateUrl: './group-form.component.html',
  styleUrls: ['./group-form.component.scss']
})
export class GroupFormComponent extends GenericFormComponent<GroupQuery, GroupForm> {
  form: FormGroup<GroupForm>;
  users$: Observable<CheckBoxEntry<string>[]>;
  groupsLoading$: Observable<boolean>;
  instSlug: string|null = null;

  constructor(fm: AkitaNgFormsManager<FormState>,
              private institutionService: InstitutionService,
              private institutionQuery: InstitutionQuery,
              groupQuery: GroupQuery,
              private personService: PersonService,
              private personQuery: PersonQuery,
              private groupService: GroupService,
              router: Router, private route: ActivatedRoute) {
    super(fm, router, groupQuery, 'addGroup')
  }

  ngOnInit() {
    this.form = new FormGroup<GroupForm>({
      name: new FormControl<string>(),
      users: new FormControl({value: {_: []}, disabled: false}),
      description: new FormControl()
    });

    this.users$ = this.personQuery.selectAll().pipe(map(ppl => ppl.map(p => ({value: p.email, caption: `${p.surname} ${p.name} <${p.email}>`}))));

    this.institutionService.connectIntitutionToQuery$(this.route).pipe(untilDestroyed(this)).subscribe(
      instSlug => {
        this.personService.fetchAll(instSlug);
        this.instSlug = instSlug;
      }
    );

    super.ngOnInit();
  }

  submit() {
    this.groupService.create$(this.instSlug, this.form.value)
      .subscribe(() => this.router.navigate(['..'], {relativeTo: this.route, queryParamsHandling: 'preserve'}));
  }
}
