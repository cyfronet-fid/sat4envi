/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {JWT_TOKEN_MODAL_ID} from './jwt-token-modal.model';
import {RouterTestingModule} from '@angular/router/testing';
import {MapModule} from './../map.module';
import {By} from '@angular/platform-browser';
import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {JwtTokenModalComponent} from './jwt-token-modal.component';
import {of} from 'rxjs';
import {MODAL_DEF} from 'src/app/modal/modal.providers';

describe('JwtTokenModalComponent', () => {
  let component: JwtTokenModalComponent;
  let fixture: ComponentFixture<JwtTokenModalComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [MapModule, RouterTestingModule],
        providers: [
          {
            provide: MODAL_DEF,
            useValue: {
              id: JWT_TOKEN_MODAL_ID,
              size: 'lg'
            }
          }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(JwtTokenModalComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should open window with auth form', () => {
    component.token = null;
    fixture.detectChanges();

    const passwordInput = fixture.debugElement.query(
      By.css('[data-ut="jwt-token-password-input"]')
    );
    expect(passwordInput).toBeTruthy();

    const getTokenBtn = fixture.debugElement.query(
      By.css('[data-ut="get-jwt-token-btn"]')
    );
    expect(getTokenBtn).toBeTruthy();

    const tokenTextarea = fixture.debugElement.query(
      By.css('[data-ut="jwt-token-txt"]')
    );
    expect(tokenTextarea).toBeFalsy();
  });
  it('should show jwt token', () => {
    const token = 'token';
    component.token = token;
    fixture.detectChanges();

    const passwordInput = fixture.debugElement.query(
      By.css('[data-ut="jwt-token-password-input"]')
    );
    expect(passwordInput).toBeFalsy();

    const getTokenBtn = fixture.debugElement.query(
      By.css('[data-ut="get-jwt-token-btn"]')
    );
    expect(getTokenBtn).toBeFalsy();

    const tokenTextarea = fixture.debugElement.query(
      By.css('[data-ut="jwt-token-txt"]')
    );
    expect(tokenTextarea).toBeTruthy();
  });
});
