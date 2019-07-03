import {ModuleWithProviders, NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {HttpClientModule} from '@angular/common/http';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {InjectorModule} from './injector.module';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TooltipModule } from 'ngx-bootstrap/tooltip';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import localePl from '@angular/common/locales/pl';
import {registerLocaleData} from '@angular/common';
import { library } from '@fortawesome/fontawesome-svg-core';
import {fas} from '@fortawesome/free-solid-svg-icons';
import {UtilsModule} from '../utils/utils.module';
import {FormErrorComponent} from '../components/form-error/form-error.component';

registerLocaleData(localePl, 'pl');
library.add(fas);

@NgModule({
  declarations: [
  ],
  imports: [
  ],
  exports: [
    BrowserModule,
    HttpClientModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    InjectorModule,
    FontAwesomeModule,
    UtilsModule
  ]
})
export class ShareModule {
  static modulesForRoot(): ModuleWithProviders[] {
    return [
      {
        ngModule: ShareModule,
        providers: []
      },
      TooltipModule.forRoot(),
      BsDropdownModule.forRoot()
    ];
  }
}
