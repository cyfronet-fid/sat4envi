import {Injectable} from '@angular/core';
import {EntityStore, StoreConfig} from '@datorama/akita';
import {Configuration, ConfigurationState} from './configuration.model';


@Injectable({providedIn: 'root'})
@StoreConfig({name: 'Configuration'})
export class ConfigurationStore extends EntityStore<ConfigurationState, Configuration> {
  constructor() {
    super({
      loading: false
    });
  }
}

