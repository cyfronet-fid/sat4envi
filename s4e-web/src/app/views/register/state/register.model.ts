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

export interface RegisterFormState {
  email: string;
  name: string;
  surname: string;
  password: string;
  passwordRepeat: string;
  domain: string;
  usage: string;
  policy: boolean;
  country: string;
  recaptcha: string;
}

// tslint:disable-next-line:no-empty-interface
export interface RegisterState {}

interface DomainType {
  label: string;
  value: 'ATMOSPHERE' | 'MARINE' | 'EMERGENCY' | 'LAND' | 'SECURITY' | 'CLIMATE' | 'OTHER';
}
export const scientificDomainsTypes: DomainType[] = [
  {
    label: 'Atmosfera',
    value: 'ATMOSPHERE'
  },
  {
    label: 'Morze',
    value: 'MARINE'
  },
  {
    label: 'Nagłe wypadki',
    value: 'EMERGENCY'
  },
  {
    label: 'Ląd',
    value: 'LAND'
  },
  {
    label: 'Bezpieczeństwo',
    value: 'SECURITY'
  },
  {
    label: 'Klimat',
    value: 'CLIMATE'
  },
  {
    label: 'Inne',
    value: 'OTHER'
  }
];

interface UsageType {
  label: string;
  value: 'RESEARCH' | 'COMMERCIAL' | 'EDUCATION' | 'OTHER';
}
export const appUsageTypes: UsageType[] = [
  {
    label: 'Przeprowadzam badania',
    value: 'RESEARCH'
  },
  {
    label: 'W celach komercyjnych',
    value: 'COMMERCIAL'
  },
  {
    label: 'W celach edukacyjnych',
    value: 'EDUCATION'
  },
  {
    label: 'Inny',
    value: 'OTHER'
  }
];
