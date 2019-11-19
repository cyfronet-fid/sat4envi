import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Person} from '../state/person.model';
import {PersonQuery} from '../state/person.query';
import {Institution} from '../../state/institution.model';
import {InstitutionQuery} from '../../state/institution.query';
import {PersonService} from '../state/person.service';
import {InstitutionService} from '../../state/institution.service';
import {ActivatedRoute, Router} from '@angular/router';
import {untilDestroyed} from 'ngx-take-until-destroy';

@Component({
  selector: 's4e-people',
  templateUrl: './person-list.component.html',
  styleUrls: ['./person-list.component.scss']
})
export class PersonListComponent implements OnInit, OnDestroy {
  error$: Observable<any>;
  users$: Observable<Person[]>;
  loading$: Observable<boolean>;
  institutions$: Observable<Institution[]>;
  institutionsLoading$: Observable<boolean>;

  constructor(private personQuery: PersonQuery, private personService: PersonService,
              private institutionQuery: InstitutionQuery, private institutionService: InstitutionService,
              private router: Router, private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.error$ = this.personQuery.selectError();
    this.loading$ = this.personQuery.selectLoading();
    this.users$ = this.personQuery.selectAll();
    this.institutions$ = this.institutionQuery.selectAll();
    this.institutionsLoading$ = this.institutionQuery.selectLoading();


    this.institutionService.connectIntitutionToQuery$(this.route).pipe(untilDestroyed(this)).subscribe(
      instSlug => this.personService.fetchAll(instSlug)
    );
  }

  ngOnDestroy(): void {
  }

  setInstitution(institutionSlug: string) {
    this.router.navigate([], {queryParamsHandling: 'merge', queryParams: {institution: institutionSlug}});
  }
}
