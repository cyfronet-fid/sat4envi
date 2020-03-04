import {Pipe, PipeTransform} from '@angular/core';
import {IMessage, ISortedMessages, MessageType} from '../types/messages';

@Pipe({
  name: 'sortMessages'
})
export class SortMessagesPipe implements PipeTransform {
  transform(value: IMessage[], args?: any): ISortedMessages[] {
    return [{type: MessageType.Error, messages: value.filter(message => message.type === MessageType.Error)},
      {type: MessageType.Success, messages: value.filter(message => message.type === MessageType.Success)},
      {type: MessageType.Info, messages: value.filter(message => message.type === MessageType.Info)},
      {type: MessageType.Warning, messages: value.filter(message => message.type === MessageType.Warning)}]
      .filter(messageGroup => messageGroup.messages.length > 0);
  }

}
