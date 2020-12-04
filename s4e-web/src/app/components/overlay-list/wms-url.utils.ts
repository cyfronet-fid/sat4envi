
import { AbstractControl } from '@angular/forms';
import {Overlay} from '../../views/map-view/state/overlay/overlay.model';

const VERSION_EXPRESSION = /version=\d+\.\d+\.\d+(&?)/i;
const REQUEST_EXPRESSION = /request=[a-zA-Z]*(&?)/i;
const LAYERS_EXPRESSION = /layers=[a-zA-Z._0-9,:-]*(&?)/i;
const STYLES_EXPRESSION = /styles=[a-zA-Z._0-9,:-]*(&?)/i;
const BBOX_EXPRESSION = /bbox=(-?)[.0-9]*,(-?)[.0-9]*,(-?)[.0-9]*,(-?)[.0-9]*(&?)/i;
const SRS_OR_CRS_EXPRESSION = /(srs|crs)=[a-zA-Z._0-9]*:[a-zA-Z._0-9]*(&?)/i;
const WIDTH_EXPRESSION = /width=[0-9]*(&?)/i;
const HEIGHT_EXPRESSION = /height=[0-9]*(&?)/i;
const FORMAT_EXPRESSION = /format=image\/(vnd.jpeg-png|vnd.jpeg-png8|png|gif|tiff|jpg)(&?)/i;
const WMS_SERVICE_EXPRESSION = /service=wms(&?)/i;
const TRANSPARENT_EXPRESSION = /transparent=(false|true)(&?)/i;

/**
 * Params to be extracted due to correct usage
 * of openlayers image wms
 */
const PARAMS_TO_BE_EXTRACTED = [
  LAYERS_EXPRESSION,
  STYLES_EXPRESSION,
  VERSION_EXPRESSION,
  REQUEST_EXPRESSION,
  WMS_SERVICE_EXPRESSION,
  TRANSPARENT_EXPRESSION,
  BBOX_EXPRESSION,
  SRS_OR_CRS_EXPRESSION,
  WIDTH_EXPRESSION,
  HEIGHT_EXPRESSION,
  FORMAT_EXPRESSION
];


export const WMS_URL_VALIDATORS = [
  /* OPTIONAL */
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

export function getBaseUrlAndParamsFrom(overlay: Partial<Overlay>): {url: string, [param: string]: any} {
  const urlBaseAndParams = overlay.url.split('?');
  if (urlBaseAndParams.length === 1) {
    return {url: urlBaseAndParams[0]};
  }

  const urlBase = urlBaseAndParams[0];
  const urlParams = urlBaseAndParams[1];

  return {url: urlBase, ..._extractParamsMapFrom(urlParams)};
}


interface IValidatorOutput {
  [key: string]: boolean;
}


/* OPTIONAL */
const VERSION_PARAM = 'version=';
function versionValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, VERSION_EXPRESSION, VERSION_PARAM, { version: true });
}

/* OPTIONAL */
const SRS_PARAM = 'srs=';
const CRS_PARAM = 'crs=';
function srsOrCrsValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, SRS_OR_CRS_EXPRESSION, CRS_PARAM, { srsOrCrs: true })
    || _hasOptionalParam(control.value, SRS_OR_CRS_EXPRESSION, SRS_PARAM, { srsOrCrs: true });
}

/* OPTIONAL */
const BBOX_PARAM = 'bbox=';
function bboxValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, BBOX_EXPRESSION, BBOX_PARAM, { bbox: true });
}

/* OPTIONAL */
const WIDTH_PARAM = 'width=';
function widthValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, WIDTH_EXPRESSION, WIDTH_PARAM, { width: true });
}

/* OPTIONAL */
const HEIGHT_PARAM = 'height=';
function heightValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, HEIGHT_EXPRESSION, HEIGHT_PARAM, { height: true });
}

/* OPTIONAL */
const FORMAT_PARAM = 'format=';
function formatValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, FORMAT_EXPRESSION, FORMAT_PARAM, { format: true });
}

/* OPTIONAL */
const SERVICE_PARAM = 'service=';
function wmsServiceValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, WMS_SERVICE_EXPRESSION, SERVICE_PARAM, { wmsService: true });
}

/* OPTIONAL */
const REQUEST_PARAM = 'request=';
function requestValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, REQUEST_EXPRESSION, REQUEST_PARAM, { request: true });
}

/* OPTIONAL */
const TRANSPARENT_PARAM = 'transparent=';
function transparentValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, TRANSPARENT_EXPRESSION, TRANSPARENT_PARAM, { transparent: true });
}

/* OPTIONAL */
const LAYERS_PARAM = 'layers=';
function layersValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, LAYERS_EXPRESSION, LAYERS_PARAM, { layers: true });
}

/* OPTIONAL */
const STYLES_PARAM = 'styles=';
function stylesValidator(control: AbstractControl): IValidatorOutput | null {
  return _hasOptionalParam(control.value, STYLES_EXPRESSION, STYLES_PARAM, { styles: true });
}


function _extractParamsMapFrom(partialUrl: string) {
  const extractedParams: {[param: string]: any} = {};
  PARAMS_TO_BE_EXTRACTED
    .map(regex => partialUrl.match(regex))
    .filter(match => !!match && match.length > 0)
    .map(match => match[0]
      .replace('&', '')
      .split('=')
    )
    .forEach(([key, value]) => extractedParams[key] = value);

  return extractedParams;
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
  return (typeof url === "string") && regex.test(url);
}

function _exist(url: string, paramName: string) {
  return (typeof url === "string")
    && url.toLowerCase().indexOf(paramName.toLowerCase()) > -1;
}
