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
  isLoading$: Observable<boolean>;
  institutions$: Observable<Institution[]>;
  error$: Observable<any>;

  institutionId = (item: Institution) => item.slug;

  constructor(private _institutionQuery: InstitutionQuery,
              private _institutionService: InstitutionService,
              private _modalService: ModalService) {
  }

  ngOnInit() {
    this.isLoading$ = this._institutionQuery.selectLoading();
    this.institutions$ = this._institutionQuery.selectAll();
    this.error$ = this._institutionQuery.selectError();
  }

  async deleteInstitution(slug: string) {
    if(await this._modalService.confirm('Usuń instytucję',
      'Czy na pewno chcesz usunąć tą instytucję? Operacja jest nieodwracalna.')) {
      this._institutionService.delete(slug).pipe(untilDestroyed(this)).subscribe();
    }
  }

  ngOnDestroy() {}
}
