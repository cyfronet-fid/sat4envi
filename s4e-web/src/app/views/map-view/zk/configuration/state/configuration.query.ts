import { Injectable } from '@angular/core';
import { QueryEntity } from '@datorama/akita';
import { ConfigurationStore } from './configuration.store';
import {Configuration, ConfigurationState} from './configuration.model';

@Injectable({
  providedIn: 'root'
})
export class ConfigurationQuery extends QueryEntity<ConfigurationState, Configuration> {

  constructor(protected store: ConfigurationStore) {
    super(store);
  }

}
