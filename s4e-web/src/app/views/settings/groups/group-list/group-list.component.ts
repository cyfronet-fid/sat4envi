import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Institution} from '../../state/institution.model';
import {InstitutionQuery} from '../../state/institution.query';
import {InstitutionService} from '../../state/institution.service';
import {ActivatedRoute, Router} from '@angular/router';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {GroupQuery} from '../state/group.query';
import {GroupService} from '../state/group.service';
import {Group} from '../state/group.model';

@Component({
  selector: 's4e-group-list',
  templateUrl: './group-list.component.html',
  styleUrls: ['./group-list.component.scss']
})
export class GroupListComponent implements OnInit, OnDestroy {
  error$: Observable<any>;
  groups$: Observable<Group[]>;
  loading$: Observable<boolean>;
  institutions$: Observable<Institution[]>;
  institutionsLoading$: Observable<boolean>;

  constructor(private groupQuery: GroupQuery, private groupService: GroupService,
              private institutionQuery: InstitutionQuery, private institutionService: InstitutionService,
              private router: Router, private route: ActivatedRoute) { }

  ngOnInit() {
    this.error$ = this.groupQuery.selectError();
    this.loading$ = this.groupQuery.selectLoading();
    this.groups$ = this.groupQuery.selectAllWithoutDefault();
    this.institutions$ = this.institutionQuery.selectAll();
    this.institutionsLoading$ = this.institutionQuery.selectLoading();

    this.institutionService.connectIntitutionToQuery$(this.route).pipe(untilDestroyed(this)).subscribe(
      instSlug => this.groupService.fetchAll(instSlug)
    );
  }

  ngOnDestroy(): void {
  }

  setInstitution(institutionSlug: string) {
    this.router.navigate([], {queryParamsHandling: 'merge', queryParams: {institution: institutionSlug}})
  }
}