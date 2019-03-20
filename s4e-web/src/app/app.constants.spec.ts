import {Provider} from '@angular/core';
import {S4E_CONSTANTS, s4eConstantsFactory} from './app.constants';

export const TestingConstantsProvider: Provider = {provide: S4E_CONSTANTS, useValue: s4eConstantsFactory()};
