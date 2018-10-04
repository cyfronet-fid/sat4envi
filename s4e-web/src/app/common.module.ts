import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {TranslateModule} from '@ngx-translate/core';
import {HttpClientModule} from '@angular/common/http';

@NgModule({
  exports: [
    BrowserModule,
    HttpClientModule,
    NgbModule,
    TranslateModule,
    HttpClientModule,
  ]
})
export class CommonModule { }
