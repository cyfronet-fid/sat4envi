import {ModuleWithProviders, NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {HttpClientModule} from '@angular/common/http';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {InjectorModule} from './injector.module';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TooltipModule } from 'ngx-bootstrap/tooltip';

@NgModule({
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
  ]
})
export class CommonModule {
  static modulesForRoot(): ModuleWithProviders[] {
    return [
      {
        ngModule: CommonModule,
        providers: []
      },
      TooltipModule.forRoot(),
      BsDropdownModule.forRoot()
    ];
  }
}
