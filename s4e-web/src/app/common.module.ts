import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {TranslateModule} from '@ngx-translate/core';
import {HttpClientModule} from '@angular/common/http';
import {FormsModule} from '@angular/forms';

@NgModule({
  exports: [
    BrowserModule,
    HttpClientModule,
    NgbModule,
    TranslateModule,
    HttpClientModule,
    FormsModule,
  ]
})
export class CommonModule { }
