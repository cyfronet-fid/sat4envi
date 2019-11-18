import {Component, OnInit} from '@angular/core';
import {GenericFormComponent} from '../../../../utils/miscellaneous/generic-form.component';
import {PersonQuery} from '../../people/state/person.query';
import {PersonForm} from '../../people/state/person.model';
import {FormControl, FormGroup} from '@ng-stack/forms';
import {combineLatest, Observable} from 'rxjs';
import {CheckBoxEntry} from '../../../../utils/s4e-forms/check-box-list/check-box-list.model';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../../../state/form/form.model';
import {InstitutionService} from '../../state/institution.service';
import {InstitutionQuery} from '../../state/institution.query';
import {PersonService} from '../../people/state/person.service';
import {GroupService} from '../state/group.service';
import {GroupQuery} from '../state/group.query';
import {ActivatedRoute, Router} from '@angular/router';
import {distinctUntilChanged, filter, map} from 'rxjs/operators';
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
  instSlug: string | null = null;
  groupSlug: string | null = null;

  constructor(fm: AkitaNgFormsManager<FormState>,
              private institutionService: InstitutionService,
              private institutionQuery: InstitutionQuery,
              groupQuery: GroupQuery,
              private personService: PersonService,
              private personQuery: PersonQuery,
              private groupService: GroupService,
              router: Router, private route: ActivatedRoute) {
    super(fm, router, groupQuery, 'addGroup');
  }

  ngOnInit() {
    this.form = new FormGroup<GroupForm>({
      name: new FormControl<string>(),
      membersEmails: new FormControl({value: {_: []}, disabled: false}),
      description: new FormControl()
    });

    (this.form.get('membersEmails').valueChanges as Observable<any>)
      .pipe(untilDestroyed(this),
        filter(() => this.groupSlug != null),
        distinctUntilChanged()
      ).subscribe(a => console.log(a));

    this.users$ = this.personQuery.selectAll().pipe(map(ppl => ppl.map(p => ({
      value: p.email,
      caption: `${p.surname} ${p.name} <${p.email}>`
    }))));

    this.institutionService.connectIntitutionToQuery$(this.route).pipe(untilDestroyed(this)).subscribe(
      instSlug => {
        this.personService.fetchAll(instSlug);
        this.instSlug = instSlug;
      }
    );

    combineLatest([
      this.route.paramMap,
      this.institutionQuery.selectActiveId().pipe(filter(i => i != null))
    ]).pipe(
      map(([pm, inst]) => [pm.get('groupSlug'), inst]),
      filter(([groupSlug, inst]) => groupSlug != null && groupSlug != 'add'),
      untilDestroyed(this)
    ).subscribe(([groupSlug, inst]) => {
      this.groupSlug = groupSlug;
      this.groupService.fetchForm$(inst, groupSlug).subscribe(fd => this.form.setValue(fd));
    });

    super.ngOnInit();
  }

  submit() {
    if (this.groupSlug == null) {
      this.groupService.create$(this.instSlug, this.form.value)
        .subscribe(() => this.router.navigate(['..'], {relativeTo: this.route, queryParamsHandling: 'preserve'}));
    } else {
      this.groupService.update$(this.instSlug, this.groupSlug, this.form.value)
        .subscribe(() => this.router.navigate(['..'], {relativeTo: this.route, queryParamsHandling: 'preserve'}));
    }
  }
}
