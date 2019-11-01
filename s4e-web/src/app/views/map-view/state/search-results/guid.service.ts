import {guid} from '@datorama/akita';
import {Injectable} from '@angular/core';

@Injectable({providedIn: 'root'})
export class AkitaGuidService {
  guid(): string {
    return guid();
  }
}
