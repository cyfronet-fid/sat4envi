import {FormControl, Validators} from '@angular/forms';

export interface SentinelParam {
  queryParam: string;
  type: string;
  [property: string]: any;
}

export interface SentinelFloatParam extends SentinelParam {
  type: 'float';
  min: number;
  max: number;
}

export function isSentinelFloatParam(param: SentinelParam): param is SentinelFloatParam {
  return param.type == 'float';
}

export interface SentinelDateTimeParam extends SentinelParam {
  type: 'datetime';
}

export function isSentinelDateTimeParam(param: SentinelParam): param is SentinelDateTimeParam {
  return param.type == 'datetime';
}

export interface SentinelTextParam extends SentinelParam {
  type: 'text';
}

export function isSentinelTextParam(param: SentinelParam): param is SentinelTextParam {
  return param.type == 'text';
}

export interface SentinelSelectParam extends SentinelParam {
  type: 'select';
  values: string|null[];
}

export function isSentinelSelectParam(param: SentinelParam): param is SentinelSelectParam {
  return param.type == 'select';
}

export interface SentinelSection {
  name: string;
  params: SentinelParam[];
}

export interface SentinelSearchMetadata {
  common: {
    params: SentinelParam[]
  },
  sections: SentinelSection[]
}

export function convertSentinelParam2FormControl(formControlDef: SentinelParam): FormControl {
  const fc = new FormControl();

  if (isSentinelSelectParam(formControlDef)) {
    // set value to the first option from the available select options
    fc.setValue(formControlDef.values[0]);
    fc.setValidators([Validators.required])
  } else if (isSentinelFloatParam(formControlDef)) {
    const validators = [];
    if(formControlDef.min != null) {
      validators.push(Validators.min(formControlDef.min));
    }
    if(formControlDef.max != null) {
      validators.push(Validators.max(formControlDef.max));
    }
    fc.setValidators(validators);
  }

  return fc;
}
