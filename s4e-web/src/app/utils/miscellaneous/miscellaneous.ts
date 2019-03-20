import {FormControl, FormGroup} from '@angular/forms';
import {JsonConvert} from 'json2typescript';

export function disableEnableForm(disable: boolean, form: FormGroup|FormControl) {
  if (disable) {
    form.disable();
  } else {
    form.enable();
  }
}


export function deserializeJsonResponse<T, Y extends T>(json: T, SerializationClass: {new(): Y}): T;
export function deserializeJsonResponse<T, Y extends T>(json: T[], SerializationClass: {new(): Y}): T[];
export function deserializeJsonResponse<T, Y extends T>(json: T|T[], SerializationClass: {new(): Y}): T|T[];
export function deserializeJsonResponse<T, Y extends T>(json: any, SerializationClass: {new(): Y}): T;
export function deserializeJsonResponse<T, Y extends T>(json: any[], SerializationClass: {new(): Y}): T[];
export function deserializeJsonResponse<T, Y extends T>(json: any|any[], SerializationClass: {new(): Y}): T|T[] {
  const converter = new JsonConvert();
  const out = converter.deserialize(json, SerializationClass);
  if (out instanceof  Array) {
    return out.map(element => Object.assign({}, element));
  }
  return Object.assign({}, out);
}
