import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { PersonService } from './person.service';
import { PersonStore } from './person.store';

describe('PersonService', () => {
  let personService: PersonService;
  let personStore: PersonStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PersonService, PersonStore],
      imports: [ HttpClientTestingModule ]
    });

    personService = TestBed.get(PersonService);
    personStore = TestBed.get(PersonStore);
  });

  it('should be created', () => {
    expect(personService).toBeDefined();
  });

});
