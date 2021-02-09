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

import {AbstractControl} from '@angular/forms';
import {Overlay} from '../../views/map-view/state/overlay/overlay.model';
import * as url from 'url';

const VERSION_EXPRESSION = /version=\d+\.\d+\.\d+/i;
const REQUEST_EXPRESSION = /request=[a-z]*/i;
const LAYERS_EXPRESSION = /layers=(([0-9a-z.:_-]+(,?))+)/i;
const STYLES_EXPRESSION = /styles=([a-z._0-9:-]*(,?))+/i;
const BBOX_EXPRESSION = /bbox=(-?)[.0-9]*,(-?)[.0-9]*,(-?)[.0-9]*,(-?)[.0-9]*/i;
const SRS_OR_CRS_EXPRESSION = /(srs|crs)=[a-z._0-9]*:[a-z._0-9]*/i;
const WIDTH_EXPRESSION = /width=[0-9]*/i;
const HEIGHT_EXPRESSION = /height=[0-9]*/i;
const FORMAT_EXPRESSION = /format=image\/(vnd.jpeg-png|vnd.jpeg-png8|png|gif|tiff|jpg)/i;
const WMS_SERVICE_EXPRESSION = /service=wms/i;
const TRANSPARENT_EXPRESSION = /transparent=(false|true)/i;

/**
 * Wms url optional params validators for reactive forms
 */
export const OPTIONAL_WMS_URL_QUERY_PARAMS_VALIDATORS = [
  wmsServiceValidator,
  requestValidator,
  layersValidator,
  stylesValidator,
  transparentValidator,
  versionValidator,
  srsOrCrsValidator,
  bboxValidator,
  widthValidator,
  heightValidator,
  formatValidator
];

/**
 * Change url string into object representation
 * to operate on it's base url and query parameters
 * @class UrlParser
 */
export class UrlParser {
  private _urlBase: string | null = null;
  private _paramsWithValues: {[param: string]: string} = {};

  constructor(url: string) {
    this._setURl(url);
  }

  getUrlBase(): string | null {
    return this._urlBase;
  }
  getFullUrl(paramsUpperCase = true): string | null {
    return bindParamsWithUrl(this._urlBase, this._paramsWithValues, paramsUpperCase);
  }
  getParamsWithValues(paramsUpperCase = true): {[param: string]: string} {
    return paramsUpperCase
      ? Object.keys(this._paramsWithValues)
          .map(fieldName => [
            fieldName.toUpperCase(),
            this._paramsWithValues[fieldName]
          ])
          .map(([fieldName, value]) => ({[fieldName]: value}))
          .reduce(
            (paramsWithValues, paramWithValue) =>
              (paramsWithValues = {
                ...paramsWithValues,
                ...paramWithValue
              }),
            {}
          )
      : this._paramsWithValues;
  }
  getParamValueOf(field: string): string | null {
    if (!this.has(field)) {
      return null;
    }

    return this._paramsWithValues[field.toLowerCase()];
  }
  setValues(field: string, ...values: string[]) {
    const valuesAsList = !!values && values.join(',');
    this._paramsWithValues[field.toLowerCase()] = valuesAsList;
  }
  addValues(field: string, ...values: string[]) {
    field = findFieldIn(this._paramsWithValues, field) || field.toLowerCase();
    const valuesAsList = !!values && values.join(',');
    const actualValue = this._paramsWithValues[field];
    if (actualValue) {
      this._paramsWithValues[field] = `${actualValue},${valuesAsList}`;
      return;
    }

    this._paramsWithValues[field] = valuesAsList;
  }
  removeValues(field: string, ...values: string[]) {
    if (!this.has(field)) {
      return;
    }

    field = findFieldIn(this._paramsWithValues, field);

    values.forEach(value => {
      if (!this.has(field)) {
        return;
      }

      const hasExactValue = this._paramsWithValues[field] === value;
      if (hasExactValue) {
        this.remove(field);
        return;
      }

      this._paramsWithValues[field] = this._paramsWithValues[field].replace(
        new RegExp(`(,?)${value}`),
        ''
      );
    });

    if (this.has(field) && this._paramsWithValues[field].startsWith(',')) {
      this._paramsWithValues[field] = this._paramsWithValues[field].substr(1);
    }
  }
  remove(...fields: string[]): void {
    fields.forEach(field => {
      if (!this.has(field)) {
        return;
      }

      field = findFieldIn(this._paramsWithValues, field);
      delete this._paramsWithValues[field];
    });
  }
  has(field: string): boolean {
    return !!findFieldIn(this._paramsWithValues, field);
  }
  hasValue(field: string, value: string) {
    if (!this.has(field)) {
      return false;
    }

    field = findFieldIn(this._paramsWithValues, field);
    return this._paramsWithValues[field].includes(value);
  }

  private _setURl(url: string | null = null): void {
    url = url.trim();
    if (!url || url === '') {
      this._urlBase = null;
      this._paramsWithValues = {};
      return;
    }

    this._urlBase = url.split('?')[0];
    this._paramsWithValues = extractParamsWithValues(url);
  }
}

