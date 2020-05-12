import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ActivateQuery} from './state/activate.query';
import {Observable} from 'rxjs';
import {ActivateService} from './state/activate.service';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {pluck} from 'rxjs/operators';
import {State} from './state/activate.model';

@Component({
  selector: 's4e-activate',
  templateUrl: './activate.component.html',
  styleUrls: ['./activate.component.scss']
})
export class ActivateComponent implements OnInit, OnDestroy {
  public error$: Observable<any>;
  public loading$: Observable<boolean>;
  private token: string;
  private state$: Observable<State>;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private query: ActivateQuery,
              private service: ActivateService) {
  }

  ngOnInit() {
    this.error$ = this.query.selectError();
    this.loading$ = this.query.selectLoading();
    this.state$ = this.query.select(state => state.state);

    this.route.params
      .pipe(untilDestroyed(this), pluck('token'))
      .subscribe(token => {
        this.token = token;
        this.service.activate(token);
      });
  }

  ngOnDestroy(): void {
  }

  resendToken() {
    this.service.resendToken(this.token);
  }
}
