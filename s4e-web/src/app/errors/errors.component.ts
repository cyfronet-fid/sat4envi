import {untilDestroyed} from 'ngx-take-until-destroy';
import {ActivatedRoute} from '@angular/router';
import {Component, OnDestroy, OnInit} from '@angular/core';
import {HTTP_404_NOT_FOUND, HTTP_500_INTERNAL_SERVER_ERROR, HTTP_502_BAD_GATEWAY} from './errors.model';
import { BACK_LINK_QUERY_PARAM } from '../state/session/session.service';

@Component({
  selector: 's4e-errors',
  templateUrl: './errors.component.html',
  styleUrls: ['./errors.component.scss']
})
export class ErrorsComponent implements OnInit, OnDestroy {
  public actualError = ErrorsComponent._getErrorBy(null);
  public backLink?: string;

  constructor(private _activatedRoute: ActivatedRoute) {
  }

  protected static _getErrorBy(code: number | null) {
    switch (code) {
      case HTTP_404_NOT_FOUND:
        return {
          title: 'Błąd 404: nie znaleziono poszukiwanej strony',
          description: 'Strona, której szukasz nie istnieje, bądź została przeniesiona'
        };
      case HTTP_502_BAD_GATEWAY:
        return {
          title: 'Błąd 502: brak odpowiedzi serwera lub błędna odpowiedź',
          description: 'Sprawdź swoje ustawienia przegladarki lub skontaktuj się z administratorem'
        };
      case HTTP_500_INTERNAL_SERVER_ERROR:
        return {
          title: 'Błąd 500: błąd po stronie serwera',
          description: 'Skontaktuj się z administratorem w przypadku powtarzającej się awarii'
        };
      case null:
      default:
        return {
          title: 'Wystąpił nieznany błąd',
          description: 'Skontaktuj się z administratorem w przypadku powtarzającej się awarii'
        };
    }
  }

  ngOnInit() {
    this._activatedRoute.paramMap
      .pipe(untilDestroyed(this))
      .subscribe((params) => {
        const actualErrorCode = +params.get('errorCode');
        this.actualError = ErrorsComponent._getErrorBy(actualErrorCode);
      });

    this._activatedRoute.queryParamMap
      .pipe(untilDestroyed(this))
      .subscribe((params) => {
        this.backLink = params.get(BACK_LINK_QUERY_PARAM);
      });
  }

  ngOnDestroy() {
  }
}