/////////////////////////////////
// URL parser
/////////////////////////////////

function findFieldIn(map: Object, field: string) {
  return Object.keys(map).find(
    key => !!field && key.toLowerCase() === field.toLowerCase()
  );
}

function bindParamsWithUrl(
  urlBase: string,
  paramsWithValues: {[field: string]: string},
  paramsUpperCase = true
): string {
  if (!urlBase || urlBase.includes('?')) {
    return urlBase;
  }

  const urlParamsWithValues = Object.keys(paramsWithValues)
    .map(
      field =>
        `${paramsUpperCase ? field.toUpperCase() : field}=${paramsWithValues[field]}`
    )
    .join('&');
  return `${urlBase}${
    Object.keys(urlParamsWithValues).length > 0 ? '?' : ''
  }${urlParamsWithValues}`;
}

function extractParamsWithValues(url: string) {
  const hasParams = url.includes('?');
  if (!hasParams) {
    return {};
  }

  const urlParams = url.split('?')[1];
  const urlParamsAreValid = urlParams.match(
    /([a-z._0-9:-]*=(([a-z._0-9:-]*(,?))+)(&?))+/i
  );
  if (
    !urlParamsAreValid ||
    (!!urlParamsAreValid && urlParamsAreValid.length === 0)
  ) {
    return {};
  }

  const paramsWithValues = urlParams
    .split('&')
    .map(paramWithValue => paramWithValue.split('='))
    .map(([fieldName, value]) => ({[fieldName.toLowerCase()]: value}))
    .reduce(
      (paramsWithValues, paramWithValue) =>
        (paramsWithValues = {
          ...paramsWithValues,
          ...paramWithValue
        }),
      {}
    );

  return paramsWithValues;
}

/////////////////////////////////
// Wms form validators
/////////////////////////////////
interface IValidatorOutput {
  [key: string]: boolean;
}

/* OPTIONAL */
const VERSION_PARAM = 'version=';
function versionValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, VERSION_EXPRESSION, VERSION_PARAM, {
    version: true
  });
}

/* OPTIONAL */
const SRS_PARAM = 'srs=';
const CRS_PARAM = 'crs=';
function srsOrCrsValidator(control: AbstractControl): IValidatorOutput | null {
  return (
    _hasOptionalParam(control.value, SRS_OR_CRS_EXPRESSION, CRS_PARAM, {
      srsOrCrs: true
    }) ||
    _hasOptionalParam(control.value, SRS_OR_CRS_EXPRESSION, SRS_PARAM, {
      srsOrCrs: true
    })
  );
}

/* OPTIONAL */
const BBOX_PARAM = 'bbox=';
function bboxValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, BBOX_EXPRESSION, BBOX_PARAM, {bbox: true});
}

/* OPTIONAL */
const WIDTH_PARAM = 'width=';
function widthValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, WIDTH_EXPRESSION, WIDTH_PARAM, {
    width: true
  });
}

/* OPTIONAL */
const HEIGHT_PARAM = 'height=';
function heightValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, HEIGHT_EXPRESSION, HEIGHT_PARAM, {
    height: true
  });
}

/* OPTIONAL */
const FORMAT_PARAM = 'format=';
function formatValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, FORMAT_EXPRESSION, FORMAT_PARAM, {
    format: true
  });
}

/* OPTIONAL */
const SERVICE_PARAM = 'service=';
function wmsServiceValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, WMS_SERVICE_EXPRESSION, SERVICE_PARAM, {
    wmsService: true
  });
}

/* OPTIONAL */
const REQUEST_PARAM = 'request=';
function requestValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, REQUEST_EXPRESSION, REQUEST_PARAM, {
    request: true
  });
}

/* OPTIONAL */
const TRANSPARENT_PARAM = 'transparent=';
function transparentValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(
    control.value,
    TRANSPARENT_EXPRESSION,
    TRANSPARENT_PARAM,
    {transparent: true}
  );
}

/* OPTIONAL */
const LAYERS_PARAM = 'layers=';
function layersValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, LAYERS_EXPRESSION, LAYERS_PARAM, {
    layers: true
  });
}

/* OPTIONAL */
const STYLES_PARAM = 'styles=';
function stylesValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, STYLES_EXPRESSION, STYLES_PARAM, {
    styles: true
  });
}

function _hasOptionalParam(
  url: string,
  paramRegex: RegExp,
  paramName: string,
  output: IValidatorOutput
): IValidatorOutput | null {
  const hasOptionalParam = !_exist(url, paramName) || _hasMatch(url, paramRegex);
  return hasOptionalParam ? null : output;
}

function _hasMatch(url: string, regex: RegExp) {
  return typeof url === 'string' && regex.test(url);
}

function _exist(url: string, paramName: string) {
  return (
    typeof url === 'string' &&
    url.toLowerCase().indexOf(paramName.toLowerCase()) > -1
  );
}
