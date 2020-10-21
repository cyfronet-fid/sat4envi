import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { ExpertHelpForm } from "../expert-help-modal/expert-help-modal.model";
import environment from "src/environments/environment";

@Injectable({providedIn: 'root'})
export class ExpertHelpService {
  constructor(private _http: HttpClient) {}

  sendHelpRequest$(request: ExpertHelpForm) {
    const url = `${environment.apiPrefixV1}/expert-help`;
    return this._http.post(url, request);
  }
}
