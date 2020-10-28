import { AbstractControl } from '@angular/forms';

const COMMA_SEPARATED_EMAILS_EXPRESSION = /^ *((,? *)((\.?)[a-zA-Z0-9_]+)+@((\.?)[a-zA-Z0-9_]+)+)+ *$/i;

interface IValidatorOutput {
  [key: string]: boolean;
}

export function emailListValidator(control: AbstractControl): IValidatorOutput | null {
  return !control.value ? null :
    (_hasMatch(control.value, COMMA_SEPARATED_EMAILS_EXPRESSION) ? null : { emails: true });
}

function _hasMatch(url: string, regex: RegExp) {
  return (typeof url === "string") && regex.test(url);
}
