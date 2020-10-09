import { SessionQuery } from 'src/app/state/session/session.query';
import { untilDestroyed } from 'ngx-take-until-destroy';
import { Component, OnInit, OnDestroy } from '@angular/core';
import {Observable} from 'rxjs';
import {Institution} from '../../state/institution/institution.model';
import {InstitutionQuery} from '../../state/institution/institution.query';
import {InstitutionService} from '../../state/institution/institution.service';
import {ModalService} from '../../../../modal/state/modal.service';

@Component({
  selector: 's4e-institution-list',
  templateUrl: './institution-list.component.html',
  styleUrls: ['./institution-list.component.scss']
})
export class InstitutionListComponent implements OnInit, OnDestroy {
  isAdmin = false;

  isLoading$ = this._institutionQuery.selectLoading();
  institutions$ = this._institutionQuery.selectAll();
  error$ = this._institutionQuery.selectError();

  institutionId = (item: Institution) => item.slug;

  constructor(
    private _institutionQuery: InstitutionQuery,
    private _institutionService: InstitutionService,
    private _sessionQuery: SessionQuery,
    private _modalService: ModalService
  ) {}

  ngOnInit() {
    this.isAdmin = this._sessionQuery.isAdmin();
  }

  isManagerOf(institution: Institution) {
    return this._institutionQuery.isManagerOf(institution);
  }

  async deleteInstitution(slug: string) {
    if(await this._modalService.confirm('Usuń instytucję',
      'Czy na pewno chcesz usunąć tą instytucję? Operacja jest nieodwracalna.')) {
      this._institutionService.delete(slug).pipe(untilDestroyed(this)).subscribe();
    }
  }

  ngOnDestroy() {}
}
