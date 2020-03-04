import {Component} from '@angular/core';
import {GenericFormComponent} from '../../../../utils/miscellaneous/generic-form.component';
import {PersonQuery} from '../../people/state/person.query';
import {FormControl, FormGroup} from '@ng-stack/forms';
import {Observable} from 'rxjs';
import {CheckBoxEntry} from '../../../../form/check-box-list/check-box-list.model';
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

    this.institutionService.connectInstitutionToQuery$(this.route).pipe(untilDestroyed(this)).subscribe(
      instSlug => {
        this.personService.fetchAll(instSlug);
        this.instSlug = instSlug;
      }
    );

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
