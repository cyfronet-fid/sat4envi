import { untilDestroyed } from 'ngx-take-until-destroy';
import { IBreadcrumb } from './breadcrumb.model';
import { filter } from 'rxjs/operators';
import { BreadcrumbService } from './breadcrumb.service';
import { ActivatedRoute, Router, NavigationEnd } from '@angular/router';
import { Component, OnInit, OnDestroy } from '@angular/core';

@Component({
  selector: 's4e-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.scss']
})
export class BreadcrumbComponent implements OnInit, OnDestroy {
  public breadcrumbs: IBreadcrumb[];

  constructor(
    private _router: Router,
    private _activatedRoute: ActivatedRoute,
    private _breadcrumbService: BreadcrumbService
  ) {}

  ngOnInit() {
    this._breadcrumbService.loadFrom(this._activatedRoute);

    this._router.events
      .pipe(
        untilDestroyed(this),
        filter(event => event instanceof NavigationEnd)
      )
      .subscribe(() => this._breadcrumbService.loadFrom(this._activatedRoute));
    this._breadcrumbService.breadcrumbs$
      .pipe(untilDestroyed(this))
      .subscribe((breadcrumbs) => this.breadcrumbs = breadcrumbs);
  }

  ngOnDestroy() {}
}
