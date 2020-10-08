import { Injectable } from '@angular/core';
import { QueryEntity } from '@datorama/akita';
import { InstitutionStore, InstitutionState } from './institution.store';
import { Institution } from './institution.model';
import { map } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { SessionQuery } from 'src/app/state/session/session.query';

@Injectable({
  providedIn: 'root'
})
export class InstitutionQuery extends QueryEntity<InstitutionState, Institution> {
  public getAdministrationInstitutions$(): Observable<Institution[]> {
    return this.selectAll()
    .pipe(map(institutions => {
      const administratorInstitutionsSlugs = this._sessionQuery.getAdministratorInstitutionsSlugs();
      return institutions
        .filter(institution => administratorInstitutionsSlugs.some(slug => slug === institution.slug));
    }));
  }
  public getMemberInstitutions$(): Observable<Institution[]> {
    return this.selectAll()
    .pipe(map(institutions => {
      const memberInstitutionSlugs = this._sessionQuery.getMemberInstitutionSlugs();
      return institutions
        .filter(institution => memberInstitutionSlugs.some(slug => slug === institution.slug));
    }));
  }
  public isManagerOf$(institution$: Observable<Institution>): Observable<boolean> {
    return institution$
      .pipe(map(institution => this.isManagerOf(institution)));
  }
  public isManagerOf(institution: Institution): boolean {
    const administratorInstitutionsSlugs = this._sessionQuery.getAdministratorInstitutionsSlugs();
    return administratorInstitutionsSlugs.some(slug => slug === institution.slug);
  }

  constructor(
    protected store: InstitutionStore,
    private _sessionQuery: SessionQuery
  ) {
    super(store);
  }
}
