import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {filter, pluck} from 'rxjs/operators';

@Component({
  selector: 's4e-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements OnInit, OnDestroy {

  constructor(private activatedRoute: ActivatedRoute,
              private router: Router) { }

  ngOnInit() {
    this.activatedRoute.queryParams.pipe(
      pluck('token'),
      untilDestroyed(this)
    ).subscribe(token => {
      if (token == null) {
        this.router.navigate(['/'], {replaceUrl: true});
      }

      // :TODO - handle password reset
    });
  }

  ngOnDestroy(): void {}
}
