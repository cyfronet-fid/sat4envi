import {Component, Host, Input, OnInit, Optional, SkipSelf} from '@angular/core';
import {ControlContainer} from '@angular/forms';
import {IMessage, MessageType} from '../types/messages';
import {S4eConfig} from '../../utils/initializer/config.service';

@Component({
  selector: 'ext-messages',
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.scss'],
})
export class MessagesComponent implements OnInit {
  MessageType = MessageType;

  constructor(@Optional() @Host() @SkipSelf() public cc: ControlContainer,
              private constants: S4eConfig) {
  }

  _messages: IMessage[] = [];

  get messages(): IMessage[] {
    let errors: IMessage[] = [];
    if (this.cc != null && this.cc.errors != null && this.cc.errors[this.constants.generalErrorKey] != null) {
      errors = <IMessage[]>(this.cc.errors[this.constants.generalErrorKey] as string[]).map(err => ({
        content: err,
        type: MessageType.Error
      }));
    }

    return [...this._messages, ...errors];
  }

  @Input() set messages(val: IMessage[]) {
    this._messages = val || [];
  }

  ngOnInit() {
  }

}
